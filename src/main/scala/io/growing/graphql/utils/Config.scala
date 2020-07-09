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

  private val exclude = "graphql.exclude"
  //for test
  private val auth = "graphql.auth"
  private val restful = "dryad.service.http"
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

  /**
   * graphql服务地址
   *
   * @return
   */
  def getGraphqlUrl = config.getString("graphql.url")

  /**
   * graphql鉴权请求头的key
   *
   * @return
   */
  def getAuthKey(): String = {
    val key = config.getConfig(auth).getString("key")
    key
  }

  /**
   * restful转发服务的地址和端口
   *
   * @return
   */
  def getRestfulServerConfig(): (String, Int) = {
    val host = Try(config.getConfig(restful).getString("host")).getOrElse("0.0.0.0")
    val port = config.getConfig(restful).getInt("port")
    host -> port
  }

  /**
   * 从配置获取restful前缀
   *
   * @return
   */
  def getRestUriPrefix(): String = {
    config.getConfig(restful).getString("prefix")
  }
}
