package io.growing.graphql

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import io.growing.dryad.ServiceProvider
import io.growing.dryad.portal.Schema
import io.growing.graphql.route.defaults.{ HealthyRouter, HttpForwardRouter }

import scala.concurrent.Future
import scala.io.StdIn
import scala.util.{ Failure, Success, Try }

/**
 * 1.使用akkahttp转发请求
 * 2.调用本地graphql时不需要此server，忽略
 *
 * @author liguobin@growingio.com
 * @version 1.0,2020/7/7
 */
object ForwardServerBootstrap extends App with HttpForwardRouter with HealthyRouter {

  private val (host, port) = Config.getRestfulServerConfig()
  private[this] val serviceProvider = ServiceProvider()
  private[this] val prefixs = Config.getRestUriPrefix().tail.split("/")
  private[this] val restUriPattern1 = "/" + Seq(prefixs(0), prefixs(1)).mkString("/") + "/([\\w]+)"
  private[this] val restUriPattern2 = "/" + Seq(prefixs(0), prefixs(1)).mkString("/") + "/([\\w]+)/([\\w]+)"

  private[this] implicit val system = ActorSystem()
  private[this] implicit val dispatcher = system.dispatcher
  private[this] implicit val materializer = ActorMaterializer()

  private[this] val routes: Route = route

  //健康检查
  private[this] val healthyRoute: Route = healthyRoute()

  private val bindingFuture: Future[Http.ServerBinding] = Http().bindAndHandle(routes ~ healthyRoute, host, port)

  println(
    """
      |  ________                    .__           .__    ___________                                    .___
      | /  _____/___________  ______ |  |__   _____|  |   \_   _____/____________  _  _______ _______  __| _/
      |/   \  __\_  __ \__  \ \____ \|  |  \ / ____/  |    |    __)/  _ \_  __ \ \/ \/ /\__  \\_  __ \/ __ |
      |\    \_\  \  | \// __ \|  |_> >   Y  < <_|  |  |__  |     \(  <_> )  | \/\     /  / __ \|  | \/ /_/ |
      | \______  /__|  (____  /   __/|___|  /\__   |____/  \___  / \____/|__|    \/\_/  (____  /__|  \____ |
      |        \/           \/|__|        \/    |__|           \/                            \/           \/
      |""".stripMargin)

  if (Config.enableServiceRegister()) {
    Try(serviceProvider.register(Schema.HTTP -> Seq(restUriPattern1, restUriPattern2))) match {
      case Success(_) =>
        println("Service register success")
      case Failure(exception) => println(s"Service register failure: ${exception.getLocalizedMessage}")
    }
  } else {
    println("disabled Service register")

  }

  StdIn.readLine() // let it run until user presses return

  bindingFuture.flatMap(_.unbind()).onComplete { _ =>
    if (Config.enableServiceRegister()) {
      serviceProvider.deregister()
    }
    system.terminate()
  }
}
