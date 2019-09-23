package com.github.awant.habrareader.models

import java.util.Date

import com.github.awant.habrareader.utils.DateUtils
import com.github.awant.habrareader.utils.DateUtils._
import io.circe._
import io.circe.syntax._

case class Post(link: String,
                title: String,
                description: String,
                author: String,
                categories: Seq[String],
                upVotes: Int,
                downVotes: Int,
                viewsCount: Int,
                commentsCount: Int,
                bookmarksCount: Int,
                updateDate: Date)

object Post {
  implicit val encoder: Encoder[Post] = (post: Post) => {
    Json.obj(
      "link" := post.link,
      "title" := post.title,
      "description" := post.description,
      "author" := post.author,
      "categories" := post.categories,
      "upvotes" := post.upVotes,
      "downvotes" := post.downVotes,
      "views" := post.viewsCount,
      "comments" := post.commentsCount,
      "bookmarks" := post.bookmarksCount,
      "updateDate" := post.updateDate
    )
  }

  implicit val decoder: Decoder[Post] = (c: HCursor) => {
    for {
      link <- c.get[String]("link")
      title <- c.get[String]("title")
      description <- c.get[String]("description")
      author <- c.get[String]("author")
      categories <- c.get[Seq[String]]("categories")
      upVotes <- c.get[Int]("upvotes")
      downVotes <- c.get[Int]("downvotes")
      viewsCount <- c.get[Int]("views")
      commentsCount <- c.get[Int]("comments")
      bookmarksCount <- c.get[Int]("bookmarks")
      updateDate <- c.get[String]("updateDate")
    } yield Post(
      link, title, description, author, categories,
      upVotes, downVotes, viewsCount, commentsCount, bookmarksCount,
      DateUtils.convertToDate(updateDate)
    )
  }

  def getTest = Post(
    "wwww.test.com", "test title", "test description", "test author", Seq("test"),
    0, 0, 0, 0, 0, DateUtils.currentDate
  )
}
