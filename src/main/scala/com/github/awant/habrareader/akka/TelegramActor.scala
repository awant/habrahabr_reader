package com.github.awant.habrareader.akka

import akka.actor.{Actor, ActorLogging}
import com.github.awant.habrareader.akka.TelegramBotActor.SendMessage

object TelegramBotActor {

	case class SendMessage(msg: String, recipient: String)

}

case class TelegramBotConfig(token: String)

class TelegramBotActor(val config: TelegramBotConfig) extends Actor with ActorLogging {

	override def receive: Receive = {
		case SendMessage(msg, recipient) =>
			???
	}

}
