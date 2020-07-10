package io.growing.graphql.request

import com.typesafe.scalalogging.LazyLogging
import io.growing.graphql.Config

/**
 * graphql 所需的三个参数
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/6
 * @param operationName graphql操作名称，一般对应一个内部fetcher，实际上可为空，这里使用这个转发请求，不能为空
 * @param variables     graphql请求的请求体，参数，json格式
 * @param query         graphql请求语句，难点
 * @param contextParams restful带过来的参数 上下文参数传递
 */
class GraphqlRequest(operationName: String, variables: Option[String] = None, query: String,
  contextParams: Map[String, Object] = Map.empty) extends Request with LazyLogging {

  private[this] val configUrl = Config.getGraphqlUrl
  private[this] val authKey = Config.getAuthKey()

  /**
   * 获取授权的token
   *
   * @return
   */
  def getAuthToken(): (String, String) = {
    logger.info(s"build auth token: \n ${authKey -> contextParams(authKey)}")
    authKey -> contextParams(authKey).toString
  }

  /**
   * 获取所有请求过来的参数，目前主要是路径参数
   *
   * @return
   */
  def getContextParams: Map[String, Object] = contextParams

  /**
   * 获取graphql查询语句
   *
   * @return
   */
  def getQuery(): String = query.replace("\n", "").trim

  /**
   * 获取graphql的参数
   *
   * @return
   */
  def getVariables(): String = variables.getOrElse("{}")

  /**
   * 生成graphql调用的body
   *
   * @return
   */
  override def toString: String = {
    s"""
       |{
       |  "operationName": "$operationName",
       |  "variables": ${variables.get},
       |  "query": "${query.replace("\n", "").trim}"
       |}
       |""".stripMargin
  }

  /**
   * 对于URL存在%s，使用项目id进行替换，得到新的graphql URL
   *
   * @return
   */
  def getExecuteUrl(): String = {
    val executeUrl = if (contextParams.nonEmpty && configUrl.contains("%s") && contextParams.contains("projectId")) {
      configUrl.format(contextParams("projectId"))
    } else {
      configUrl
    }

    logger.info(s"build graphql url: \n $executeUrl")
    executeUrl
  }
}
