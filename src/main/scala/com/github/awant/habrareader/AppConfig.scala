package com.github.awant.habrareader

import com.typesafe.config.{Config, ConfigFactory}
import pureconfig.generic.auto._


object AppConfig {
  final case class AppConfig(tgbot: TgBotActorConfig,
                             shop: ShopActorConfig,
                             library: LibraryActorConfig,
                             mongo: MongoConfig)

  final case class ProxyConfig(ip: String, port: Int)
  final case class TgBotActorConfig(token: String, proxy: ProxyConfig)
  final case class ShopActorConfig(articlesUpdateTimeSeconds: Int)
  final case class LibraryActorConfig(chatsUpdateTimeSeconds: Int)
  final case class MongoConfig(uri: String, database: String, writeLogs: Boolean)

  def apply(): AppConfig = config

  def asUntyped: Config = untyped

  private lazy val untyped: Config = {
    val configPath = sys.env.getOrElse(configEnvKey, configDefaultPath)
    ConfigFactory.load(configPath)
  }

  private lazy val config: AppConfig = {
    val loaded = pureconfig.loadConfig[AppConfig](untyped)
    loaded.right.get
  }
}
