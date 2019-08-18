package com.github.awant.habrareader.models

import io.circe.syntax._
import io.circe._

case class Post(id: Long,
                link: String,
                title: String,
                description: String,
                author: String)

object Post {
  implicit val encoder: Encoder[Post] = (post: Post) => {
    Json.obj(
      "id" -> post.id.asJson,
      "link" -> post.link.asJson,
      "title" -> post.title.asJson,
      "description" -> post.description.asJson,
      "author" -> post.author.asJson
    )
  }

  implicit val decoder: Decoder[Post] = (c: HCursor) => {
    for {
      id <- c.downField("id").as[Long]
      link <- c.downField("link").as[String]
      title <- c.downField("title").as[String]
      description <- c.downField("description").as[String]
      author <- c.downField("author").as[String]
    } yield Post(id, link, title, description, author)
  }
}
