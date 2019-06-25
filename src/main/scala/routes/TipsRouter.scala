package routes

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import models._
import slick.driver.SQLiteDriver.api._
import scala.util.{Failure, Success}

object TipsRouter {

  val db = Database.forConfig("sqlite")
  val tips = TableQuery[Tips]
  val tipsUser: Query[Rep[String], String, Seq] = for {
    c <- tips
    u <- c.user
  } yield u.username

  val tipsTotal = tips.map(_.amount).sum

  val route: Route =
    get {
      pathPrefix("tips" / "users") {
        onComplete(db.run(tipsUser.distinct.result)) {
          case Success(value) => complete(value)
          case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
        }
      } ~
      pathPrefix("tips" / "total") {
        onComplete(db.run(tipsTotal.result)) {
          case Success(value) => complete(s"${value.getOrElse(0)}")
          case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
        }
      }
    }
}
