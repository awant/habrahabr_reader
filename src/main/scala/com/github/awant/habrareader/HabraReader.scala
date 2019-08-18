package com.github.awant.habrareader

import akka.actor.ActorSystem
import com.github.awant.habrareader.actors.{ShopActor, LibraryActor, TgBotActor}
import com.github.awant.habrareader.models.ChatData
import com.github.awant.habrareader.mongodb.Mongo
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import pureconfig.generic.auto._


object HabraReader extends App {
  val defaultBotConfigPath: String = "bot.conf"
  val localBotConfigPath: String = "botLocal.conf"
  val akkaConfigPath: String = "akka.conf"

  final case class ProxyConfig(ip: String, port: Int)
  final case class BotConfig(isOnServer: Boolean, token: String, proxy: ProxyConfig)

  val botConfig: BotConfig = {
    val config = {
      val conf = ConfigFactory.load(defaultBotConfigPath)

      Option(localBotConfigPath)
        .filter(isResourceExists)
        .map(name => ConfigFactory.load(name).withFallback(conf))
        .getOrElse(conf)
    }

    pureconfig.loadConfig[BotConfig](config.getConfig("bot"))
  }.right.get

  if (botConfig.token.isEmpty) throw new RuntimeException("Empty bot token")

  val akkaConfig = ConfigFactory.load(akkaConfigPath)
  val actorSystem = ActorSystem("system", akkaConfig)

  implicit val ec: ExecutionContext = actorSystem.dispatcher

  val libraryActor = actorSystem.actorOf(LibraryActor.props(10.second,
    new ChatData(Mongo.chatCollection, Mongo.postCollection, Mongo.eventCollection)), "library")
  val shopActor = actorSystem.actorOf(ShopActor.props(5.minute, libraryActor), "shop")
  val tgBotActor = actorSystem.actorOf(TgBotActor.props(botConfig, libraryActor), "tgBot")

  private def isResourceExists(name: String): Boolean = getClass.getClassLoader.getResource(name) != null
}
