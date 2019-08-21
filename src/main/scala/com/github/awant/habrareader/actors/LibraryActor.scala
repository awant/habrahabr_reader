package com.github.awant.habrareader.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.github.awant.habrareader.actors.TgBotActor.Reply
import com.github.awant.habrareader.models

import scala.concurrent.duration._
import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}


object LibraryActor {
  def props(subscriptionReplyInterval: FiniteDuration, chatData: models.ChatData): Props =
    Props(new LibraryActor(subscriptionReplyInterval, chatData))

  final case class BotSubscription(subscriber: ActorRef)

  final case class SubscriptionChanging(chatId: Long, subscribe: Boolean)
  final case class SettingsGetting(chatId: Long)
  final case class SettingsChanging(chaitId: Long, body: String)
  final case class NewPostsSending()
  final case class PostsUpdating(posts: Seq[models.Post])
}

class LibraryActor(subscriptionReplyInterval: FiniteDuration, chatData: models.ChatData) extends Actor with ActorLogging {
  import LibraryActor._

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  // Can be extended to several subscribed bots
  var subscribedBot: ActorRef = _
  var currentDate: Int = 0

  override def preStart(): Unit = {
    context.system.scheduler.schedule(subscriptionReplyInterval, subscriptionReplyInterval, self, NewPostsSending)
  }

  override def receive: Receive = {
    case BotSubscription(subscriber) => subscribedBot = subscriber

    case SubscriptionChanging(chatId: Long, subscribe: Boolean) => chatData.updateSubscription(chatId, subscribe)
    case SettingsGetting(chatId) =>
      chatData.getChatSettings(chatId).onComplete {
        case Success(settings) => subscribedBot ! Reply(chatId, settings)
        case Failure(_) => subscribedBot ! Reply(chatId, "")
      }
    case NewPostsSending =>
      chatData.getUpdates(currentDate).onComplete {
        case Success(updates) => updates.foreach{case (chat, post) => subscribedBot ! Reply(chat.id, post.title)}
        case Failure(e) => log.error(s"$e")
      }
      currentDate += 1
    case PostsUpdating(posts) =>
      chatData.save(posts)
  }
}
