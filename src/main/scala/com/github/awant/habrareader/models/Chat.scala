package com.github.awant.habrareader.models

import com.github.awant.habrareader.utils.DateUtils
import java.util.Date

import com.github.awant.habrareader.models.ChatScope.ChatScope
import io.circe.syntax._
import io.circe._


object ChatScope extends Enumeration {
  type ChatScope = Value
  val ALL: Value = Value("all")
  val NONE: Value = Value("")

  def fromString(scope: String): ChatScope = {
    values.find(_.toString == scope).getOrElse(ALL)
  }
}

case class Chat(id: Long, lastUpdateDate: Date, subscription: Boolean,
                authorsScope: ChatScope, authors: Seq[String], excludedAuthors: Seq[String],
                categoryScope: ChatScope, categories: Seq[String], excludedCategories: Seq[String]) {

  private def formScope(scope: ChatScope, values: Seq[String], excludedValues: Seq[String]): String = scope match {
    case ChatScope.ALL => scope.toString + (if (excludedValues.nonEmpty) ", except: " + excludedValues.mkString(", ") else "")
    case ChatScope.NONE => scope.toString + values.mkString(", ")
  }
  private def formAuthorsScope: String = formScope(authorsScope, authors, excludedAuthors)
  private def formCategoriesScope: String = formScope(categoryScope, categories, excludedCategories)

  def getSettingsPrettify: String = {
    s"""subscription: $subscription
      |authors: $formAuthorsScope
      |categories: $formCategoriesScope
    """.stripMargin
  }
}

object Chat {
  def withDefaultSettings(id: Long) = Chat(id, DateUtils.currentDate,
    subscription = true,
    authorsScope = ChatScope.ALL,
    authors = Seq.empty,
    excludedAuthors = Seq.empty,
    categoryScope = ChatScope.ALL,
    categories = Seq.empty,
    excludedCategories = Seq.empty
  )

  def withEmptySettings(id: Long) = Chat(id, DateUtils.currentDate,
    subscription = false,
    authorsScope = ChatScope.NONE,
    authors = Seq.empty,
    excludedAuthors = Seq.empty,
    categoryScope = ChatScope.NONE,
    categories = Seq.empty,
    excludedCategories = Seq.empty
  )

  implicit val encoder: Encoder[Chat] = (chat: Chat) => {
    Json.obj(
      "id" -> chat.id.asJson,
      "lastUpdateDate" -> DateUtils.convertToStr(chat.lastUpdateDate).asJson,
      "subscription" -> chat.subscription.asJson,

      "authorsScope" -> chat.authorsScope.toString.asJson,
      "authors" -> chat.authors.asJson,
      "excludedAuthors" -> chat.excludedAuthors.asJson,

      "categoryScope" -> chat.categoryScope.toString.asJson,
      "categories" -> chat.categories.asJson,
      "excludedCategories" -> chat.excludedCategories.asJson,
    )
  }

  implicit val decoder: Decoder[Chat] = (c: HCursor) => {
    for {
      id <- c.downField("id").as[Long]
      lastUpdateDate <- c.downField("lastUpdateDate").as[String]
      subscription <- c.downField("subscription").as[Boolean]

      authorsScope <- c.downField("authorsScope").as[String]
      authors <- c.downField("authors").as[Seq[String]]
      excludedAuthors <- c.downField("excludedAuthors").as[Seq[String]]

      categoryScope <- c.downField("categoryScope").as[String]
      categories <- c.downField("categories").as[Seq[String]]
      excludedCategories <- c.downField("excludedCategories").as[Seq[String]]

    } yield Chat(
      id, DateUtils.convertToDate(lastUpdateDate), subscription,
      ChatScope.fromString(authorsScope), authors, excludedAuthors,
      ChatScope.fromString(categoryScope), categories, excludedCategories)
  }
}
