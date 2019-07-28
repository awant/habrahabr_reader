package com.github.awant.habrareader.akka

import akka.actor.{Actor, ActorLogging}
import com.github.awant.habrareader.HabraPost
import com.github.awant.habrareader.akka.PostsHolderActor.{AddPost, GetPost, GetPosts, SaveOnDisk, UpdatePost}

import scala.collection.mutable

object PostsHolderActor {

	case class AddPost(post: HabraPost)

	case class UpdatePost(post: HabraPost)

	case object SaveOnDisk

	case class GetPost(id: Int)

	case class GetPosts(predicate: HabraPost => Boolean)

}

class PostsHolderActor() extends Actor with ActorLogging {

	// todo loading previous post from disk or smth else

	private val allPosts: mutable.Map[Int, HabraPost] = new mutable.HashMap()

	override def receive: Receive = {
		case AddPost(post) =>
			allPosts(post.id) = post
		// todo reroute messages to other actors like filtering channels of posts
		case UpdatePost(post) =>
			allPosts(post.id) = post
		// todo reroute messages to other actors like filtering channels of posts
		case GetPost(id) =>
			allPosts.get(id)
		case GetPosts(predicate) =>
			allPosts.values.filter(predicate)
		case SaveOnDisk =>
			??? // todo
	}
}
