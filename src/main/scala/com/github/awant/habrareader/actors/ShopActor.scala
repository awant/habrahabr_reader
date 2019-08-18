package com.github.awant.habrareader.actors

import scala.concurrent.duration._
import akka.actor.{Actor, ActorRef, Props}

import scala.concurrent.ExecutionContext.Implicits.global


object ShopActor {
  def props(updatePostsInterval: FiniteDuration, library: ActorRef): Props =
    Props(new ShopActor(updatePostsInterval, library))

  final case class UpdatePosts()
}

class ShopActor private(updatePostsInterval: FiniteDuration, library: ActorRef) extends Actor {
  import ShopActor._

  override def preStart(): Unit = {
    context.system.scheduler.schedule(0.second, updatePostsInterval, self, UpdatePosts)
  }

  override def receive: Receive = {
    case UpdatePosts => updatePosts()
    }

  def updatePosts(): Unit = {
    // TODO: download posts, parse posts, store in the library
  }

}
