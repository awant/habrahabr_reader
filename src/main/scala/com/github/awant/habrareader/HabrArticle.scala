package com.github.awant.habrareader

import java.util.Date

import com.github.awant.habrareader.HabrArticle.Id


object HabrArticle {
  type Id = Int
}

case class ArticleRating(upvotes: Int, downvotes: Int)

case class HabrArticle(id: Id,
                       link: String,
                       title: String,
                       description: String,
                       author: String,
                       date: Date,
                       categories: Set[String],
                       fullText: Option[String],
                       rating: Option[ArticleRating])
