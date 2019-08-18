package com.github.awant.habrareader.actors

import akka.actor.{Actor, ActorRef, Props}
import com.bot4s.telegram.methods.{ParseMode, SendMessage}
import com.bot4s.telegram.models.{Chat, Message}
import com.github.awant.habrareader.habr.HabrArticle
import com.github.awant.habrareader.utils.ParsedCommand

import scala.collection.mutable
import scala.util.Try

@deprecated("legacy", "")
object TgChatActor {
  def props(chat: Chat, tgHandler: ActorRef) = Props(new TgChatActor(chat, tgHandler))
}

@deprecated("legacy", "")
class TgChatActor private(chat: Chat, tgHandler: ActorRef) extends Actor {

  private object preferences {
    // authors in lowercase
    val favoriteAuthors = new mutable.HashSet[String]()
    val bannedAuthors = new mutable.HashSet[String]()
    var ratingThreshold: Int = _
    var positiveByNegativeRatioThreshold: Double = _

    def reset(): Unit = {
      favoriteAuthors.clear()
      bannedAuthors.clear()
      ratingThreshold = 10
      positiveByNegativeRatioThreshold = 2.0
    }

    reset()
  }

  val alreadyPublished = new mutable.HashSet[HabrArticle.Id]()

  override def receive: Receive = {
    case HabrArticlesCache.PostAdded(post) => process(post)

    case HabrArticlesCache.PostUpdated(post) => process(post)

    case msg: Message => handleUserInput(msg)
  }

  private def process(article: HabrArticle): Unit = {
    if (alreadyPublished.contains(article.id)) {
      return
    }

    if (articleIsOk(article)) {
      alreadyPublished += article.id
      showArticle(article)
    }
  }

  private def articleIsOk(article: HabrArticle): Boolean = {
    if (preferences.favoriteAuthors.contains(article.author.toLowerCase)) {
      return true
    }

    if (preferences.bannedAuthors.contains(article.author.toLowerCase())) {
      return false
    }

    article.rating.exists { rating =>
      rating.totalVotes >= preferences.ratingThreshold &&
        rating.upVotes > rating.downVotes * preferences.positiveByNegativeRatioThreshold
    }
  }

  private def showArticle(article: HabrArticle): Unit = {
    val r = article.rating.get
    tgHandler ! textMessage(
      s"""author: *${article.author}*
         |rating: *${r.totalVotes}* = *${r.upVotes}* - *${r.downVotes}*
         |*${r.viewsCount}* views, *${r.bookmarksCount}* bookmarks, *${r.commentsCount}* comments
         |tags: ${article.categories.map(_.filter(c => c != '.' && c != ' ')).map(t => s"#$t").mkString("{", ", ", "}")}
         |${article.link}
      """.stripMargin)
  }

  private def handleUserInput(msg: Message): Unit = {
    msg.text.map(_.toLowerCase).foreach { text =>
      if (Set("start", "help").flatMap(s => Set(s, "/" + s)) contains text.trim) {
        tgHandler ! textMessage(
          s"""Hello, I'm bot for filtering habr articles
             |${getPreferences()}
             |${possibleCommands()}
          """.stripMargin)
        return
      }
      text match {
        case "/settings" =>
          tgHandler ! textMessage(getPreferences())

        case "/reset" =>
          preferences.reset()
          tgHandler ! textMessage(s"preferences was reset to default:\n${getPreferences()}")

        case "/about" =>
          tgHandler ! textMessage("email: habrahabrreader@gmail.com")
        // pswd = scalaforever

        case ParsedCommand("/ban", name) =>
          preferences.bannedAuthors.add(name.trim)
          preferences.favoriteAuthors.remove(name.trim)

        case ParsedCommand("/unban", name) =>
          removeOrSendText(name.trim, preferences.bannedAuthors)

        case ParsedCommand("/like", name) =>
          preferences.favoriteAuthors.add(name.trim)
          preferences.bannedAuthors.remove(name.trim)

        case ParsedCommand("/dislike", name) =>
          removeOrSendText(name.trim, preferences.favoriteAuthors)

        case ParsedCommand("/setrating", value) =>
          Try {
            preferences.ratingThreshold = value.toInt
          }.failed.foreach { ex =>
            tgHandler ! textMessage("should be an int!")
          }

        case ParsedCommand("/setratio", value) =>
          Try {
            preferences.positiveByNegativeRatioThreshold = value.toDouble
          }.failed.foreach { ex =>
            tgHandler ! textMessage("should be a double!")
          }

        case _ =>
          tgHandler ! textMessage(
            s"""unknown command!
               |${possibleCommands()}
            """.stripMargin)
      }
    }
  }

  private def removeOrSendText(author: String, set: mutable.Set[String]): Unit =
    if (!set.contains(author)) {
      tgHandler ! textMessage(
        s"""author $author wasn't found
           |${makeList("current list:", set.toSeq.sorted)}""".stripMargin)
    } else {
      set.remove(author)
    }

  private def textMessage(text: String): SendMessage = SendMessage(chat.id, text, parseMode = Option(ParseMode.Markdown))

  private def getPreferences(): String =
    s"""${makeList("favorite authors", preferences.favoriteAuthors.toSeq.sorted)}
       |${makeList("banned authors", preferences.bannedAuthors.toSeq.sorted)}
       |minimal article rating: ${preferences.ratingThreshold}
       |minimal positive/negative ratio: ${f"${preferences.positiveByNegativeRatioThreshold}%1.1f".replace(',', '.')}""".stripMargin

  private def possibleCommands(): String =
    """possible commands:
      |- (/ban, /unban, /like, /dislike) authorName
      |- /setRating 10
      |- /setRatio 2.0
      |- /reset"""

  private def makeList(title: String, points: Seq[String]): String = {
    if (points.isEmpty) s"$title: empty"
    else s"$title:\n${points.map(p => s"- $p").mkString("\n")}"
  }
}
