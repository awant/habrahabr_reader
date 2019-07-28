package com.github.awant.habrareader


case class HabraPost(id: Int,
                     guid: String,
                     title: String,
                     description: String,
                     pubDate: String,
                     author: String,
                     categories: Set[String])
