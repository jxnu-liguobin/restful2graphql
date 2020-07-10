package io.growing.graphql.parser

import com.google.common.reflect.ClassPath

import scala.collection.JavaConverters._

/**
 * 获取路径下所有资源
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/10
 */
trait ScannerSupport {

  val classPath: ClassPath

  def getAllResources(prefix: String, suffix: String): Set[ClassPath.ResourceInfo] = {
    val resourceClass = classOf[ClassPath.ResourceInfo]
    classPath.getResources.asScala.filter { r ⇒
      lazy val name = r.getResourceName
      r.getClass == resourceClass && name.startsWith(prefix) && name.endsWith(suffix)
    }.toSet
  }
}
