package com.github.awant.habrareader.habr

import com.github.awant.habrareader.Facade
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object HabraReader {

  def main(args: Array[String]): Unit = {
    val settings = ConfigFactory.load("settings.conf")
    val tgToken = settings.getString("tg_token")

    val bot = new Facade(tgToken)
    val eol = bot.run()
    println("Press [ENTER] to shutdown the bot")
    scala.io.StdIn.readLine()
    bot.shutdown()
    Await.result(eol, Duration.Inf)
  }

}
