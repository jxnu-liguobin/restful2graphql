package io.growing.graphql.request

import com.typesafe.scalalogging.LazyLogging
import io.growing.graphql.RestOperation
import io.growing.graphql.RestOperation.RestOperation
import spray.json.{ JsObject, JsString, JsValue }

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
  requestBody: Option[JsValue] = None, isBatch: Boolean = false) extends Request with LazyLogging {

  private val empty = JsObject.empty

  /**
   * 获取rest的请求体，这个也是graphql请求传递的变量，该参数可选
   *
   * @return
   */
  def getRequestBody: String = {
    if (queryParams.isEmpty) {
      val ret = if (requestBody.isEmpty) empty else requestBody.getOrElse(empty)
      ret.prettyPrint
    } else {
      val originFields = requestBody.getOrElse(empty).asJsObject.fields
      val newRequestBody = if (originFields.keySet.contains("id")) {
        requestBody.getOrElse(empty).asJsObject
      } else {
        requestBody.getOrElse(empty).asJsObject.copy(fields = originFields ++ Map("id" -> JsString(queryParams("id"))))

      }
      logger.info(s"build restful request body: \n${newRequestBody.prettyPrint}")
      newRequestBody.prettyPrint
    }

  }

  /**
   * 根据rest请求的资源和方法，获得graphql对应的操作方法，这个操作方法在graphql中是唯一的，但是实际上是可选的，这里是为了做映射
   * 实际上是后端graphql服务的一个fetcher
   *
   * @return
   */
  def getOperationName: String = {
    val operation = restOperation match {
      case RestOperation.CREATE => restOperation.toString + resource.capitalize
      case RestOperation.DELETE if isBatch => "batch" + restOperation.toString.capitalize + resource.capitalize
      case RestOperation.DELETE => restOperation.toString + resource.capitalize
      case RestOperation.GET if isBatch => resource
      case RestOperation.GET => resource
      case RestOperation.UPDATE if isBatch => "batch" + restOperation.toString.capitalize + resource.capitalize
      case RestOperation.UPDATE => restOperation.toString + resource.capitalize
    }
    logger.info(s"build restful request method mapping to graphql fetcher: $operation")
    operation
  }

  /**
   * 将rest请求体转化为graphql请求体
   *
   * @param operationQueryMappings
   * @return
   */
  def toGraphqlRequest()(implicit operationQueryMappings: Map[String, String]): GraphqlRequest = {
    val currentOperation = operationQueryMappings(this.getOperationName)
    logger.info(s"get graphql fetcher name: $currentOperation")
    new GraphqlRequest(operationName = this.getOperationName, variables = Some(this.getRequestBody),
      query = currentOperation)
  }
}