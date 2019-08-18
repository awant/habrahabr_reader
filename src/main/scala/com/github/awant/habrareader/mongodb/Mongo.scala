package com.github.awant.habrareader.mongodb

import com.typesafe.config.ConfigFactory
import org.bson.codecs.configuration.CodecRegistries._
import org.mongodb.scala._
import com.github.awant.habrareader.models.{Chat, Post, Event}
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._

object Mongo {
  lazy val config = ConfigFactory.load()
  lazy val mongoClient: MongoClient = MongoClient(config.getString("mongo.uri"))
  lazy val codecRegistry = fromRegistries(fromProviders(classOf[Chat]), DEFAULT_CODEC_REGISTRY)
  lazy val database: MongoDatabase = mongoClient.getDatabase(config.getString("mongo.database")).withCodecRegistry(codecRegistry)

  lazy val chatCollection: MongoCollection[Chat] = database.getCollection[Chat]("chats")
  lazy val postCollection: MongoCollection[Post] = database.getCollection[Post]("posts")
  lazy val eventCollection: MongoCollection[Event] = database.getCollection[Event]("events")
}
