package io.growing.graphql

import java.io.IOException

import com.google.common.net.HttpHeaders
import io.growing.graphql.request.GraphqlRequest
import io.growing.graphql.utils.{ JacksonScalaSupport, OkHttp }
import okhttp3._

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, Future, Promise }

/**
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/7
 */
object GraphqlExecution extends App {


  def executeRequest(request: GraphqlRequest): Future[String] = {
    val json = MediaType.parse("application/json; charset=utf-8")
    val body = request.toString
    println("graphql request\n" + body)
    val url = s"http://localhost:8086/projects/WlGk4Daj/graphql"
    val rb = new Request.Builder().url(url).addHeader(HttpHeaders.USER_AGENT, "X-User-Id")
      .post(RequestBody.create(body, json))
    val promise = Promise[String]

    OkHttp.client.newCall(rb.build()).enqueue(new Callback {

      override def onFailure(call: Call, e: IOException): Unit = {
        promise.failure(e)
      }

      override def onResponse(call: Call, response: Response): Unit = {
        if (response.isSuccessful) {
          val bytes = response.body().bytes()
          promise.success(new String(bytes, "utf8"))
        } else {
          val r: String = JacksonScalaSupport.mapper.writeValueAsString(response)
          promise.failure(new Exception(r))
        }
      }
    })
    promise.future
  }

  override def main(args: Array[String]): Unit = {
    val s = executeRequest(new GraphqlRequest(operationName = "insightDimensions", variables = Some(
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
        |""".stripMargin))
    println(Await.result(s, Duration.Inf))
  }

}
