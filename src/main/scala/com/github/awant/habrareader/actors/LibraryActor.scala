package com.github.awant.habrareader.actors

import java.util.Date

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.github.awant.habrareader.AppConfig.LibraryActorConfig
import com.github.awant.habrareader.actors.TgBotActor.{PostReply, Reply}
import com.github.awant.habrareader.models
import com.github.awant.habrareader.utils.{ChangeCommand, DateUtils, SettingsRequestParser}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.util.{Failure, Success}


object LibraryActor {
  def props(config: LibraryActorConfig, chatData: models.ChatData): Props =
    Props(new LibraryActor(config.chatsUpdateTimeSeconds.seconds, chatData))

  final case class BotSubscription(subscriber: ActorRef)

  final case class PostWasSentToTg(event: models.Event)
  final case class SubscriptionChanging(chatId: Long, subscribe: Boolean)
  final case class SettingsGetting(chatId: Long)
  final case class SettingsChanging(chatId: Long, body: String)
  final case class NewPostsSending()
  final case class PostsUpdating(posts: Seq[models.Post])

  private final case class UpdateChatDataLastTime(date: Date)
}

class LibraryActor(subscriptionReplyInterval: FiniteDuration, chatData: models.ChatData) extends Actor with ActorLogging {
  import LibraryActor._

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  // Can be extended to several subscribed bots
  var subscribedBot: ActorRef = _
  var lastPostUpdatedDate: Date = DateUtils.yesterday

  override def preStart(): Unit = {
    context.system.scheduler.schedule(10.seconds, subscriptionReplyInterval, self, NewPostsSending)
  }

  override def receive: Receive = {
    case BotSubscription(subscriber) => subscribedBot = subscriber

    case SubscriptionChanging(chatId: Long, subscribe: Boolean) =>
      chatData.updateSubscription(chatId, subscribe).onComplete {
        case Success(_) =>
        case Failure(err) => log.error(err.toString)
      }
    case SettingsChanging(chatId: Long, cmd: String) =>
      log.debug(s"settingsChanging got: $cmd")
      val settingsCmd = SettingsRequestParser.parse(cmd)
      log.debug(s"settingsChanging parsed: ${settingsCmd.toString}")
      settingsCmd.cmd match {
        case ChangeCommand.UNKNOWN => subscribedBot ! Reply(chatId, settingsCmd.err)
        case ChangeCommand.RESET => chatData.updateChat(models.Chat.withDefaultSettings(chatId))
        case ChangeCommand.CLEAR => chatData.updateChat(models.Chat.withEmptySettings(chatId))
        case ChangeCommand.SET => chatData.appendSettingToChat(chatId, settingsCmd.args.head, settingsCmd.args(1))
      }

    case SettingsGetting(chatId) =>
      chatData.getChatSettings(chatId).onComplete {
        case Success(settings) => subscribedBot ! Reply(chatId, settings)
        case Failure(_) => subscribedBot ! Reply(chatId, "Can't find the chat settings. You should subscribe first")
      }
    case NewPostsSending => processNewPostSending()
    case PostsUpdating(posts) => chatData.updatePosts(posts)
    case PostWasSentToTg(event) => chatData.addEvent(event)
  }

  private def processNewPostSending(): Unit = {
    log.debug("sending new posts to chats")
    val dateFrom = lastPostUpdatedDate

    chatData.getUpdates(dateFrom).onComplete {
      case Success(updates) =>
        updates.foreach{case (chat, post) => subscribedBot ! PostReply(chat.id, post)}
        lastPostUpdatedDate = DateUtils.getMax(updates.map(_._2.updateDate) :+ lastPostUpdatedDate)
      case Failure(e) => log.error(s"$e")
    }
  }
}
