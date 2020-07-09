package io.growing.graphql

import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.server.PathMatcher.{ Matched, _ }
import akka.http.scaladsl.server.PathMatcher1
import io.growing.graphql.utils.Config

/**
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/7
 */
object ResourceMatcher extends PathMatcher1[String] {

  def apply(path: Path): Matched[Tuple1[String]] = Matched(path.tail, Tuple1(path.head.toString))
}


object ResourceIdMatcher extends PathMatcher1[String] {

  def apply(path: Path): Matched[Tuple1[String]] = Matched(path.tail, Tuple1(path.head.toString))
}

object ProjectMatcher {

  //TODO 支持其他格式
  def getRestUriPathMather: PathMatcher1[Option[String]] = {
    val uri = Config.getRestUriPrefix().tail
    //目前只支持一个预定义路径参数，就是项目id
    if (uri.count(_ == ':') == 1 && uri.count(_ == '/') == 2) {
      val pathArray = uri.split("/")
      //意思是：匹配 forward/projects/:project_id 和 forward/projects
      val matcher: PathMatcher1[Option[String]] = s"${pathArray(0)}" / s"${pathArray(1)}" / ResourceIdMatcher.?
      matcher
    } else {
      throw new Exception("not support value for `rest.prefix`")
    }
  }
}