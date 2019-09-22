package com.github.awant.habrareader.models

import java.util.Date

import com.github.awant.habrareader.utils.DateUtils
import io.circe._
import io.circe.syntax._

case class Event(id: Long, chatId: Long, messageId: Long, postId: Long, update: Date)

object Event {
  implicit val encoder: Encoder[Event] = (event: Event) => {
    Json.obj(
      "id" -> event.id.asJson,
      "chatId" -> event.chatId.asJson,
      "messageId" -> event.messageId.asJson,
      "postId" -> event.postId.asJson,
      "update" -> DateUtils.convertToStr(event.update).asJson
    )
  }

  implicit val decoder: Decoder[Event] = (c: HCursor) => {
    for {
      id <- c.downField("id").as[Long]
      chatId <- c.downField("chatId").as[Long]
      messageId <- c.downField("messageId").as[Long]
      postId <- c.downField("postId").as[Long]
      update <- c.downField("update").as[String]
    } yield Event(id, chatId, messageId, postId, DateUtils.convertToDate(update))
  }
}
