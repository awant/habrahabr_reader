package com.github.awant.habrareader.models

import io.circe.syntax._
import io.circe._
import java.util.Date
import com.github.awant.habrareader.utils.DateUtils

case class Post(link: String,
                title: String,
                description: String,
                author: String,
                upVotes: Int,
                downVotes: Int,
                viewsCount: Int,
                commentsCount: Int,
                bookmarksCount: Int,
                updateDate: Date)

object Post {
  implicit val encoder: Encoder[Post] = (post: Post) => {
    Json.obj(
      "link" -> post.link.asJson,
      "title" -> post.title.asJson,
      "description" -> post.description.asJson,
      "author" -> post.author.asJson,
      "upvotes" -> post.upVotes.asJson,
      "downvotes" -> post.downVotes.asJson,
      "views" -> post.viewsCount.asJson,
      "comments" -> post.commentsCount.asJson,
      "bookmarks" -> post.bookmarksCount.asJson,
      "update_date" -> DateUtils.convertToStr(post.updateDate).asJson
    )
  }

  implicit val decoder: Decoder[Post] = (c: HCursor) => {
    for {
      link <- c.downField("link").as[String]
      title <- c.downField("title").as[String]
      description <- c.downField("description").as[String]
      author <- c.downField("author").as[String]
      upVotes <- c.downField("upvotes").as[Int]
      downVotes <- c.downField("downvotes").as[Int]
      viewsCount <- c.downField("views").as[Int]
      commentsCount <- c.downField("comments").as[Int]
      bookmarksCount <- c.downField("bookmarks").as[Int]
      updateDate <- c.downField("update_date").as[String]
    } yield Post(
      link, title, description, author, upVotes, downVotes, viewsCount, commentsCount, bookmarksCount,
      DateUtils.convertToDate(updateDate)
    )
  }

  def getTest = Post("wwww.test.com", "test title", "test description", "test author", 0, 0, 0, 0, 0, DateUtils.currentDate)
}
