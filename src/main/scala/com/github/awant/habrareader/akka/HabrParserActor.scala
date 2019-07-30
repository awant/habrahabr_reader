package com.github.awant.habrareader.akka

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Future
import akka.actor.{Actor, ActorLogging}
import akka.event.LoggingReceive
import akka.pattern.pipe
import com.github.awant.habrareader.{HabraPost, RssParser}

case class HabrParserConfig(updateDuration: FiniteDuration)

class HabrParserActor(config: HabrParserConfig) extends Actor with ActorLogging {

  private case object WantUpdate

  private case class AddArticles(articles: Seq[HabraPost])

  override def preStart(): Unit = {
    self ! WantUpdate
  }

  override def receive = LoggingReceive {
    case WantUpdate =>
      log.debug("WantUpdate received!")
      val future = checkHabrForUpdates()
      future pipeTo self
      future.failed.foreach(ex => log.error(s"$ex"))
      scheduleUpdate()
    case AddArticles(articles) =>
      ??? // todo
  }

  private def checkHabrForUpdates(): Future[AddArticles] =
    Future {
      AddArticles {
        RssParser.loadPosts("https://habr.com/ru/rss/all/all/")
      }
    }

  private def scheduleUpdate(): Unit =
    context.system.scheduler.scheduleOnce(config.updateDuration, self, WantUpdate)

}
