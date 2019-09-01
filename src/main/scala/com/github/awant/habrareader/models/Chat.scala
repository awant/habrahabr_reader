package com.github.awant.habrareader.models

import com.github.awant.habrareader.utils.DateUtils

import java.util.Date
import io.circe.syntax._
import io.circe._

case class Chat(id: Long, createdDate: Date, subscription: Boolean)

object Chat {
  implicit val encoder: Encoder[Chat] = (chat: Chat) => {
    Json.obj(
      "id" -> chat.id.asJson,
      "subscription" -> chat.subscription.asJson,
      "createdDate" -> DateUtils.convertToStr(chat.createdDate).asJson
    )
  }

  implicit val decoder: Decoder[Chat] = (c: HCursor) => {
    for {
      id <- c.downField("id").as[Long]
      subscription <- c.downField("subscription").as[Boolean]
      createdDate <- c.downField("createdDate").as[String]
    } yield Chat(id, DateUtils.convertToDate(createdDate), subscription)
  }
}
