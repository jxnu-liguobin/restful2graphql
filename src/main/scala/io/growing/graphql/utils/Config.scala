package io.growing.graphql.utils

import com.typesafe.config.ConfigFactory

import scala.collection.JavaConverters._
import scala.util.Try

/**
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/7
 */
object Config {

  val exclude = "graphql.exclude"
  val include = "graphql.include"
  private lazy val config = ConfigFactory.load(this.getClass.getClassLoader)

  /**
   * 获取当前fetcher需要排除的字段，这是因为自动生成的gql，会使用所有字段
   *
   * @param operationName 实际上是后端graphql服务的一个fetcher
   * @return
   */
  def getExcludeFields(operationName: String): List[String] = {
    Try(config.getConfig(exclude).getList(operationName).unwrapped().asScala.map(_.toString).toList).getOrElse(List.empty[String])
  }

}
