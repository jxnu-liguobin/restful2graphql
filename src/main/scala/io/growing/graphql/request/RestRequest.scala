package io.growing.graphql.request

import io.growing.graphql.RestOperation
import io.growing.graphql.RestOperation.RestOperation

/**
 * restful标准请求
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/6
 * @param restOperation 通过方法和relativeUri获取crud的operationName
 * @param resource      指的是/rest/:resource/:resource_id中的resource
 * @param queryParams   查询参数，目前仅存储id。因为graphql的更新和删除目前使用了id，但是id在restful中放在路径参数，所以这里分开存
 * @param requestBody   请求体，格式与使用graphql一样，目前不处理请求体和响应体
 * @param isBatch       查询和删除需要区分批量
 */
case class RestRequest(restOperation: RestOperation, resource: String, queryParams: Map[String, String] = Map.empty,
  requestBody: Option[String] = None, isBatch: Boolean = false) extends Request {

  def getRequestBody = requestBody.getOrElse("{}")

  def getOperationName: String = {
    restOperation match {
      case RestOperation.CREATE => restOperation.toString + resource.capitalize
      case RestOperation.DELETE if isBatch => "batch" + restOperation.toString.capitalize + resource.capitalize
      case RestOperation.DELETE => restOperation.toString + resource.capitalize
      case RestOperation.GET if isBatch => resource + "s"
      case RestOperation.GET => resource
      case RestOperation.UPDATE => restOperation.toString + resource.capitalize
    }
  }
}

object RestRequest {
  def toGraphqlRequest(restRequest: RestRequest): Unit = {
    new GraphqlRequest(operationName = restRequest.getOperationName, variables = Some(restRequest.getRequestBody), query = "query not impl")
  }
}
