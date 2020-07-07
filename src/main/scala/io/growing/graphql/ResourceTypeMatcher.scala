package io.growing.graphql

import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.server.PathMatcher.Matched
import akka.http.scaladsl.server.PathMatcher1

/**
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/7
 */
object ResourceTypeMatcher extends PathMatcher1[String] {

  def apply(path: Path): Matched[Tuple1[String]] = Matched(path.tail, Tuple1(path.head.toString))
}


object ResourceIdMatcher extends PathMatcher1[String] {

  def apply(path: Path): Matched[Tuple1[String]] = Matched(path.tail, Tuple1(path.head.toString))
}