package com.github.awant.habrareader.actors

import java.util.Date

import scala.concurrent.duration._
import akka.actor.{Actor, ActorRef, Props}
import com.github.awant.habrareader.models
import com.github.awant.habrareader.loaders.HabrArticlesDownloader
import com.github.awant.habrareader.utils.DateUtils

import scala.concurrent.ExecutionContext.Implicits.global


object ShopActor {
  def props(updatePostsInterval: FiniteDuration, library: ActorRef): Props =
    Props(new ShopActor(updatePostsInterval, library))

  final case class UpdatePosts()
}

class ShopActor private(updatePostsInterval: FiniteDuration, library: ActorRef) extends Actor {
  import ShopActor._

  var lastTimeUpdate: Date = DateUtils.currentDate

  override def preStart(): Unit = {
    context.system.scheduler.schedule(0.second, updatePostsInterval, self, UpdatePosts)
  }

  override def receive: Receive = {
    case UpdatePosts => updatePosts()
  }

  def updatePosts(): Unit = {
    val from = lastTimeUpdate
    val to = DateUtils.add(from, updatePostsInterval)

    val habrArticles = HabrArticlesDownloader.get(from, to)

    lastTimeUpdate = DateUtils.add(lastTimeUpdate, updatePostsInterval)

    val posts = habrArticles.map(article => models.Post(
      link = article.link,
      title = article.title,
      description = article.description,
      author = article.author,
      categories = article.categories.toSeq,
      upVotes = article.upVotes,
      downVotes = article.downVotes,
      viewsCount = article.viewsCount,
      commentsCount = article.commentsCount,
      bookmarksCount = article.bookmarksCount,
      updateDate = DateUtils.currentDate))
    library ! LibraryActor.PostsUpdating(posts)
  }

}
