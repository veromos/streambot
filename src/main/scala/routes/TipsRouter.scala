package routes

import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import models._
import slick.driver.SQLiteDriver.api._
import scala.util.{Failure, Success}
import spray.json._

object TipsRouter extends JsonSupport {

  val db = Database.forConfig("sqlite")
  val tips = TableQuery[Tips]
  val users = TableQuery[Users]

  def createTip(amount: Double, userId: Int) = db.run(tips += (0, amount, userId))

  // get list of user who give tips
  val tipsUser: Query[Rep[String], String, Seq] = for {
    t <- tips
    u <- t.user
  } yield u.username


  // get tips total amount
  val tipsTotal = tips.map(_.amount).sum


  // get users with tips amount
  val tipsAmountByUsers = (for {
    t <- tips
    u <- t.user
  } yield (u.username, t)).groupBy(_._1)
    .map { case (username, t) =>
    (username, t.map(_._2.amount).sum)
  }


  // get tips amount by user Id
  val tipsAmountByUserIdCompiled = Compiled(tipsAmountByUserId _)

  def tipsAmountByUserId(userId: Rep[Int]) =
    tips.filter(_.userId === userId).map(_.amount).sum


  val route: Route =
    get {
      path("tips") {
        onComplete(db.run(tips.result)) {
          case Success(value) => complete(value)
          case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
        }
      } ~
      path("tips" / "users") {
        onComplete(db.run(tipsUser.distinct.result)) {
          case Success(value) => complete(value)
          case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
        }
      } ~
      path("tips" / "users" / "amount" / IntNumber ) {
        id => {
          onComplete(db.run(tipsAmountByUserIdCompiled(id).result)) {
            case Success(value) => complete(s"${value.getOrElse(0)}")
            case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
          }
        }
      } ~
      path("tips" / "users" / "amount") {
        onComplete(db.run(tipsAmountByUsers.result)) {
          case Success(value) => complete(value)
          case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
        }
      } ~
      path("tips" / "total") {
        onComplete(db.run(tipsTotal.result)) {
          case Success(value) => complete(s"${value.getOrElse(0)}")
          case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
        }
      }/* ~
      post {
        entity(as[Tip]) { tip =>
          complete(createTip(tip.amount, tip.userId).toJson)
        }
      }*/
    }
}
