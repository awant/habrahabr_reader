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
  final case class MongoConfig(uri: String, database: String)

  def apply(): AppConfig = config

  def asUntyped: Config = untyped

  private lazy val untyped: Config = {
    val configsNames: Seq[String] =
      sys.env.get("HABRA_READER_CONFIG").map(_.split(",")).getOrElse(
        Array(
          "botLocal.conf",
          "mongoLocal.conf",
          "app.conf"
        ))

    configsNames.map(ConfigFactory.load).reduce(_.withFallback(_))
  }

  private lazy val config: AppConfig = {
    val loaded = pureconfig.loadConfig[AppConfig](untyped)
    println(s"loaded config = $loaded")
    loaded.right.get
  }
}