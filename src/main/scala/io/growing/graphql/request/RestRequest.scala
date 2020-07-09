package io.growing.graphql.request

import com.typesafe.scalalogging.LazyLogging
import io.growing.graphql.RestOperation
import io.growing.graphql.RestOperation.RestOperation
import io.growing.graphql.utils.Config
import spray.json.{ JsObject, JsString, JsValue }

/**
 * restful标准请求
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/6
 * @param restOperation 通过方法和relativeUri获取crud的operationName
 * @param resource      指的是/rest/:resource/:resource_id中的resource
 * @param queryParams   查询参数，携带项目id，授权的token键值对，类似执行上下文
 * @param requestBody   请求体，格式与使用graphql一样，目前不处理请求体和响应体
 * @param isBatch       查询和删除需要区分批量
 */
case class RestRequest(restOperation: RestOperation, resource: String, queryParams: Map[String, String] = Map.empty,
  requestBody: Option[JsValue] = None, isBatch: Boolean = false) extends Request with LazyLogging {

  private[this] val configUrl = Config.getGraphqlUrl
  private[this] val empty = JsObject.empty
  private[this] val authKey = Config.getAuthKey()

  def buildAuthToken(): (String, String) = {
    logger.info(s"build auth token: \n ${authKey -> queryParams(authKey)}")
    authKey -> queryParams(authKey)
  }

  /**
   * 对于URL存在%s，使用项目id进行替换，得到新的graphql URL
   *
   * @return
   */
  def buildGraphqlExecutionUri: String = {
    val executeUrl = if (queryParams.nonEmpty && configUrl.contains("%s") && queryParams.contains("projectId")) {
      configUrl.format(queryParams("projectId"))
    } else {
      configUrl
    }

    logger.info(s"build graphql url: \n $executeUrl")
    executeUrl
  }

  /**
   * 获取rest的请求体（该参数可选），这个也是graphql请求传递的变量
   *
   * @return
   */
  def buildRequestBody: String = {
    if (queryParams.isEmpty) {
      val ret = if (requestBody.isEmpty) empty else requestBody.getOrElse(empty)
      ret.prettyPrint
    } else {
      //请求体可能存在的id与查询参数穿过来的路径中的id，选取路径参数为准，忽略请求体中的
      val originFields = requestBody.getOrElse(empty).asJsObject.fields
      val newRequestBody = if (originFields.keySet.contains("id")) {
        requestBody.getOrElse(empty).asJsObject
      } else {
        val newFields = if (queryParams.contains("id")) {
          originFields - "id" ++ Map("id" -> JsString(queryParams("id")))
        } else originFields

        requestBody.getOrElse(empty).asJsObject.copy(fields = newFields)

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
  def buildOperationName: String = {
    val operation = restOperation match {
      case RestOperation.CREATE => restOperation.toString + resource.capitalize
      case RestOperation.DELETE if isBatch => "batch" + restOperation.toString.capitalize + resource.capitalize
      case RestOperation.DELETE => restOperation.toString + resource.capitalize
      case RestOperation.GET if isBatch => resource
      case RestOperation.GET => resource
      case RestOperation.UPDATE if isBatch => "batch" + restOperation.toString.capitalize + resource.capitalize
      case RestOperation.UPDATE => restOperation.toString + resource.capitalize
    }
    logger.info(s"build restful request method mapping to graphql fetcher: \n$operation")
    operation
  }

  /**
   * 将rest请求体转化为graphql请求体
   *
   * @param operationQueryMappings
   * @return
   */
  def toGraphqlRequest()(implicit operationQueryMappings: Map[String, String]): GraphqlRequest = {
    val currentOperation = operationQueryMappings(this.buildOperationName)
    logger.info(s"get graphql query from memory: \n$currentOperation")
    new GraphqlRequest(operationName = this.buildOperationName, variables = Some(this.buildRequestBody),
      query = currentOperation, executeUrl = this.buildGraphqlExecutionUri, this.buildAuthToken())
  }
}