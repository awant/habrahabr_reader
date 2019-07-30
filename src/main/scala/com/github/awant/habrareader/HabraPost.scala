package com.github.awant.habrareader


case class HabraPost(id: Int,
                     link: String,
                     title: String,
                     description: String,
                     author: String,
                     categories: Set[String])

// todo add date
// todo add full text
