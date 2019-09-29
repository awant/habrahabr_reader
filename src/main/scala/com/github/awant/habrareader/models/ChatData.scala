package com.github.awant.habrareader.models

import java.util.Date

import com.github.awant.habrareader.utils.DateUtils
import org.mongodb.scala._
import org.mongodb.scala.model.{ReplaceOptions, UpdateOptions}
import org.mongodb.scala.result.UpdateResult
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object ChatData {

  case class Update(chat: Chat, post: Post, prevMessageId: Option[Int] = None) {
    def date: Date = post.updateDate
  }

}

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

  private def getUpdates(chats: Seq[Chat], posts: Seq[Post], events: Seq[Event]): Seq[ChatData.Update] = {
    def getLastPost(left: Post, right: Post): Post =
      if (left.updateDate.after(right.updateDate)) left else right

    def getLastEvent(left: Event, right: Event): Event =
      if (left.updateDate.after(right.updateDate)) left else right

    val eventsByChat: Map[Long, Seq[Event]] = events.groupBy(_.chatId)

    val lastPosts: Iterable[Post] = posts.groupBy(_.id).map { case (_, posts) =>
      posts.reduce(getLastPost)
    }

    for {
      chat <- chats
      eventsByPostId = eventsByChat.getOrElse(chat.id, List()).groupBy(_.postId)
      post <- lastPosts
      relatedEvents = eventsByPostId.get(post.id)
      if relatedEvents.nonEmpty || predicate(chat, post)
    } yield ChatData.Update(chat, post, relatedEvents.map(_.reduce(getLastEvent).messageId))
  }

  def getUpdates(fromDate: Date): Future[Seq[ChatData.Update]] = {
    val threeDaysBack = DateUtils.addDays(fromDate, -3)

    for {
      chats <- chatCollection.find(Document("subscription" -> true)).toFuture()
      posts <- postCollection.find(Document("updateDate" -> Document("$gt" -> fromDate))).toFuture()
      events <- eventCollection.find(Document("updateDate" -> Document("$gt" -> threeDaysBack))).toFuture()
    } yield getUpdates(chats, posts, events)
  }

  def updatePosts(posts: Seq[Post]): Unit =
    posts.foreach(updatePost)

  def updatePost(post: Post): Unit =
    postCollection
      .replaceOne(Document("link" -> post.link), post, ReplaceOptions().upsert(true))
      .toFuture().onComplete {
      case Success(value) => log.debug(s"update post ${post.link}: $value")
      case Failure(exception) => log.error(s"can't update post ${post.link}: $exception")
    }

  def addEvent(event: Event): Future[Completed] =
    eventCollection.insertOne(event).toFuture()
}
