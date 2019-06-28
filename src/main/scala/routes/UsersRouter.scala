package routes

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import models._
import slick.driver.SQLiteDriver.api._
import scala.util.{Failure, Success}
import spray.json._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

object UsersRouter extends JsonSupport {

  val db = Database.forConfig("sqlite")
  val users = TableQuery[Users]
  val subs = users.filter(_.isSub === 1)

  def blacklistAction(userId: Rep[Int]) = users.filter(_.id === userId).map(_.isBlacklist).update(1)

  def createUser(id: Int, username: String, isSub: Int, isBlacklist: Int) = {
    Await.result(db.run(users returning users.map(_.id) += (0, username, 0, 0)), Duration.Inf)

    //db.run(users returning users.map(_.id) += (0, username, 0, 0))
  }

  val route: Route =
    get {
      path("users") {
        onComplete(db.run(users.result)) {
          case Success(value) => complete(value)
          case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
        }
      } ~
      path("users" / "subs") {
        onComplete(db.run(subs.result)) {
          case Success(value) => complete(value)
          case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
        }
      } ~
      path("users" / "blacklist" / IntNumber ) { id => {
        onComplete(db.run(blacklistAction(id))) {
            case Success(value) => complete(s"user is blacklist")
            case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
          }
        }
      }
    } ~
    post {
      entity(as[User]) { user =>
        complete(createUser(0, user.username, 0, 0).toJson)
      }
    }
}