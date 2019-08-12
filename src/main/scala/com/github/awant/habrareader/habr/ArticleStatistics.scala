package com.github.awant.habrareader.habr

import cats.kernel.CommutativeSemigroup

// https://en.wikipedia.org/wiki/Conflict-free_replicated_data_type
case class ArticleStatistics(upVotes: Int,
                             downVotes: Int,
                             viewsCount: Int,
                             commentsCount: Int,
                             bookmarksCount: Int) {
  def totalVotes: Int = upVotes - downVotes
}

object ArticleStatistics {
  import cats.syntax.semigroup._

  private implicit val intCommutativeSemigroup = new CommutativeSemigroup[Int] {
    override def combine(x: Int, y: Int): Int = math.max(x, y)
  }

  implicit val commutativeSemigroup = new CommutativeSemigroup[ArticleStatistics] {
    override def combine(x: ArticleStatistics, y: ArticleStatistics): ArticleStatistics =
      ArticleStatistics(
        CommutativeSemigroup[Int].combine(x.upVotes, y.upVotes),
        x.downVotes |+| y.downVotes,
        x.viewsCount |+| y.viewsCount,
        x.commentsCount |+| y.commentsCount,
        x.bookmarksCount |+| y.bookmarksCount
      )

    //todo add usage of mapN instead of repeating names
  }

  implicit def optionCommutativeSemigroup[T: CommutativeSemigroup] = new CommutativeSemigroup[Option[T]] {

    override def combine(x: Option[T], y: Option[T]): Option[T] = {
      for {
        xx <- x
        yy <- y
      } yield xx |+| yy
    }.orElse(x).orElse(y)
  }
}
