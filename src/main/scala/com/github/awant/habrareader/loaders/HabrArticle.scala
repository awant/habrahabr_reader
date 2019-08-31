package com.github.awant.habrareader.loaders

import java.util.Date

import com.github.awant.habrareader.utils.DateUtils

/** some kind of id */
case class HabrArticleImprint(link: String, publicationDate: Date) {
  override def toString: String = s"link: $link; date: $publicationDate"
}

case class HabrArticle(id: Int,
                       link: String,
                       title: String,
                       description: String,
                       author: String,
                       publicationDate: Date,
                       categories: Set[String],
                       upVotes: Int = -1,
                       downVotes: Int = -1,
                       viewsCount: Int = -1,
                       commentsCount: Int = -1,
                       bookmarksCount: Int = -1) {
  override def hashCode(): Int = id
}
