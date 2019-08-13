package com.github.awant.habrareader.actors

import akka.actor.{Actor, ActorRef, Props}
import com.bot4s.telegram.methods.SendMessage
import com.bot4s.telegram.models.Chat
import com.github.awant.habrareader.ChatDataActor
import com.github.awant.habrareader.actors.AkkaSystem.BotConfig

import scala.collection.mutable

object TgHandlerActor {
  def props(botConfig: BotConfig, cacheBot: ActorRef, chatDataActor: ActorRef) =
    Props(new TgHandlerActor(botConfig, cacheBot, chatDataActor))
}

class TgHandlerActor private(botConfig: BotConfig, cacheBot: ActorRef, chatDataActor: ActorRef) extends Actor {

  val chats = new mutable.HashMap[Long, ActorRef]()

  lazy val tgBot: ActorRef = {
    context.actorOf(TgBotActor.props(botConfig, self))
  }

  override def preStart(): Unit = {
    tgBot
    cacheBot ! HabrArticlesCache.Subscribe(self, receiveNew = true, receiveUpdates = true, receiveExisting = true)
  }

  override def receive: Receive = {
    case msg: HabrArticlesCache.PostAdded =>
      brodcastToChats(msg)

    case msg: HabrArticlesCache.PostUpdated =>
      brodcastToChats(msg)

    case msg: SendMessage =>
      tgBot ! msg

    case TgBotActor.MessageReceived(msg) =>
      val chatActor = chats.getOrElseUpdate(msg.chat.id, createChat(msg.chat))
      chatActor ! msg
      chatDataActor ! ChatDataActor.RegisterChat(msg.chat)
  }

  private def brodcastToChats[T](msg: T): Unit =
    chats.values.foreach(_ ! msg)

  private def createChat(chat: Chat): ActorRef = {
    val name = s"${chat.id}${chat.title.orElse(chat.username).getOrElse("")}"
    context.actorOf(TgChatActor.props(chat, self), name)
  }
}