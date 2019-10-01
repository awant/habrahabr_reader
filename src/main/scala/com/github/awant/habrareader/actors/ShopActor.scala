package com.github.awant.habrareader.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.github.awant.habrareader.AppConfig.ShopActorConfig
import com.github.awant.habrareader.loaders.HabrArticlesDownloader
import com.github.awant.habrareader.models
import com.github.awant.habrareader.utils.DateUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


object ShopActor {
  def props(config: ShopActorConfig, library: ActorRef): Props =
    Props(new ShopActor(config.articlesUpdateTimeSeconds.seconds, library))

  final case class UpdatePosts()
}

class ShopActor private(updatePostsInterval: FiniteDuration, library: ActorRef) extends Actor with ActorLogging {

  import ShopActor._

  override def preStart(): Unit = {
    context.system.scheduler.schedule(0.second, updatePostsInterval, self, UpdatePosts)
  }

  override def receive: Receive = {
    case UpdatePosts => updatePosts()
  }

  def updatePosts(): Unit = {
    log.debug("downloading new posts")
    val now = DateUtils.currentDate

    val habrArticles = HabrArticlesDownloader.getArticles()

    val posts = habrArticles.map(article => models.Post(
      id = article.id,
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
      updateDate = now)
    )
    log.debug(s"updating posts:\n${posts.mkString("\n")}")

    library ! LibraryActor.PostsUpdating(posts)
  }
}
