package com.github.awant.habrareader.actors

import java.net.Proxy

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.clients.ScalajHttpClient
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.bot4s.telegram.methods.SendMessage
import com.bot4s.telegram.models.Message
import com.github.awant.habrareader.actors.AkkaSystem.BotConfig
import com.github.awant.habrareader.actors.TgBotActor.MessageReceived
import slogging.{LogLevel, LoggerConfig, PrintLoggerFactory}

import scala.concurrent.{ExecutionContext, Future}

object TgBotActor {

  def props(botConfig: BotConfig, msgHandler: ActorRef) =
    Props(new TgBotActor(botConfig, msgHandler))

  type SendMessage = com.bot4s.telegram.methods.SendMessage

  final case class MessageReceived(msg: Message)

}


class TgBotActor private(botConfig: BotConfig, msgHandler: ActorRef) extends Actor with ActorLogging {

  import ExecutionContext.Implicits.global

  private lazy val bot = {
    val bot = TgBot(botConfig)
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
    case m: MessageReceived =>
      msgHandler ! m
    case msg: SendMessage =>
      bot.request(msg)
  }
}

object TgBot {
  LoggerConfig.factory = PrintLoggerFactory()
  LoggerConfig.level = LogLevel.TRACE

  def apply(botConfig: BotConfig)(implicit ec: ExecutionContext): TgBot =
    new TgBot(new ScalajHttpClient(botConfig.token, Proxy.NO_PROXY))
}

class TgBot(override val client: RequestHandler[Future]) extends TelegramBot with Polling with Commands[Future]
