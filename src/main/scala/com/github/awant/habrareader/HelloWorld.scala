package com.github.awant.habrareader

import com.typesafe.config.ConfigFactory

import pureconfig.generic.auto._

case class HelloConfig(version: String)

object HelloWorld extends App {

  def helloMessage: String = "Hello world!"

  println(helloMessage)

  //	val cfg: ConfigReader.Result[HelloConfig] = pureconfig.loadConfig[HelloConfig]
  val helloConfig = pureconfig.loadConfig[HelloConfig](ConfigFactory.load().withFallback(ConfigFactory.load("default.conf")))
  println(helloConfig.right.get)

  val posts = RssParser.loadPosts("https://habr.com/ru/rss/all/all/")
  println(posts)
}
