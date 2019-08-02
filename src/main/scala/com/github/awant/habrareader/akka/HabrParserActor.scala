package com.github.awant.habrareader.akka

import akka.actor.{Actor, ActorLogging, Props}
import akka.event.LoggingReceive
import akka.pattern.pipe
import com.github.awant.habrareader.{HabrArticle, RssParser}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object HabrParserActor {
  def props(): Props = Props(new HabrParserActor())

  case object RequestRss

  case class ParsedRss(articles: Seq[HabrArticle])

}

/**
  * actor receives RequestRss message, gets it and sends back parsed result
  */
class HabrParserActor private() extends Actor with ActorLogging {

  import HabrParserActor._

  override def receive = LoggingReceive {
    case RequestRss =>
      val future = Future {
        ParsedRss(RssParser.loadPosts("https://habr.com/ru/rss/all/all/"))
      }
      future pipeTo sender
      future.failed.foreach(ex => log.error(s"$ex"))
  }
}
