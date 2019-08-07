package com.github.awant.habrareader

import java.util.Date

import com.github.awant.habrareader.HabrArticle.Id


object HabrArticle {
  type Id = Int
}

case class ArticleStatistics(upVotes: Int,
                             downVotes: Int,
                             viewsCount: Int,
                             commentsCount: Int,
                             bookmarksCount: Int) {
  def totalVotes: Int = upVotes - downVotes
}

case class HabrArticle(id: Id,
                       link: String,
                       title: String,
                       description: String,
                       author: String,
                       date: Option[Date],
                       categories: Set[String],
                       fullText: Option[String],
                       rating: Option[ArticleStatistics]) {

  def merge(anotherVersion: HabrArticle): HabrArticle = {
    assert(id == anotherVersion.id)
    ??? // todo
  }
}
