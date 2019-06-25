import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.Done
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import slick.basic.DatabaseConfig
import spray.json.DefaultJsonProtocol._
import models._
import scala.io.StdIn
import slick.driver.SQLiteDriver.api._

import scala.util.{Failure, Success}

object WebServer {

  // needed to run the route
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  // needed for the future map/flatmap in the end and future in fetchItem and saveOrder
  implicit val executionContext = system.dispatcher

  def main(args: Array[String]) {

    val users = TableQuery[Users]
    val tips = TableQuery[Tips]

    val tipsUsersQuery: Query[Rep[String], String, Seq] = for {
      c <- tips
      u <- c.user
    } yield u.username

    val db = Database.forConfig("sqlite")
    val route: Route =
      get {
        pathPrefix("users") {
          onComplete(db.run(users.map(_.username).result)) {
            case Success(value) => complete(value)
            case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
          }
        }
        pathPrefix("tips/users") {
          onComplete(db.run(tipsUsersQuery.result)) {
            case Success(value) => complete(value)
            case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
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