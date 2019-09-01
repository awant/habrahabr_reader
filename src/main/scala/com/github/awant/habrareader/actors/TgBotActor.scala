package com.github.awant.habrareader.actors

import java.net.{InetSocketAddress, Proxy}

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.clients.ScalajHttpClient
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.bot4s.telegram.methods.SendMessage
import com.github.awant.habrareader.BotConfig
import com.github.awant.habrareader.models.Post
import slogging.{LogLevel, LoggerConfig, PrintLoggerFactory}

import scala.concurrent.{ExecutionContext, Future}


object TgBotActor {
  def props(botConfig: BotConfig, library: ActorRef) = Props(new TgBotActor(botConfig, library))

  final case class Subscription(chatId: Long, set: Boolean)
  final case class Settings(chatId: Long)
  final case class SettingsUpd(chatId: Long, text: String)
  final case class Reply(chatId: Long, msg: String)
  final case class PostReply(chatId: Long, post: Post)
}

class TgBotActor private(botConfig: BotConfig, library: ActorRef) extends Actor with ActorLogging {
  import TgBotActor._

  import ExecutionContext.Implicits.global

  private val bot = {
    val bot = TgBot(botConfig)

    bot.onCommand(_.cmd.equals("subscribe")) { msg =>
      Future { self ! Subscription(msg.chat.id, set=true) }
    }

    bot.onCommand(_.cmd.equals("unsubscribe")) { msg =>
      Future { self ! Subscription(msg.chat.id, set=false) }
    }

    bot.onCommand(_.cmd.equals("settings")) { msg =>
      Future { self ! Settings(msg.chat.id) }
    }

    bot.onCommand(_.cmd.startsWith("set")) { msg =>
      Future { self ! SettingsUpd(msg.chat.id, msg.text.get) }
    }
    bot
  }

  override def preStart(): Unit = {
    library ! LibraryActor.BotSubscription(self)
    bot.run()
  }

  private def formMessage(post: Post): String = {
    s"""author: *${post.author}*
         |up votes: *${post.upVotes}*
         |down votes: *${post.downVotes}*
         |*${post.viewsCount}* views, *${post.bookmarksCount}* bookmarks, *${post.commentsCount}* comments
         |${post.link}
      """.stripMargin
  }

  override def receive: Receive = {
    case Subscription(chatId, set) => library ! LibraryActor.SubscriptionChanging(chatId, set)
    case Settings(chatId) => library ! LibraryActor.SettingsGetting(chatId)
    case SettingsUpd(chatId, body) => library ! LibraryActor.SettingsChanging(chatId, body)
    case Reply(chatId, msg) => bot.request(SendMessage(chatId, msg))
        case PostReply(chatId, post) => bot.request(SendMessage(chatId, formMessage(post)))
  }
}

object TgBot {
  LoggerConfig.factory = PrintLoggerFactory()
  LoggerConfig.level = LogLevel.TRACE

  def apply(botConfig: BotConfig)(implicit ec: ExecutionContext): TgBot = {
    val proxy = if (botConfig.proxy.ip.isEmpty) Proxy.NO_PROXY else
      new Proxy(Proxy.Type.SOCKS, InetSocketAddress.createUnresolved(botConfig.proxy.ip, botConfig.proxy.port))
    new TgBot(new ScalajHttpClient(botConfig.token, proxy))
  }
}

class TgBot(override val client: RequestHandler[Future]) extends TelegramBot with Polling with Commands[Future]
