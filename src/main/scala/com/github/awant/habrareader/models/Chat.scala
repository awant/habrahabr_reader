package com.github.awant.habrareader.models

import io.circe.syntax._
import io.circe._

case class Chat(id: Long, date: Int, subscription: Boolean)

object Chat {
  implicit val encoder: Encoder[Chat] = (chat: Chat) => {
    Json.obj(
      "id" -> chat.id.asJson,
      "date" -> chat.date.asJson,
      "subscription" -> chat.subscription.asJson
    )
  }

  implicit val decoder: Decoder[Chat] = (c: HCursor) => {
    for {
      id <- c.downField("id").as[Long]
      date <- c.downField("date").as[Int]
      subscription <- c.downField("subscription").as[Boolean]
    } yield Chat(id, date, subscription)
  }
}
