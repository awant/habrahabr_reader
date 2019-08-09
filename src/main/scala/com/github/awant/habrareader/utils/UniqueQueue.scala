package com.github.awant.habrareader.utils

import scala.collection.mutable

class UniqueQueue[T] {

  private val queue = new mutable.Queue[T]()
  private val queuedElems = new mutable.HashSet[T]()

  def enqueue(elem: T): Boolean =
    if (!queuedElems.contains(elem)) {
      queue.enqueue(elem)
      queuedElems += elem
      true
    } else {
      false
    }

  def isEmpty: Boolean = queue.isEmpty

  def dequeue(): Option[T] =
    if (queue.nonEmpty) {
      val result = queue.dequeue()
      queuedElems.remove(result)
      Option(result)
    } else {
      None
    }
}
