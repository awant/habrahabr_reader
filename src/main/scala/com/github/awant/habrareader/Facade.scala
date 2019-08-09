package com.github.awant.habrareader

import java.net.Proxy
import cats.instances.future._
import cats.syntax.functor._
import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.clients.FutureSttpClient
import com.bot4s.telegram.future.{Polling, TelegramBot}
import slogging.{LogLevel, LoggerConfig, PrintLoggerFactory}
import scala.concurrent.Future

class Facade(val token: String, proxy: Proxy = Proxy.NO_PROXY) extends TelegramBot with Polling with Commands[Future] {
  LoggerConfig.factory = PrintLoggerFactory()
  LoggerConfig.level = LogLevel.TRACE

  implicit val backend = SttpBackends.default
  override val client = new ScalajHttpClient(token, proxy)

  onCommand("new_articles") { implicit msg =>
    reply("Take these articles").void
  }

  onMessage { implicit msg =>
    reply("echo: " + msg.text).void
  }

}
