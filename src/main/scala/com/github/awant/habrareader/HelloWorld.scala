package com.github.awant.habrareader

import com.github.awant.habrareader.habr.HabrParser
import com.typesafe.config.ConfigFactory
import pureconfig.generic.auto._

case class HelloConfig(version: String)

object HelloWorld extends App {
  //	val cfg: ConfigReader.Result[HelloConfig] = pureconfig.loadConfig[HelloConfig]
  val helloConfig = pureconfig.loadConfig[HelloConfig](ConfigFactory.load().withFallback(ConfigFactory.load("default.conf")))
  println(helloConfig.right.get)

  val posts = HabrParser.loadPosts("https://habr.com/ru/rss/all/all/")
  println(posts)
}
