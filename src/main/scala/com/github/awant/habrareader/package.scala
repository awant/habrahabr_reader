package com.github.awant

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

package object habrareader {
  val akkaConfigPath: String = "akka.conf"

  val defaultBotConfigPath: String = "bot.conf"
  val localBotConfigPath: String = "botLocal.conf"

  val defaultMongoConfigPath: String = "application.conf"
  val localMongoConfigPath: String = "applicationLocal.conf"

  val articlesUpdateTime: FiniteDuration = 5.minutes
  val chatsUpdateTime: FiniteDuration = 10.seconds

  final case class ProxyConfig(ip: String, port: Int)
  final case class BotConfig(isOnServer: Boolean, token: String, proxy: ProxyConfig)
}
