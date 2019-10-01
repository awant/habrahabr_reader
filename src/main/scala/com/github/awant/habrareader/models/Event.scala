package com.github.awant.habrareader.models

import java.util.Date

import io.circe._
import io.circe.syntax._
import com.github.awant.habrareader.utils.DateUtils._

case class Event(chatId: Long, postId: Long, updateDate: Date, status: String)

object Event {
  def getSendEvent(chatId: Long, postId: Long, updateDate: Date): Event = Event(chatId, postId, updateDate, "send")

  implicit val encoder: Encoder[Event] = (event: Event) => {
    Json.obj(
      "chatId" := event.chatId,
      "postId" := event.postId,
      "updateDate" := event.updateDate,
      "status" := event.status
    )
  }

  implicit val decoder: Decoder[Event] = (c: HCursor) => {
    for {
      chatId <- c.get[Long]("chatId")
      postId <- c.get[Long]("postId")
      updateDate <- c.get[Date]("updateDate")
      status <- c.get[String]("status")
    } yield Event(chatId, postId, updateDate, status)
  }
}
