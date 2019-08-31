package com.github.awant.habrareader

import akka.actor.ActorSystem
import com.github.awant.habrareader.utils.ConfigLoader
import com.github.awant.habrareader.actors.{LibraryActor, ShopActor, TgBotActor}
import com.github.awant.habrareader.models.ChatData
import com.github.awant.habrareader.mongodb.Mongo
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext
import pureconfig.generic.auto._


object HabraReader extends App {
  val botConfig: BotConfig = ConfigLoader.getConfig[BotConfig]("bot")(defaultBotConfigPath, localBotConfigPath)
  if (botConfig.token.isEmpty) throw new RuntimeException("Empty bot token")

  val akkaConfig: Config = ConfigLoader.getConfig(akkaConfigPath)
  val actorSystem = ActorSystem("system", akkaConfig)

  implicit val ec: ExecutionContext = actorSystem.dispatcher

  val libraryActor = actorSystem.actorOf(LibraryActor.props(chatsUpdateTime,
    new ChatData(Mongo.chatCollection, Mongo.postCollection, Mongo.eventCollection)), "library")
  val shopActor = actorSystem.actorOf(ShopActor.props(articlesUpdateTime, libraryActor), "shop")
  val tgBotActor = actorSystem.actorOf(TgBotActor.props(botConfig, libraryActor), "tgBot")
}
