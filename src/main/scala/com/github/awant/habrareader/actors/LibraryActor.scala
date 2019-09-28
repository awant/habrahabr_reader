package com.github.awant.habrareader.actors

import java.util.Date

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.github.awant.habrareader.AppConfig.LibraryActorConfig
import com.github.awant.habrareader.actors.TgBotActor.{PostEdit, PostReply, Reply}
import com.github.awant.habrareader.models
import com.github.awant.habrareader.models.{Chat, ChatData, Event}
import com.github.awant.habrareader.utils.{ChangeCommand, DateUtils, SettingsRequestParser}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.util.{Failure, Success}


object LibraryActor {
  def props(config: LibraryActorConfig, chatData: models.ChatData): Props =
    Props(new LibraryActor(config.chatsUpdateTimeSeconds.seconds, chatData))

  final case class BotSubscription(subscriber: ActorRef)

  final case class PostWasSentToTg(event: Event)
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
  var chatDataLastTime: Date = DateUtils.currentDate

  override def preStart(): Unit = {
    context.system.scheduler.schedule(subscriptionReplyInterval, subscriptionReplyInterval, self, NewPostsSending)
  }

  override def receive: Receive = {
    case BotSubscription(subscriber) => subscribedBot = subscriber

    case SubscriptionChanging(chatId: Long, subscribe: Boolean) =>
      chatData.updateSubscription(chatId, subscribe).onComplete {
        case Success(_) =>
        case Failure(err) => println(err)
      }
    case SettingsChanging(chatId: Long, cmd: String) =>
      val settingsCmd = SettingsRequestParser.parse(cmd)
      settingsCmd.cmd match {
        case ChangeCommand.UNKNOWN => Reply(chatId, "Unknown command")
        case ChangeCommand.RESET => chatData.updateChat(Chat.withDefaultSettings(chatId))
        case ChangeCommand.CLEAR => chatData.updateChat(Chat.withEmptySettings(chatId))
        case ChangeCommand.SET => chatData.appendSettingToChat(chatId, settingsCmd.args.head, settingsCmd.args(1))
      }

    case SettingsGetting(chatId) =>
      chatData.getChatSettings(chatId).onComplete {
        case Success(settings) => subscribedBot ! Reply(chatId, settings)
        case Failure(_) => subscribedBot ! Reply(chatId, "")
      }
    case NewPostsSending =>
      processNewPostSending()
    case UpdateChatDataLastTime(date) =>
      chatDataLastTime = date
    case PostsUpdating(posts) =>
      chatData.updatePosts(posts)
    case PostWasSentToTg(event) =>
      chatData.addEvent(event)
  }

  private def processNewPostSending(): Unit = {
    val currentLast = chatDataLastTime

    chatData.getUpdates(currentLast).onComplete {
      case Success(updates) =>

        val newLastDate = updates.view.map(_.date).foldLeft(currentLast)(DateUtils.getLast)
        self ! UpdateChatDataLastTime(newLastDate)

        updates.foreach {
          case ChatData.Update(chat, post, None) =>
            subscribedBot ! PostReply(chat.id, post)
          case ChatData.Update(chat, post, Some(prevMessageId)) =>
            subscribedBot ! PostEdit(chat.id, prevMessageId, post)
        }
      case Failure(e) => log.error(s"$e")
    }
  }
}
