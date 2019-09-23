package com.github.awant.habrareader.models

import java.util.Date

import com.github.awant.habrareader.utils.DateUtils
import io.circe._
import io.circe.syntax._

case class Event(id: Long, chatId: Long, messageId: Long, postId: Long, update: Date)

object Event {
  private implicit val dateEncoder: Encoder[Date] =
    (date: Date) => DateUtils.convertToStr(date).asJson

  private implicit val dateDecoder: Decoder[Date] = Decoder[String].map(DateUtils.convertToDate(_))

  implicit val encoder: Encoder[Event] = (event: Event) => {
    Json.obj(
      "id" := event.id,
      "chatId" := event.chatId,
      "messageId" := event.messageId,
      "postId" := event.postId,
      "update" := event.update,
    )
  }

  implicitly[Decoder[Long]]

  implicit val decoder: Decoder[Event] = (c: HCursor) => {
    for {
      id <- c.downField("id").as[Long]
      chatId <- c.downField("chatId").as[Long]
      messageId <- c.downField("messageId").as[Long]
      postId <- c.downField("postId").as[Long]
      update <- c.downField("update").as[Date]
    } yield Event(id, chatId, messageId, postId, update)
  }
}
