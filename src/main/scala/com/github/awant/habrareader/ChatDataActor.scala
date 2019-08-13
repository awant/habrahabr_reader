package com.github.awant.habrareader

import akka.actor.{Actor, Props}
import com.bot4s.telegram.models.Chat
import com.github.awant.habrareader.models.ChatData

object ChatDataActor {
  def props(chatData: ChatData): Props = Props(new ChatDataActor(chatData))

  final case class RegisterChat(chat: Chat)
  final case class GetChat(id: Long)
  final case class UpdateChat(id: Long, chat: Chat)
  final case class RemoveChat(id: Long)
}

class ChatDataActor(chatData: ChatData) extends Actor {
  import ChatDataActor._

  override def receive: Receive = {
    case RegisterChat(chat) => chatData.save(com.github.awant.habrareader.models.Chat(chat.id, chat.title.getOrElse("")))
  }

}
