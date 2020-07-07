package io.growing.graphql

import akka.http.scaladsl.model.{ ContentTypes, HttpEntity }
import akka.http.scaladsl.server.{ HttpApp, Route }
import io.growing.graphql.request.RestRequest

/**
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/7
 */
object ForwardServer extends HttpApp with App {

  override def routes: Route =
    path("rest" / ResourceTypeMatcher / ResourceIdMatcher) { (typ, id) =>
      concat(
        get {
          val req = RestRequest(restOperation = RestOperation.GET, resource = typ, queryParams = Map("id" -> id))
          println(s"variables: ${req.getRequestBody}, operation: ${req.getOperationName}")
          complete(HttpEntity(ContentTypes.`application/json`, "Ok"))
        }
          ~ delete {
          val req = RestRequest(restOperation = RestOperation.DELETE, resource = typ, queryParams = Map("id" -> id))
          println(s"variables: ${req.getRequestBody}, operation: ${req.getOperationName}")
          complete(HttpEntity(ContentTypes.`application/json`, "Ok"))
        }
          ~ put {
          decodeRequest {
            entity(as[String]) { requestBody =>
              val req = RestRequest(restOperation = RestOperation.UPDATE,
                requestBody = Some(requestBody), resource = typ, queryParams = Map("id" -> id))
              println(s"variables: ${req.getRequestBody}, operation: ${req.getOperationName}")
              complete(HttpEntity(ContentTypes.`application/json`, "Ok"))
            }
          }
        }
      )
    } ~
      path("rest" / ResourceTypeMatcher) { typ =>
        concat(
          get {
            val req = RestRequest(restOperation = RestOperation.GET, resource = typ, queryParams = Map.empty, isBatch = true)
            println(s"variables: ${req.getRequestBody}, operation: ${req.getOperationName}")
            complete(HttpEntity(ContentTypes.`application/json`, "Ok"))
          }
            ~
            post {
              decodeRequest {
                entity(as[String]) { requestBody =>
                  val req = RestRequest(restOperation = RestOperation.CREATE,
                    requestBody = Some(requestBody), resource = typ, queryParams = Map.empty)
                  println(s"variables: ${req.getRequestBody}, operation: ${req.getOperationName}")
                  complete(HttpEntity(ContentTypes.`application/json`, "Ok"))
                }
              }
            }
            ~
            delete {
              decodeRequest {
                entity(as[String]) { requestBody =>
                  val req = RestRequest(restOperation = RestOperation.DELETE,
                    requestBody = Some(requestBody), resource = typ, queryParams = Map.empty, isBatch = true)
                  println(s"variables: ${req.getRequestBody}, operation: ${req.getOperationName}")
                  complete(HttpEntity(ContentTypes.`application/json`, "Ok"))
                }
              }
            }
        )
      }

  ForwardServer.startServer("localhost", 8080)
}
