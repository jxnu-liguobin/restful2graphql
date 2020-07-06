package io.growing.graphql.request

import io.growing.graphql.RestMethod.RestMethod

/**
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/6
 */
case class RestRequest(method: RestMethod, relativeUri: String, queryParams: Map[String, String] = Map.empty,
  requestBody: Option[RestRequestBody] = None) extends Request
