package com.github.awant.habrareader.akka

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

object AkkaSystem extends App {

  val akkaConfig = ConfigFactory.load("akka.conf")
  val system = ActorSystem("system", akkaConfig)

  val habrParserActor = system.actorOf(HabrParserActor.props(), "habrParser")
  val habrArticlesCache = system.actorOf(HabrArticlesCache.props(1.minute, 10.seconds, habrParserActor), "habrArticlesCache")
  val naiveSubscriber = system.actorOf(NaiveSubscriber.props(habrArticlesCache), "naiveSubscriber")

  val bot = system.actorOf(TgBotFacadeActor.props("571623448:AAFZj7IY6IUf1DiAwwbMfQbfQKxAm1w5MzU"))
}
