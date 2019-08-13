package com.github.awant.habrareader.models

import io.circe.syntax._
import io.circe._

case class Chat(id: Long, title: String)

object Chat {
  implicit val encoder: Encoder[Chat] = (chat: Chat) => {
    Json.obj(
      "id" -> chat.id.asJson,
      "title" -> chat.title.asJson
    )
  }

  implicit val decoder: Decoder[Chat] = (c: HCursor) => {
    for {
      id <- c.downField("id").as[Long]
      title <- c.downField("title").as[String]
    } yield Chat(id, title)
  }
}
