package com.github.awant.habrareader.akka

import akka.actor.{Actor, ActorLogging, Props}
import akka.event.LoggingReceive
import akka.pattern.pipe
import com.github.awant.habrareader.habr.{HabrArticle, HabrParser}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object HabrParserActor {
  def props(): Props = Props(new HabrParserActor())

  case object RequestRss

  case class ParsedRss(articles: Seq[HabrArticle])

  case class RequestHtml(link: String)

  case class ParsedHtml(article: HabrArticle)

}

/**
  * actor receives RequestRss message, gets it and sends back parsed result
  */
class HabrParserActor private() extends Actor with ActorLogging {

  import HabrParserActor._

  override def receive = LoggingReceive {
    case RequestRss =>
      processAndSendBack {
        ParsedRss(HabrParser.loadPosts("https://habr.com/ru/rss/all/all/"))
      }
    case RequestHtml(link) =>
      processAndSendBack {
        ParsedHtml(HabrParser.loadHtml(link))
      }
  }

  private def processAndSendBack[T](body: => T) {
    val future = Future(body)
    future pipeTo sender
    future.failed.foreach { ex =>
      log.error(s"$ex")
      ex.printStackTrace()
    }
  }
}
