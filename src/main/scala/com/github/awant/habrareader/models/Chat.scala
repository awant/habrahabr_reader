package com.github.awant.habrareader.models

import com.github.awant.habrareader.utils.DateUtils
import java.util.Date

import io.circe.syntax._
import io.circe._

import org.bson.{BsonReader, BsonWriter}
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}


sealed class ChatScope {
  def chatScopeType: String = ""
}

case class ChatScopeAll() extends ChatScope { override val chatScopeType: String = "all" }
case class ChatScopeNone() extends ChatScope { override val chatScopeType: String = "" }

object ChatScope {
  def fromString(scope: String): ChatScope = {
    if (scope == ChatScopeAll().chatScopeType) ChatScopeAll()
    else if (scope == ChatScopeNone().chatScopeType) ChatScopeNone()
    else throw new IllegalArgumentException("illegal scope argument")
  }
}

class ChatScopeCodec extends Codec[ChatScope] {
  override def encode(writer: BsonWriter,
                      value: ChatScope,
                      encoderContext: EncoderContext
                     ): Unit = writer.writeString(value.chatScopeType)
  override def getEncoderClass: Class[ChatScope] = classOf[ChatScope]
  override def decode(reader: BsonReader,
                      decoderContext: DecoderContext): ChatScope = {
    val value = reader.readString()
    ChatScope.fromString(value)
  }
}

case class Chat(id: Long, lastUpdateDate: Date, subscription: Boolean,
                authorsScope: ChatScope, authors: Seq[String], excludedAuthors: Seq[String],
                categoryScope: ChatScope, categories: Seq[String], excludedCategories: Seq[String]) {

  private def formScope(scope: ChatScope, values: Seq[String], excludedValues: Seq[String]): String = scope match {
    case ChatScopeAll() => scope.chatScopeType + (if (excludedValues.nonEmpty) ", except: " + excludedValues.mkString(", ") else "")
    case ChatScopeNone() => scope.chatScopeType + values.mkString(", ")
    case _ => ""
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
  def withDefaultSettings(id: Long, subscription: Boolean = true) = Chat(id, DateUtils.currentDate,
    subscription = subscription,
    authorsScope = ChatScopeAll(),
    authors = Seq[String](),
    excludedAuthors = Seq[String](),
    categoryScope = ChatScopeAll(),
    categories = Seq[String](),
    excludedCategories = Seq[String]()
  )

  def withEmptySettings(id: Long) = Chat(id, DateUtils.currentDate,
    subscription = false,
    authorsScope = ChatScopeNone(),
    authors = Seq[String](),
    excludedAuthors = Seq[String](),
    categoryScope = ChatScopeNone(),
    categories = Seq[String](),
    excludedCategories = Seq[String]()
  )

  implicit val encoder: Encoder[Chat] = (chat: Chat) => {
    Json.obj(
      "id" -> chat.id.asJson,
      "lastUpdateDate" -> DateUtils.convertToStr(chat.lastUpdateDate).asJson,
      "subscription" -> chat.subscription.asJson,

      "authorsScope" -> chat.authorsScope.chatScopeType.asJson,
      "authors" -> chat.authors.asJson,
      "excludedAuthors" -> chat.excludedAuthors.asJson,

      "categoryScope" -> chat.categoryScope.chatScopeType.asJson,
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
