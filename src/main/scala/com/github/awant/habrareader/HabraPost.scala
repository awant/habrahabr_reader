package com.github.awant.habrareader

import java.util.Date


case class HabraPost(id: Int,
                     link: String,
                     title: String,
                     description: String,
                     author: String,
                     date: Date,
                     categories: Set[String],
                     fullText: Option[String])
