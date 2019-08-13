package com.github.awant.habrareader.actors

import akka.actor.ActorSystem
import com.github.awant.habrareader.ChatDataActor
import com.typesafe.config.ConfigFactory
import com.github.awant.habrareader.models.ChatData
import com.github.awant.habrareader.mongodb.Mongo

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import pureconfig.generic.auto._


object AkkaSystem extends App {
  val defaultBotConfigPath: String = "bot.conf"
  val localBotConfigPath: String = "botLocal.conf"
  val akkaConfigPath: String = "akka.conf"

  final case class BotConfig(isOnServer: Boolean, token: String)

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
  val system = ActorSystem("system", akkaConfig)

  implicit val ec: ExecutionContext = system.dispatcher

  val habrParserActor = system.actorOf(HabrParserActor.props(), "habrParser")
  val habrArticlesCache = system.actorOf(HabrArticlesCache.props(1.minute, 10.seconds, habrParserActor), "habrArticlesCache")
  val chatActor = system.actorOf(ChatDataActor.props(new ChatData(Mongo.chatCollection)))
  val tgHandlerActor = system.actorOf(TgHandlerActor.props(botConfig, habrArticlesCache, chatActor))

  private def isResourceExists(name: String): Boolean = getClass.getClassLoader.getResource(name) != null
}
