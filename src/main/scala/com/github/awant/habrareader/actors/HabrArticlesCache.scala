package com.github.awant.habrareader.actors

import akka.actor.{Actor, ActorRef, Props}
import cats.syntax.semigroup._
import com.github.awant.habrareader.habr.HabrArticle
import com.github.awant.habrareader.utils.UniqueQueue

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object HabrArticlesCache {
  def props(updateRssInterval: FiniteDuration, updateCacheInterval: FiniteDuration, habraParser: ActorRef): Props =
    Props(new HabrArticlesCache(updateRssInterval, updateCacheInterval, habraParser))

  final case class Subscribe(subscriber: ActorRef, receiveNew: Boolean, receiveUpdates: Boolean, receiveExisting: Boolean)
  final case class PostUpdated(article: HabrArticle)
  final case class PostAdded(article: HabrArticle)

  private case object RequestUpdateRss

  private case object RequestUpdateCache

}

/**
  * actor requests rss from time to time
  * saves articles
  * notifies actors subscribed to this about updates
  *
  * @param updateRssInterval : time between rss udates
  * @param habrParserActor   : actor parsing rss
  */
class HabrArticlesCache private(updateRssInterval: FiniteDuration, updateCacheInterval: FiniteDuration, habrParserActor: ActorRef) extends Actor {

  import HabrArticlesCache._

  val cachedArticles: mutable.Map[HabrArticle.Id, HabrArticle] = new mutable.HashMap()
  val newPostSubscribers = new mutable.HashSet[ActorRef]()
  val updatedPostSubscribers = new mutable.HashSet[ActorRef]()
  val queuedForUpdate = new UniqueQueue[HabrArticle]

  override def preStart(): Unit = {
    context.system.scheduler.schedule(0.second, updateRssInterval, self, RequestUpdateRss)
    context.system.scheduler.schedule(0.second, updateCacheInterval, self, RequestUpdateCache)
  }

  override def receive: Receive = {
    case RequestUpdateRss =>
      habrParserActor ! HabrParserActor.RequestRss
    case RequestUpdateCache =>
      chooseArticleForUpdate().foreach { article =>
        habrParserActor ! HabrParserActor.RequestHtml(article.link)
      }
    case HabrParserActor.ParsedHtml(article) =>
      self ! article
    case HabrParserActor.ParsedRss(habraPosts) =>
      habraPosts.foreach(self ! _)
    case article: HabrArticle =>
      cachedArticles.get(article.id) match {
        case None => add(article)
        case Some(oldArticle) => if (article != oldArticle) {
          update(oldArticle |+| article)
        }
      }
    case Subscribe(subscriber, receiveNewPosts, receiveUpdates, receiveExistingPosts) =>
      if (receiveNewPosts) {
        newPostSubscribers += subscriber
      }

      if (receiveUpdates) {
        updatedPostSubscribers += subscriber
      }

      if (receiveExistingPosts) {
        cachedArticles.values.foreach(post => subscriber ! PostAdded(post))
      }
  }

  private def add(article: HabrArticle): Unit = {
    cachedArticles(article.id) = article
    queuedForUpdate.enqueue(article)
    val msg = PostAdded(article)
    newPostSubscribers.foreach(_ ! msg)
  }

  private def update(article: HabrArticle): Unit = {
    cachedArticles(article.id) = article
    val msg = PostUpdated(article)
    updatedPostSubscribers.foreach(_ ! msg)
  }

  private def chooseArticleForUpdate(): Option[HabrArticle] = {
    if (queuedForUpdate.isEmpty) {
      cachedArticles.values.foreach(queuedForUpdate.enqueue)
    }
    queuedForUpdate.dequeue()
  }
}
