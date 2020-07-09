package io.growing.graphql.request

/**
 * graphql 所需的三个参数
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/6
 * @param operationName graphql操作名称，一般对应一个内部fetcher，实际上可为空，这里使用这个转发请求，不能为空
 * @param variables     graphql请求的请求体，参数，json格式
 * @param query         graphql请求语句，难点
 * @param authToken     graphql授权
 */
class GraphqlRequest(operationName: String, variables: Option[String] = None, query: String, executeUrl: String, authToken: (String, String)) extends Request {

  def getExecuteUrl: String = executeUrl

  def getAuthToken: (String, String) = authToken

  override def toString: String = {
    s"""
       |{
       |  "operationName": "$operationName",
       |  "variables": ${variables.get},
       |  "query": "${query.replace("\n", "").trim}"
       |}
       |""".stripMargin
  }
}
