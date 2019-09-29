package com.github.awant.habrareader.models

import java.util.Date

import com.github.awant.habrareader.utils.DateUtils
import com.github.awant.habrareader.utils.DateUtils._
import io.circe._
import io.circe.syntax._
import org.bson.codecs.{Codec, DecoderContext, EncoderContext}
import org.bson.{BsonReader, BsonWriter}


case class ChatScope(chatScopeType: String)

object ChatScope {
  val all = ChatScope("all")
  val none = ChatScope("")

  def fromString(scope: String): ChatScope =
    scope match {
      case  ChatScope.all.chatScopeType => ChatScope.all
      case ChatScope.none.chatScopeType => ChatScope.none
      case _ => throw new IllegalArgumentException("illegal scope argument")
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
                authorScope: ChatScope, author: Seq[String], excludedAuthor: Seq[String],
                categoryScope: ChatScope, category: Seq[String], excludedCategory: Seq[String]) {

  private def formScope(scope: ChatScope, values: Seq[String], excludedValues: Seq[String]): String = scope match {
    case ChatScope.all => scope.chatScopeType + (if (excludedValues.nonEmpty) ", except: " + excludedValues.mkString(", ") else "")
    case ChatScope.none => scope.chatScopeType + values.mkString(", ")
    case _ => ""
  }
  private def formAuthorsScope: String = formScope(authorScope, author, excludedAuthor)
  private def formCategoriesScope: String = formScope(categoryScope, category, excludedCategory)

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
    authorScope = ChatScope.all,
    author = Seq[String](),
    excludedAuthor = Seq[String](),
    categoryScope = ChatScope.all,
    category = Seq[String](),
    excludedCategory = Seq[String]()
  )

  def withEmptySettings(id: Long) = Chat(id, DateUtils.currentDate,
    subscription = false,
    authorScope = ChatScope.none,
    author = Seq[String](),
    excludedAuthor = Seq[String](),
    categoryScope = ChatScope.none,
    category = Seq[String](),
    excludedCategory = Seq[String]()
  )

  implicit val encoder: Encoder[Chat] = (chat: Chat) => {
    Json.obj(
      "id" := chat.id,
      "lastUpdateDate" := chat.lastUpdateDate,
      "subscription" := chat.subscription,

      "authorScope" := chat.authorScope.chatScopeType,
      "author" := chat.author,
      "excludedAuthor" := chat.excludedAuthor,

      "categoryScope" := chat.categoryScope.chatScopeType,
      "category" := chat.category,
      "excludedCategory" := chat.excludedCategory,
    )
  }

  implicit val decoder: Decoder[Chat] = (c: HCursor) => {
    for {
      id <- c.downField("id").as[Long]
      lastUpdateDate <- c.downField("lastUpdateDate").as[String]
      subscription <- c.downField("subscription").as[Boolean]

      authorsScope <- c.downField("authorScope").as[String]
      authors <- c.downField("author").as[Seq[String]]
      excludedAuthors <- c.downField("excludedAuthors").as[Seq[String]]

      categoryScope <- c.downField("categoryScope").as[String]
      categories <- c.downField("category").as[Seq[String]]
      excludedCategories <- c.downField("excludedCategory").as[Seq[String]]

    } yield Chat(
      id, DateUtils.convertToDate(lastUpdateDate), subscription,
      ChatScope.fromString(authorsScope), authors, excludedAuthors,
      ChatScope.fromString(categoryScope), categories, excludedCategories)
  }
}
