package com.github.awant

package object habrareader {
  val akkaConfigPath: String = "akka.conf"

  val defaultBotConfigPath: String = "bot.conf"
  val localBotConfigPath: String = "botLocal.conf"

  val defaultMongoConfigPath: String = "application.conf"
  val localMongoConfigPath: String = "applicationLocal.conf"

  final case class ProxyConfig(ip: String, port: Int)
  final case class BotConfig(isOnServer: Boolean, token: String, proxy: ProxyConfig)
}
