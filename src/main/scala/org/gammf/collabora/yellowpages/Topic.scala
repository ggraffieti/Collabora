package org.gammf.collabora.yellowpages

import scala.annotation.tailrec

/**
  * @author Manuel Peruzzi
  * Represents a generic structured topic, in order to identify the scope in which an actor operates.
  * Every topic consists of a main topic and can contain some other subtopics.
  * @tparam A the generic topic type class.
  */
sealed trait Topic[A] {
  /**
    * Returns the main topic.
    */
  def main: Option[A]

  /**
    * Returns the subtopics.
    */
  def subtopics: Option[Topic[A]]

  /**
    * Returns the size of the topic, the number of structured topics.
    */
  def size(): Int

  /**
    * Executes a provided function once for each topic element.
    * @param consumer the function to apply to each element.
    */
  def foreach(consumer: A => Unit): Unit

  /**
    * Selects all elements of this topic which satisfy a predicate.
    * @param predicate the predicate used to filter the elements.
    * @return a new topic of all elements of this topic that satisfy the given predicate p.
    */
  def filter(predicate: A => Boolean): Topic[A]

  /**
    * Builds a new topic by applying a function to all elements of this topic.
    * @param f the function to apply to each element.
    * @tparam B the element type of the returned topic.
    * @return a new topic resulting from applying the given function f to each element of this list and collecting the results.
    */
  def map[B](f: A => B): Topic[B]

  /**
    * Builds a new topic by applying a function to all elements of this topic and using the elements of the resulting topics.
    * @param f the function to apply to each element.
    * @tparam B the element type of the returned topic.
    * @return a new topic resulting from applying the given function f to each element of this topic and concatenating the results.
    */
  def flatMap[B](f: A => Topic[B]): Topic[B]

  /**
    * Equals method. Compares this topic to some other topic.
    * @param that the other topic to be compared to this topic.
    * @return true if the given topic is equals to this, false otherwise.
    */
  def ==(that: Topic[A]): Boolean

  /**
    * Greater method. Checks if this topic is greater than some other topic.
    * @param that the other topic to be compared to this topic.
    * @return true if this topic is greater than the given topic, false otherwise.
    */
  def >(that: Topic[A]): Boolean

  /**
    * Greater or equals method. Checks if this topic is equals or greater than some other topic.
    * @param that the other topic to be compared to this topic.
    * @return true if this topic is equals or greater than the given topic, false otherwise.
    */
  def >=(that: Topic[A]): Boolean

  /**
    * Lesser method. Checks if this topic is lesser than some other topic.
    * @param that the other topic to be compared to this topic.
    * @return true if this topic is lesser than the given topic, false otherwise.
    */
  def <(that: Topic[A]): Boolean
  /**
    * Lesser or equals method. Checks if this topic is equals or lesser than some other topic.
    * @param that the other topic to be compared to this topic.
    * @return true if this topic is equals or lesser than the given topic, false otherwise.
    */
  def <=(that: Topic[A]): Boolean

  /**
    * A copy of this topic with an element appended as subtopic.
    * @param element the element to be appended.
    * @return a new topic of all elements of this topic followed by element as subtopic.
    */
  def :+(element: A): Topic[A]

  /**
    * A copy of this topic with an element prepended as main topic.
    * @param element the element to be prepended.
    * @return a new topic consisting of the element as main topic followed by all elements of this topic.
    */
  def +:(element: A): Topic[A]

  /**
    * Returns a new topic containing the elements from this topic followed by the elements from some other topic.
    * @param that the topic to be appended.
    * @return a new topic which contains all elements of this topic followed by all elements of that.
    */
  def ++(that: Topic[A]): Topic[A]

  /**
    * Adds an element as main topic of this topic.
    * @param element the element to be prepended.
    * @return a topic which contains the element as main topic and which continues with this topic.
    */
  def ::(element: A): Topic[A]
}

case class ActualTopic[A](_main: A, _subtopics: Topic[A]) extends TopicImpl[A]
case class EmptyTopic[A]() extends TopicImpl[A]

object :: {
  def unapply[A](topic: Topic[A]): Option[(A, Topic[A])] = topic match {
    case ActualTopic(m, s) => Some((m, s))
    case _ => None
  }
}

/**
  * @author Manuel Peruzzi
  * A generic implementation of a structured topic.
  * @tparam A the generic topic type class.
  */
trait TopicImpl[A] extends Topic[A] {
  override def main : Option[A] = this match {
    case m ::_ => Some(m)
    case _ => None
  }
  override def subtopics: Option[Topic[A]] = this match {
    case _ :: s => Some(s)
    case _ => None
  }
  override def size(): Int = {
    @tailrec
    def getSize(topic: Topic[A], size: Int): Int = topic match {
      case _ :: s => getSize(s, size + 1)
      case _ => size
    }
    getSize(this, 0)
  }
  override def foreach(consumer: A => Unit): Unit = this match {
    case m :: s => consumer(m); s foreach consumer
    case _ => None
  }
  override def filter(predicate: A => Boolean): Topic[A] = this match {
    case m :: s if predicate(m) => m :: (s filter predicate)
    case _ => EmptyTopic()
  }
  override def map[B](f: A => B): Topic[B] = this match {
    case m :: s => f(m) :: (s map f)
    case _ => EmptyTopic()
  }
  override def flatMap[B](f: A => Topic[B]): Topic[B] = this match {
    case m :: s => f(m) ++ (s flatMap f)
    case _ => EmptyTopic()
  }
  override def ==(topic: Topic[A]): Boolean = this match {
    case _ if size != topic.size => false
    case m :: s => m == topic.main.get && s == topic.subtopics.get
    case _ => true
  }
  override def >(topic: Topic[A]): Boolean = this match {
    case _ if size >= topic.size => false
    case m :: s => m == topic.main.get && s > topic.subtopics.get
    case _ => true
  }
  override def >=(topic: Topic[A]): Boolean = this > topic || this == topic
  override def <(topic: Topic[A]): Boolean = topic > this
  override def <=(topic: Topic[A]): Boolean = topic >= this
  override def :+(element: A): Topic[A] = this match {
    case m :: s => m :: (s :+ element)
    case _ => element :: EmptyTopic()
  }
  override def +:(element: A): Topic[A] = element :: this
  override def ++(topic: Topic[A]): Topic[A] = this match {
    case m :: s => m :: (s ++ topic)
    case _ => topic
  }
  override def ::(m: A): Topic[A] = ActualTopic(m, this)
  override def toString: String = this match {
    case m :: s if s != EmptyTopic() => m + "." + s.toString
    case m :: _ => m + ""
  }
}
