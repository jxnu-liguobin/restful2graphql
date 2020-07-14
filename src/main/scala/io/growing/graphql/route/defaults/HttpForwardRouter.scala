package io.growing.graphql.route.defaults

import akka.http.scaladsl.server.Route
import io.growing.graphql.{ GraphqlExecution, RestOperation }
import io.growing.graphql.request.RestRequest
import io.growing.graphql.RestOperation.RestOperation
import spray.json.JsValue

import scala.concurrent.ExecutionContext

/**
 * 转发接口
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/10
 */
trait HttpForwardRouter extends HttpSupport with GraphqlExecution {

  override implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  private[this] def buildRestRequest(restOperation: RestOperation)(projectId: Option[String], typ: String, id: Option[String], authValue: String,
    requestBody: Option[JsValue] = None, isBatch: Boolean = false): RestRequest = {
    val auth = Map(authKey -> authValue)
    val requireParams = id.fold(auth)(i => auth ++ Map("id" -> i))
    RestRequest(restOperation = restOperation,
      resource = typ,
      contextParams = projectId.fold(requireParams)(pid => requireParams ++ Map("projectId" -> pid)),
      requestBody = requestBody,
      isBatch = isBatch)
  }

  override def route: Route = {
    path(ProjectMatcher.getRestUriPathMather / ResourceMatcher / ResourceIdMatcher) { (projectId, typ, id) =>
      // URI    /v1/projects/:project_id/:resources/:resource_id
      concat(
        get {
          headerValueByName(authKey) { authValue =>
            val req = buildRestRequest(RestOperation.GET)(projectId, typ, Some(id), authValue)
            val ret = executeRequest(req.toGraphqlRequest())
            resultJson(ret)
          }
        }
          ~ delete {
          headerValueByName(authKey) { authValue =>
            val req = buildRestRequest(RestOperation.DELETE)(projectId, typ, Some(id), authValue)
            val ret = executeRequest(req.toGraphqlRequest())
            resultJson(ret)
          }
        }
          ~ put {
          headerValueByName(authKey) { authValue =>
            decodeRequest {
              entity(as[JsValue]) { requestBody =>
                val req = buildRestRequest(RestOperation.UPDATE)(projectId, typ, Some(id), authValue, Some(requestBody))
                val ret = executeRequest(req.toGraphqlRequest())
                resultJson(ret)
              }
            }
          }
        }
      )
    } ~
      // URI    /v1/projects/:project_id/:resources
      path(ProjectMatcher.getRestUriPathMather / ResourceMatcher) { (projectId, typ) =>
        concat(
          get {
            headerValueByName(authKey) { authValue =>
              decodeRequest {
                //查询全部时为空
                entity(as[JsValue]) { requestBody =>
                  val req = buildRestRequest(RestOperation.GET)(projectId, typ, None, authValue, Some(requestBody))
                  val ret = executeRequest(req.toGraphqlRequest())
                  resultJson(ret)
                }
              }
            }
          }
            ~
            post {
              headerValueByName(authKey) { authValue =>
                decodeRequest {
                  entity(as[JsValue]) { requestBody =>
                    val req = buildRestRequest(RestOperation.CREATE)(projectId, typ, None, authValue, Some(requestBody))
                    val ret = executeRequest(req.toGraphqlRequest())
                    resultJson(ret)

                  }
                }
              }
            }
            ~
            delete {
              headerValueByName(authKey) { authValue =>
                decodeRequest {
                  entity(as[JsValue]) { requestBody =>
                    val req = buildRestRequest(RestOperation.DELETE)(projectId, typ, None, authValue, Some(requestBody), isBatch = true)
                    val ret = executeRequest(req.toGraphqlRequest())
                    resultJson(ret)

                  }
                }
              }
            }
            ~
            put {
              headerValueByName(authKey) { authValue =>
                decodeRequest {
                  entity(as[JsValue]) { requestBody =>
                    val req = buildRestRequest(RestOperation.UPDATE)(projectId, typ, None, authValue, Some(requestBody), isBatch = true)
                    val ret = executeRequest(req.toGraphqlRequest())
                    resultJson(ret)
                  }
                }
              }
            }
        )
      }
  }

}
