package com.github.awant.habrareader.models

import java.util.Date

import org.mongodb.scala._

import scala.concurrent.{ExecutionContext, Future}
import org.mongodb.scala.result.UpdateResult
import com.mongodb.client.model.{ReplaceOptions, UpdateOptions}

import scala.util.{Failure, Success}

class ChatData(chatCollection: MongoCollection[Chat],
               postCollection: MongoCollection[Post])(implicit ec: ExecutionContext) {

  def updateSubscription(id: Long, subscription: Boolean): Future[UpdateResult] = {
    val options = new ReplaceOptions().upsert(true)
    val chat = Chat.withDefaultSettings(id, subscription)
    chatCollection.replaceOne(Document("id" -> id), chat, options).toFuture
  }

  def updateChat(chat: Chat): Future[Chat] = {
    chatCollection.findOneAndReplace(Document("id" -> chat.id), chat).toFuture
  }

  def getChat(id: Long): Future[Chat] = {
    chatCollection.find(Document("id" -> id)).first.head
  }

  def appendSettingToChat(id: Long, field: String, value: String): Unit = {
    val options = new UpdateOptions().upsert(true)
    chatCollection.updateOne(Document("id" -> id),
      Document("$push" -> Document(field -> value)),
      options).toFuture
  }

  def getChatSettings(id: Long): Future[String] =
    chatCollection.find(Document("id" -> id)).first.head.map(_.getSettingsPrettify)

  private def predicate(chat: Chat, post: Post): Boolean = {
    // filter by author
    if ((chat.authorsScope == ChatScopeAll()) && chat.excludedAuthors.contains(post.author)) false
    else if ((chat.authorsScope == ChatScopeNone()) && !chat.authors.contains(post.author)) false
    // filter by categories
    else if ((chat.categoryScope == ChatScopeAll()) && post.categories.exists(chat.excludedCategories.contains(_))) false
    else if ((chat.categoryScope == ChatScopeNone()) && post.categories.forall(!chat.categories.contains(_))) false
    else true
  }

  def getUpdates(fromDate: Date): Future[Seq[(Chat, Post)]] = {
    val chats = chatCollection.find(Document("subscription" -> true))
    val posts = postCollection.find(Document("updateDate" -> Document("$gt" -> fromDate)))

    chats.flatMap(chat => posts.map(post => (chat, post)).filter{case (c, p) => predicate(c, p)}).toFuture()
  }

  def save(posts: Seq[Post]): Unit = {
    postCollection.insertMany(posts).toFuture().onComplete{
      case Success(_) => Nil
      case Failure(_) => Nil
    }
  }

}
