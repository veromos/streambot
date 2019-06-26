package routes

import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import models._
import slick.driver.SQLiteDriver.api._
import scala.util.{Failure, Success}

object TipsRouter {

  val db = Database.forConfig("sqlite")
  val tips = TableQuery[Tips]
  val users = TableQuery[Users]


  // get list of user who give tips
  val tipsUser: Query[Rep[String], String, Seq] = for {
    t <- tips
    u <- t.user
  } yield u.username


  // get tips total amount
  val tipsTotal = tips.map(_.amount).sum


  /* get list of users with tips amount
  val tipsAmountByUsers = for {
    t <- tips
    u <- t.user
  } yield (u.username, t.amount).groupBy(t.userId)*/


  // get tips amount by user Id
  val tipsAmountByUserIdCompiled = Compiled(tipsAmountByUserId _)

  def tipsAmountByUserId(userId: Rep[Int]) =
    tips.filter(_.userId === userId).map(_.amount).sum


  val route: Route =
    get {
      path("tips" / "users") {
        onComplete(db.run(tipsUser.distinct.result)) {
          case Success(value) => complete(value)
          case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
        }
      } ~
      path("tips" / "total") {
        onComplete(db.run(tipsTotal.result)) {
          case Success(value) => complete(s"${value.getOrElse(0)}")
          case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
        }
      } ~
      path("tips" / "user" / IntNumber ) { id => {
            onComplete(db.run(tipsAmountByUserIdCompiled(id).result)) {
            case Success(value) => complete(s"${value.getOrElse(0)}")
            case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
          }
        }
      }
    }
}
