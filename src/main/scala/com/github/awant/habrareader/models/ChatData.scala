package com.github.awant.habrareader.models

import org.mongodb.scala._

import scala.concurrent.{ExecutionContext, Future}

class ChatData(collection: MongoCollection[Chat])(implicit ec: ExecutionContext) {
  def findById(id: Long): Future[Option[Chat]] =
      collection
        .find(Document("id" -> id))
        .first
        .head
        .map(Option(_))

    def save(chat: Chat): Future[Long] =
      collection
        .insertOne(chat)
        .head
        .map { _ => chat.id }
}
