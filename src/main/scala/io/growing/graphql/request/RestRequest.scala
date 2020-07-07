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

  /**
   * 获取rest的请求体，这个也是graphql请求传递的变量，该参数可选
   *
   * @return
   */
  def getRequestBody: String = {
    if (queryParams.isEmpty) if (requestBody.isEmpty || requestBody.get == "") "{}" else requestBody.getOrElse("{}")
    else {
      val hashId =
        s"""
           |"id": "${queryParams("id")}",
           |""".stripMargin
      val vars = new StringBuilder
      vars.append("{").append(hashId)
      //说明不是默认是空花括号，需要去除逗号
      if (requestBody.getOrElse("{}").length == 2) vars.deleteCharAt(hashId.length - 1)
      vars.append(requestBody.getOrElse("{}").replaceFirst("\\{", ""))
      vars.toString()
    }

  }

  /**
   * 根据rest请求的资源和方法，获得graphql对应的操作方法，这个操作方法在graphql中是唯一的，但是实际上是可选的，这里是为了做映射
   * 实际上是后端graphql服务的一个fetcher
   *
   * @return
   */
  def getOperationName: String = {
    restOperation match {
      case RestOperation.CREATE => restOperation.toString + resource.capitalize
      case RestOperation.DELETE if isBatch => "batch" + restOperation.toString.capitalize + resource.capitalize
      case RestOperation.DELETE => restOperation.toString + resource.capitalize
      case RestOperation.GET if isBatch => resource
      case RestOperation.GET => resource
      case RestOperation.UPDATE if isBatch => "batch" + restOperation.toString.capitalize + resource.capitalize
      case RestOperation.UPDATE => restOperation.toString + resource.capitalize
    }
  }

  /**
   * 将rest请求体转化为graphql请求体
   *
   * @param operationQueryMappings
   * @return
   */
  def toGraphqlRequest()(implicit operationQueryMappings: Map[String, String]): GraphqlRequest = {
    new GraphqlRequest(operationName = this.getOperationName, variables = Some(this.getRequestBody),
      query = operationQueryMappings(this.getOperationName))
  }
}