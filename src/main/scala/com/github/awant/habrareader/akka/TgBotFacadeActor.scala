package com.github.awant.habrareader.akka

import java.net.Proxy

import akka.actor.{Actor, ActorLogging, Props}
import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.clients.ScalajHttpClient
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.bot4s.telegram.methods.SendMessage
import com.bot4s.telegram.models.Message
import com.github.awant.habrareader.akka.TgBotFacadeActor.MessageReceived
import slogging.{LogLevel, LoggerConfig, PrintLoggerFactory}

import scala.concurrent.{ExecutionContext, Future}

object TgBotFacadeActor {

  def props(token: String, proxy: Proxy = Proxy.NO_PROXY) =
    Props(new TgBotFacadeActor(token, proxy))

  type SendMessage = com.bot4s.telegram.methods.SendMessage

  private case class MessageReceived(msg: Message)

}


class TgBotFacadeActor private(token: String, proxy: Proxy) extends Actor with ActorLogging {

  import ExecutionContext.Implicits.global

  private lazy val bot = {
    val bot = TgBot(token, proxy)
    bot.onMessage { msg =>
      Future {
        self ! MessageReceived(msg)
      }
    }
    bot
  }

  override def preStart(): Unit = {
    bot.run()
  }

  override def receive: Receive = {
    case MessageReceived(msg) =>
      log.debug(s"message received: $msg")
      if (msg.text.contains("/start")) {
        self ! SendMessage(msg.source, text = "Hi, I'm bot for filtering articles from habr.com")
      } else {
        self ! SendMessage(msg.source, text = "msg1")
        self ! SendMessage(msg.source, text = "msg2")
      }
    case msg: SendMessage =>
      bot.request(msg)
  }
}

object TgBot {
  LoggerConfig.factory = PrintLoggerFactory()
  LoggerConfig.level = LogLevel.TRACE

  def apply(token: String, proxy: Proxy = Proxy.NO_PROXY)(implicit ec: ExecutionContext = ExecutionContext.Implicits.global): TgBot =
    new TgBot(new ScalajHttpClient(token, proxy))
}

class TgBot(override val client: RequestHandler[Future]) extends TelegramBot with Polling with Commands[Future] {
}
