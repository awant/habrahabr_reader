package com.github.awant.habrareader.models

import io.circe.syntax._
import io.circe._

case class Event(id: Long, chatId: Long, postId: Long)

object Event {
  implicit val encoder: Encoder[Event] = (event: Event) => {
    Json.obj(
      "id" -> event.id.asJson,
      "chatId" -> event.chatId.asJson,
      "postId" -> event.postId.asJson
    )
  }

  implicit val decoder: Decoder[Event] = (c: HCursor) => {
    for {
      id <- c.downField("id").as[Long]
      chatId <- c.downField("chatId").as[Long]
      postId <- c.downField("postId").as[Long]
    } yield Event(id, chatId, postId)
  }
}
