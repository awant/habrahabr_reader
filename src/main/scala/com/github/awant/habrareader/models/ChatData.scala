package com.github.awant.habrareader.models

import org.mongodb.scala._

import scala.concurrent.{ExecutionContext, Future}
import org.mongodb.scala.result.{ DeleteResult, UpdateResult }
import com.mongodb.client.model.UpdateOptions

class ChatData(chatCollection: MongoCollection[Chat],
               postCollection: MongoCollection[Post],
               eventCollection: MongoCollection[Event])(implicit ec: ExecutionContext) {

  def updateSubscription(id: Long, subscription: Boolean): Future[UpdateResult] = {
    val options = new UpdateOptions().upsert(true)
    chatCollection.updateOne(Document("id" -> id),
      Document("$set" -> Document("subscription" -> subscription)),
      options).toFuture
  }

  def getChatSettings(id: Long): Future[String] = {
    chatCollection.find(Document("id" -> id))
      .first
      .head
      .map("subscription: " + _.subscription)
  }

  def getUpdates: Future[Seq[(Chat, Post)]] = {
    val chats = chatCollection.find()
    chats.map(chat => (chat, Post(0, "testLink", "testTitle", "testDescription", "testAuthor"))).toFuture()
  }

  def save(post: Post): Future[Long] =
    postCollection.insertOne(post)
      .head
      .map { _ => post.id }
}
