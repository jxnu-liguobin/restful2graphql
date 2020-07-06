package io.growing.graphql.request

import io.growing.graphql.RestMethod.HttpMethod

/**
 * restful标准请求
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/6
 * @param httpMethod  通过方法和relativeUri获取crud的operationName
 * @param resource    指的是/rest/:resource/:resource_id中的resource
 * @param queryParams 路径参数和查询参数
 * @param requestBody 请求体，格式与使用graphql一样，目前不处理请求体和响应体
 */
case class RestRequest(httpMethod: HttpMethod, resource: String, queryParams: Map[String, String] = Map.empty,
                       requestBody: Option[String] = None) extends Request
