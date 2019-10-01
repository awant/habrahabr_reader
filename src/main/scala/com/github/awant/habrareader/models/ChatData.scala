package com.github.awant.habrareader.models

import java.util.Date

import com.github.awant.habrareader.utils.DateUtils
import org.mongodb.scala._
import org.mongodb.scala.model.{ReplaceOptions, UpdateOptions}
import org.mongodb.scala.result.UpdateResult
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ChatData(chatCollection: MongoCollection[Chat],
               postCollection: MongoCollection[Post],
               eventCollection: MongoCollection[Event])(implicit ec: ExecutionContext) {

  private val log = LoggerFactory.getLogger(classOf[ChatData])

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
    if ((chat.authorScope == ChatScope.all) && chat.excludedAuthor.contains(post.author)) false
    else if ((chat.authorScope == ChatScope.none) && !chat.author.contains(post.author)) false
    // filter by categories
    else if ((chat.categoryScope == ChatScope.all) && post.categories.exists(chat.excludedCategory.contains)) false
    else if ((chat.categoryScope == ChatScope.none) && post.categories.forall(!chat.category.contains(_))) false
    else true
  }

  private def getNewUpdates(chats: Seq[Chat], posts: Seq[Post], events: Seq[Event]): Seq[(Chat, Post)] = {
    val idPairs = events.map(event => (event.chatId, event.postId)).toSet

    def postWasSent(chat: Chat, post: Post): Boolean = idPairs.contains((chat.id, post.id))

    for {
      chat <- chats
      post <- posts
      if !postWasSent(chat, post) && predicate(chat, post)
    } yield (chat, post)
  }

  def getUpdates(fromDate: Date): Future[Seq[(Chat, Post)]] = {
    val historyDateFrom = DateUtils.addDays(DateUtils.currentDate, -3)

    for {
      chats <- chatCollection.find(Document("subscription" -> true)).toFuture()
      posts <- postCollection.find(Document("updateDate" -> Document("$gt" -> fromDate))).toFuture()
      events <- eventCollection.find(Document("updateDate" -> Document("$gt" -> historyDateFrom))).toFuture()
    } yield getNewUpdates(chats, posts, events)
  }

  def updatePosts(posts: Seq[Post]): Unit = posts.foreach(updatePost)

  def updatePost(post: Post): Unit = {
    postCollection
      .replaceOne(Document("id" -> post.id), post, ReplaceOptions().upsert(true))
      .toFuture().onComplete {
      case Success(value) => log.debug(s"post was updated $value")
      case Failure(exception) => log.error(s"can't update post $post: $exception")
    }
  }

  def addEvent(event: Event): Future[Completed] = eventCollection.insertOne(event).toFuture()
}
