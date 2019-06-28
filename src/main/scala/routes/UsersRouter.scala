package routes

import models._
import services.HttpService._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import slick.driver.SQLiteDriver.api._
import akka.http.scaladsl.model.StatusCodes._

import scala.concurrent.Future
import scala.util.{Failure, Success}

object UsersRouter extends JsonSupport {

  val db = Database.forConfig("sqlite")
  val users = TableQuery[Users]
  val subs = users.filter(_.isSub === 1)

  def blacklistAction(userId: Rep[Int]) = db.run(users.filter(_.id === userId).map(_.isBlacklist).update(1))

  def createUser(id: Int, username: String, isSub: Int, isBlacklist: Int): Future[Option[(Int, String, Int, Int)]] = {
    val addUser = users returning users.map(_.id) += (0, username, 0, 0)
    val composedAction = addUser.flatMap { id => users.filter(_.id === id).result.headOption}
    db.run(composedAction)
  }

  val route: Route =
    get {
      path("users") {
        onComplete(db.run(users.result)) {
          case Success(value) => complete(value.map(e => (User.apply _) tupled e))
          case Failure(ex) => complete((InternalServerError, s"An error occured: ${ex.getMessage}"))
        }
      } ~
      path("users" / "subs") {
        onComplete(db.run(subs.result)) {
          case Success(value) => complete(value.map(e => (User.apply _) tupled e))
          case Failure(ex) => complete((InternalServerError, s"An error occured: ${ex.getMessage}"))
        }
      } ~
      path("users" / "blacklist" / IntNumber ) { id => {
        onComplete(blacklistAction(id)) {
            case Success(value) => complete("{\"id\": " + id + ", \"response\": \"user is blacklisted\"}")
            case Failure(ex) => complete((InternalServerError, s"An error occured: ${ex.getMessage}"))
          }
        }
      }
    } ~
    post {
      entity(as[User]) { user =>
        complete(Created, createUser(0, user.username, 0, 0).map(e => (User.apply _) tupled e.get))
      }
    }
}