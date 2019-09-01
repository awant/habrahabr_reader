package com.github.awant.habrareader.models

import java.util.Date

import com.github.awant.habrareader.actors.TgBotActor.Reply
import com.github.awant.habrareader.utils.DateUtils
import org.mongodb.scala._

import scala.concurrent.{ExecutionContext, Future}
import org.mongodb.scala.result.{DeleteResult, UpdateResult}
import com.mongodb.client.model.UpdateOptions

import scala.util.{Failure, Success}

class ChatData(chatCollection: MongoCollection[Chat],
               postCollection: MongoCollection[Post])(implicit ec: ExecutionContext) {

  def updateSubscription(id: Long, subscription: Boolean): Future[UpdateResult] = {
    val options = new UpdateOptions().upsert(true)
    chatCollection.updateOne(Document("id" -> id),
      Document("$set" -> Document(
        "subscription" -> subscription,
        "createdDate" -> DateUtils.currentDate)), // last updated time
      options).toFuture
  }

  def getChatSettings(id: Long): Future[String] = {
    chatCollection.find(Document("id" -> id))
      .first
      .head
      .map("subscription: " + _.subscription)
  }

  private def predicate(chat: Chat, post: Post): Boolean = {
    true
  }

  def getUpdates(fromDate: Date): Future[Seq[(Chat, Post)]] = {
    val chats = chatCollection.find(Document("subscription" -> true))
    val posts = postCollection.find(Document("updateDate" -> Document("$gt" -> fromDate)))

    chats.flatMap(chat => posts.map(post => (chat, post)).filter{case (c, p) => predicate(c, p)}).toFuture()
  }

  def save(posts: Seq[Post]): Unit = {
    postCollection.insertMany(posts).toFuture().onComplete{
      case Success(_) => _
      case Failure(err) => _
    }
  }

}
