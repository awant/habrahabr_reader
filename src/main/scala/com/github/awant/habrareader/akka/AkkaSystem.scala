package com.github.awant.habrareader.akka

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object AkkaSystem extends App {

	val akkaConfig = ConfigFactory.load("akka.conf")
	val system = ActorSystem("system", akkaConfig)

	val habrParserActor = system.actorOf(Props(new HabrParserActor(HabrParserConfig(10.seconds))), "habrParser")

	system.scheduler.scheduleOnce(10.seconds)(system.terminate())
	system.getWhenTerminated
}
