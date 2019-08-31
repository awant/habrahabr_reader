package com.github.awant.habrareader.actors

import scala.concurrent.duration._
import akka.actor.{Actor, ActorRef, Props}
import com.github.awant.habrareader.habr.HabrParser
import com.github.awant.habrareader.models
import com.github.awant.habrareader.utils.DateUtils

import scala.concurrent.ExecutionContext.Implicits.global


object ShopActor {
  def props(updatePostsInterval: FiniteDuration, library: ActorRef): Props =
    Props(new ShopActor(updatePostsInterval, library))

  final case class UpdatePosts()
}

class ShopActor private(updatePostsInterval: FiniteDuration, library: ActorRef) extends Actor {
  import ShopActor._

  override def preStart(): Unit = {
    context.system.scheduler.schedule(0.second, updatePostsInterval, self, UpdatePosts)
  }

  override def receive: Receive = {
    case UpdatePosts => updatePosts()
  }

  def updatePosts(): Unit = {
    // TODO: download posts, parse posts, store in the library
    val posts = HabrParser.loadPosts().map(article => models.Post(
      link = article.link,
      title = article.title,
      description = article.description,
      author = article.author,
      upVotes = article.rating.get.upVotes,
      downVotes = article.rating.get.downVotes,
      viewsCount = article.rating.get.viewsCount,
      commentsCount = article.rating.get.commentsCount,
      bookmarksCount = article.rating.get.bookmarksCount,
      updateDate = DateUtils.currentDate))
    library ! LibraryActor.PostsUpdating(posts)
  }

}
