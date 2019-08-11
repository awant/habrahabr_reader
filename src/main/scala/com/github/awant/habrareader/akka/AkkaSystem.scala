package com.github.awant.habrareader.akka

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import pureconfig.generic.auto._

object AkkaSystem extends App {

  case class BotConfig(isOnServer: Boolean, token: String)

  val botConfig: BotConfig = {
    val config = {
      val conf = ConfigFactory.load("bot.conf")

      Option("botLocal.conf")
        .filter(isResourceExists)
        .map(name => ConfigFactory.load(name).withFallback(conf))
        .getOrElse(conf)
    }

    pureconfig.loadConfig[BotConfig](config.getConfig("bot"))
  }.right.get

  assert(botConfig.token.nonEmpty)

  val akkaConfig = ConfigFactory.load("akka.conf")
  val system = ActorSystem("system", akkaConfig)

  val habrParserActor = system.actorOf(HabrParserActor.props(), "habrParser")
  val habrArticlesCache = system.actorOf(HabrArticlesCache.props(1.minute, 10.seconds, habrParserActor), "habrArticlesCache")

  val tgHandlerActor = system.actorOf(TgHandlerActor.props(botConfig, habrArticlesCache))

  private def isResourceExists(name: String): Boolean =
    getClass.getClassLoader.getResource(name) != null
}
