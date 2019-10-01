package com.github.awant.habrareader.models

import java.util.Date

import com.github.awant.habrareader.utils.DateUtils._
import io.circe._
import io.circe.syntax._

case class Post(id: Long,
                link: String,
                title: String,
                description: String,
                author: String,
                categories: Seq[String],
                upVotes: Int,
                downVotes: Int,
                viewsCount: Int,
                commentsCount: Int,
                bookmarksCount: Int,
                updateDate: Date) {
  override def toString: String = s"Post[$title]"
}

object Post {
  implicit val encoder: Encoder[Post] = (post: Post) => {
    Json.obj(
      "id" := post.id,
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
      id <- c.get[Long]("id")
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
      updateDate <- c.get[Date]("updateDate")
    } yield Post(
      id, link, title, description, author, categories,
      upVotes, downVotes, viewsCount, commentsCount, bookmarksCount,
      updateDate
    )
  }
}
