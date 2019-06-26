package routes

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import models._
import slick.driver.SQLiteDriver.api._
import scala.util.{Failure, Success}

object UsersRouter {

  val db = Database.forConfig("sqlite")
  val users = TableQuery[Users]

  val route: Route =
    get {
      pathPrefix("users") {
        onComplete(db.run(users.result)) {
          case Success(value) => complete(value)
          case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
        }
      }
    }
}