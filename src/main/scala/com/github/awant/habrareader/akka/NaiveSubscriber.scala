package com.github.awant.habrareader.akka

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object NaiveSubscriber {
  def props(habrArticlesCache: ActorRef): Props = Props(new NaiveSubscriber(habrArticlesCache))
}

class NaiveSubscriber private(habrArticlesCache: ActorRef) extends Actor with ActorLogging {

  import HabrArticlesCache._

  override def preStart(): Unit = {
    log.debug("subscribing!")
    habrArticlesCache ! Subscribe(self, recieveNew = true, receiveUpdates = true, receiveExisting = true)
  }

  override def receive: Receive = {
    case PostAdded(post) =>
      log.debug(s"PostAdded(${post.link})")
    case PostUpdated(post) =>
      log.debug(s"PostUpdated(${post.link})")
  }
}
