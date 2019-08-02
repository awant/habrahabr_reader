package com.github.awant.habrareader.akka

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

object AkkaSystem extends App {

  val akkaConfig = ConfigFactory.load("akka.conf")
  val system = ActorSystem("system", akkaConfig)

  val habrParserActor = system.actorOf(HabrParserActor.props(), "habrParser")
  val habrArticlesCache = system.actorOf(HabrArticlesCache.props(1.minute, habrParserActor), "habrArticlesCache")
  val naiveSubscriber = system.actorOf(NaiveSubscriber.props(habrArticlesCache), "naiveSubscriber")
}
