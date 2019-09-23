package com.github.awant.habrareader.models

import java.util.Date

import io.circe._
import io.circe.syntax._
import com.github.awant.habrareader.utils.DateUtils._

case class Event(chatId: Long, messageId: Int, postId: Long, update: Date)

object Event {

  implicit val encoder: Encoder[Event] = (event: Event) => {
    Json.obj(
      "chatId" := event.chatId,
      "messageId" := event.messageId,
      "postId" := event.postId,
      "update" := event.update,
    )
  }

  implicitly[Decoder[Long]]

  implicit val decoder: Decoder[Event] = (c: HCursor) => {
    for {
      chatId <- c.get[Long]("chatId")
      messageId <- c.get[Int]("messageId")
      postId <- c.get[Long]("postId")
      update <- c.get[Date]("update")
    } yield Event(chatId, messageId, postId, update)
  }
}
