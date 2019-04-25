import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.Done
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import utils.SQLiteHelpers
import utils.FromMap.to

import scala.io.StdIn

object WebServer {

  // needed to run the route
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  // needed for the future map/flatmap in the end and future in fetchItem and saveOrder
  implicit val executionContext = system.dispatcher

  case class Key(key: Int)
  case class Keys(vec: Vector[Key])
  implicit val keyFormat = jsonFormat1(Key)
  implicit val keysFormat = jsonFormat1(Keys)

  // To run your application, use the following command : sbt "run path/to/the/correction/src/main/resources/test.db"
  // Currently, test.db has only one table "table_test" with one column "key" (type integer)
  def main(args: Array[String]) {

    val url = s"""jdbc:sqlite:${args(0)}"""

    val route: Route =
      get {
        pathPrefix("col") {
          val req = SQLiteHelpers.request(url, "SELECT * FROM table_test", Seq("key"))
          req match {
            case Some(r) => val values = r.flatMap(v => to[Key].from(v))
              complete(values)
            case None => complete("mauvaise table")
          }
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ ⇒ system.terminate()) // and shutdown when done

  }
}