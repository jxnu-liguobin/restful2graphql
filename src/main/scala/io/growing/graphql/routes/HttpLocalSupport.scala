package io.growing.graphql.routes

import java.util.Locale

import akka.http.scaladsl.server.Route
import com.google.common.net.HttpHeaders
import io.growing.graphql.Rest2GraphqlForwardServer.executeRequest
import io.growing.graphql.request.RestRequest
import io.growing.graphql.{ Config, Constants }

import scala.concurrent.Future

/**
 * akka 调用本地graphql helper
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/10
 */
trait HttpLocalSupport extends HttpSupport {

  private[this] val authKey = Config.getAuthKey()

  def executeRequestWithHeaders(route: Future[String] => Route, r: RestRequest)(implicit operationQueryMappings: Map[String, String]): Route = {
    optionalHeaderValueByName(Constants.XUserId) { xUserId =>
      headerValueByName(authKey) { authValue =>
        optionalHeaderValueByName(Constants.XRequestId) { XRequestId =>
          optionalHeaderValueByName(Constants.XInnerUserId) { XInnerUserId =>
            optionalHeaderValueByName(HttpHeaders.ACCEPT_LANGUAGE) { locale =>
              optionalHeaderValueByName(HttpHeaders.X_FORWARDED_FOR) { XForwardedFor =>
                optionalHeaderValueByName(Constants.XRealIP) { XRealIP =>
                  val extractParams: Map[String, Object] = Map(Constants.XUserId -> xUserId.getOrElse(""), Constants.XRequestId -> XRequestId.getOrElse(""),
                    Constants.XInnerUserId -> XInnerUserId.orElse(xUserId).getOrElse(""), HttpHeaders.ACCEPT_LANGUAGE
                      -> locale.getOrElse(Locale.CHINA), HttpHeaders.X_FORWARDED_FOR -> XForwardedFor.getOrElse(""),
                    Constants.XRealIP -> XRealIP.getOrElse(""), authKey -> authValue)
                  val newR = r.copy(contextParams = r.contextParams ++ extractParams)
                  route {
                    executeRequest(newR.toGraphqlRequest())
                  }
                }
              }
            }
          }
        }
      }
    }
  }

}
