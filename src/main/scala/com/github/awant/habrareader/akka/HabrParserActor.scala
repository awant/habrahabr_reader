package com.github.awant.habrareader.akka

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.Future
import akka.actor.{Actor, ActorLogging}
import akka.event.LoggingReceive
import akka.pattern.pipe

case class HabrParserConfig(updateDuration: FiniteDuration)

class HabrParserActor(config: HabrParserConfig) extends Actor with ActorLogging {

	private type HabrArticle = Object // todo make real type

	private case object WantUpdate

	private case class ArticleAdded(t: HabrArticle)

	private case class ArticleUpdated(t: HabrArticle)

	override def preStart(): Unit = {
		self ! WantUpdate
	}

	override def receive = LoggingReceive {
		case WantUpdate =>
			log.debug("WantUpdate received!")
			val future = checkHabrForUpdates()
			future.map(article => ArticleAdded(article)) pipeTo self
			future.failed.foreach(ex => log.error(s"$ex"))
			scheduleUpdate()
		case ArticleAdded(article) =>
			??? // todo
		case ArticleUpdated(article) =>
			??? // todo
	}

	private def checkHabrForUpdates(): Future[HabrArticle] =
		Future {
			Thread.sleep(2000)
			throw new RuntimeException("habr checking isn't implemented yet")
		}

	private def scheduleUpdate(): Unit =
		context.system.scheduler.scheduleOnce(config.updateDuration, self, WantUpdate)

}
