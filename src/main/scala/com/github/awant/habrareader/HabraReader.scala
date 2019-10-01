package com.github.awant.habrareader

import akka.actor.ActorSystem
import com.github.awant.habrareader.actors.{LibraryActor, ShopActor, TgBotActor}
import com.github.awant.habrareader.models.ChatData
import com.github.awant.habrareader.mongodb.Mongo

import scala.concurrent.ExecutionContext


object HabraReader extends App {
  assert(AppConfig().tgbot.token.nonEmpty, "Empty bot token")

  val actorSystem = ActorSystem("system", AppConfig.asUntyped)

  implicit val ec: ExecutionContext = actorSystem.dispatcher

  val mongo = new Mongo(AppConfig().mongo)

  val libraryActor = actorSystem.actorOf(LibraryActor.props(AppConfig().library,
    new ChatData(mongo.chatCollection, mongo.postCollection, mongo.eventCollection)), "library")
  val shopActor = actorSystem.actorOf(ShopActor.props(AppConfig().shop, libraryActor), "shop")
  val tgBotActor = actorSystem.actorOf(TgBotActor.props(AppConfig().tgbot, libraryActor), "tgBot")
}
