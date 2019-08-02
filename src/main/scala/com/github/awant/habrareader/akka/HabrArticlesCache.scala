package com.github.awant.habrareader.akka

import akka.actor.{Actor, ActorRef, Props}
import com.github.awant.habrareader.HabrArticle

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object HabrArticlesCache {
  def props(updateTime: FiniteDuration, habraParser: ActorRef): Props =
    Props(new HabrArticlesCache(updateTime, habraParser))

  case class Subscribe(subscriber: ActorRef, recieveNew: Boolean, receiveUpdates: Boolean, receiveExisting: Boolean)

  case class PostUpdated(article: HabrArticle)

  case class PostAdded(article: HabrArticle)

  private case object RequestUpdate

}

/**
  * actor requests rss from time to time
  * saves articles
  * notifies actors subscribed to this about updates
  *
  * @param updateTime: time between rss udates
  * @param habrParserActor: actor parsing rss
  */
class HabrArticlesCache private(updateTime: FiniteDuration, habrParserActor: ActorRef) extends Actor {

  import HabrArticlesCache._

  val cachedArticles: mutable.Map[HabrArticle.Id, HabrArticle] = new mutable.HashMap()
  val newPostSubscribers = new mutable.HashSet[ActorRef]()
  val updatedPostSubscribers = new mutable.HashSet[ActorRef]()

  override def preStart(): Unit =
    context.system.scheduler.schedule(0.second, updateTime, self, RequestUpdate)

  override def receive: Receive = {
    case RequestUpdate =>
      habrParserActor ! HabrParserActor.RequestRss
    case HabrParserActor.ParsedRss(habraPosts) =>
      habraPosts.foreach(self ! _)
    case article: HabrArticle =>
      cachedArticles.get(article.id) match {
        case None => add(article)
        case Some(oldArticle) => if (article != oldArticle) {
          update(article)
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
    val msg = PostAdded(article)
    newPostSubscribers.foreach(_ ! msg)
  }

  private def update(article: HabrArticle): Unit = {
    cachedArticles(article.id) = article
    val msg = PostUpdated(article)
    updatedPostSubscribers.foreach(_ ! msg)
  }
}
