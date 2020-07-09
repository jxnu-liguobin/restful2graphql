package io.growing.graphql

import java.io.IOException

import com.typesafe.scalalogging.LazyLogging
import io.growing.graphql.request.GraphqlRequest
import io.growing.graphql.utils.{ Config, JacksonScalaSupport, OkHttp }
import okhttp3._

import scala.concurrent.{ Await, Future, Promise }
import scala.concurrent.duration.Duration

/**
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/7
 */
object GraphqlExecution extends App with LazyLogging {

  //因为有main方法，全局变量加载拿不到值，必须加lazy
  private lazy val json = MediaType.parse("application/json; charset=utf-8")
  private lazy val charset = "utf8"

  def executeRequest(request: GraphqlRequest): Future[String] = {
    val body = request.toString
    logger.info(s"graphql request: \n$body")
    val url = request.getExecuteUrl
    val rb = new Request.Builder().url(url).addHeader(request.getAuthToken._1, request.getAuthToken._2)
      .post(RequestBody.create(body, json))
    val promise = Promise[String]

    OkHttp.client.newCall(rb.build()).enqueue(new Callback {

      override def onFailure(call: Call, e: IOException): Unit = {
        promise.failure(e)
      }

      override def onResponse(call: Call, response: Response): Unit = {
        if (response.isSuccessful) {
          val bytes = response.body().bytes()
          promise.success(new String(bytes, charset))
        } else {
          val r: String = JacksonScalaSupport.mapper.writeValueAsString(Map(response.code() -> response.message()))
          promise.failure(new Exception(r))
        }
      }
    })
    promise.future
  }

  override def main(args: Array[String]): Unit = {
    val s = executeRequest(new GraphqlRequest(operationName = "insightDimensions", executeUrl = Config.getGraphqlUrl, variables = Some(
      """
        |   {
        |        "measurements":[
        |            {
        |                "id":"evp9kDOx",
        |                "type":"custom",
        |                "attribute":""
        |            }
        |        ],
        |        "timeRange":"abs:1593446400000,1593532799999"
        |    }
        |""".stripMargin), query =
      """
        |  query ($measurements: [MeasurementInput]) {
        |  insightDimensions(measurements: $measurements) {
        |    id
        |    name
        |    groupId
        |    groupName
        |    type
        |    valueType
        |    __typename
        |  }
        |}
        |
        |""".stripMargin, authToken = "Cookie" -> "token"))
    println(Await.result(s, Duration.Inf))
  }

}
