package com.github.awant.habrareader

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}

object HelloAkka extends App {

	val system = ActorSystem("system")
	val mainActor = system.actorOf(Props(new MainActor), "mainActor")

	system.getWhenTerminated
}


class MainActor extends Actor with ActorLogging {

	case object Start

	case object Stop

	override def preStart(): Unit = {
		self ! Start
	}

	override def receive: Receive = {
		case Start =>
			log.debug("start")
			println("start")
			self ! Stop
		case Stop =>
			log.debug("stop!")
			println("stop!")
			context.system.terminate()
	}
}
