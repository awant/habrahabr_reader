package com.github.awant.habrareader.actors

import akka.actor.{Actor, ActorRef, Props}
import com.github.awant.habrareader.actors.TgBotActor.Reply
import com.github.awant.habrareader.models.ChatData

import scala.concurrent.duration._
import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}


object LibraryActor {
  def props(subscriptionReplyInterval: FiniteDuration, chatData: ChatData): Props =
    Props(new LibraryActor(subscriptionReplyInterval, chatData))

  final case class BotSubscription(subscriber: ActorRef)

  final case class SubscriptionChanging(chatId: Long, subscribe: Boolean)
  final case class SettingsGetting(chatId: Long)
  final case class SettingsChanging(chaitId: Long, body: String)
  final case class NewPostsSending()
}

class LibraryActor(subscriptionReplyInterval: FiniteDuration, chatData: ChatData) extends Actor {
  import LibraryActor._

  implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  // Can be extended to several subscribed bots
  var subscribedBot: ActorRef = _

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
      chatData.getUpdates.onComplete {
        case Success(updates) => println("Success") ; updates.foreach{case (chat, post) => subscribedBot ! Reply(chat.id, post.title)}
        case Failure(_) => println("failure")
      }
  }
}
