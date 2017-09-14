package org.gammf.collabora.yellowpages.util

/**
  * This trait represent a data structure whose elements are cachable, that is, valid only for a certain period of time.
  * At a certain time, an element can be considered as brand-new, still-valid or outdated, depending on its insertion time.
  * @tparam A the type of the data structure elements.
  */
trait CachableSet[A] {
  /**
    * Adds a new element to the data structure, overwriting an eventual already-present copy.
    * @param element the element to be added.
    */
  def ::(element: A): Unit

  /**
    * Returns the first element of the data structure which matches with the given predicate.
    * @param predicate the predicate to be applied over the data structure elements.
    * @return an option of tuple, which can be <ul>
    * <li> (element, true) if the element is considered ad brand-new.
    * <li> (element, false) if the elements isn't brand-new, but still valid.
    * <li> None if the element is outdated or not present.
    * </ul>
    */
  def get(predicate: A => Boolean) : Option[(A, Boolean)]
}

/**
  * Basilar implementation of [[CachableSet]].
  * @tparam A the type of the data structure elements.
  */
trait CachableSetImpl[A] extends CachableSet[A] {
  private[this] val shortTimeout = 5 * 60 * 1000
  private[this] val longTimeout = 3 * 60 * 60 * 1000
  private[this] var cachableSet: Set[(A, Long)] = Set()

  override def ::(element: A): Unit = cachableSet = cachableSet.filterNot(setElement => setElement._1 == element) + ((element, System.currentTimeMillis))

  override def get(predicate: (A) => Boolean): Option[(A, Boolean)] = {
    cachableSet.find(tuple => predicate(tuple._1)) match {
      case Some((element, timestamp)) if isTimestampValid(timestamp) => Some((element, true))
      case Some((element, timestamp)) if !isTimestampValid(timestamp) && !isOverLongTimeout(timestamp) => Some((element, false))
      case Some((element, timestamp)) if isOverLongTimeout(timestamp) => cachableSet = cachableSet - ((element,timestamp)); None
      case _ => None
    }
  }

  private[this] def isTimestampValid(timestamp: Long): Boolean = System.currentTimeMillis - timestamp < shortTimeout

  private[this] def isOverLongTimeout(timestamp: Long): Boolean = System.currentTimeMillis - timestamp > longTimeout
}

case class BasicCachableSet[A]() extends CachableSetImpl[A]

object CachableSet {
  def apply[A](): CachableSet[A] = BasicCachableSet[A]()
}
