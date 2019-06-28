package routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import models._
import services.HttpService._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes._
import slick.driver.SQLiteDriver.api._
import scala.util.{Failure, Success}

object TipsRouter extends JsonSupport {

  val db = Database.forConfig("sqlite")
  val tips = TableQuery[Tips]
  val users = TableQuery[Users]

  def createTip(amount: Double, userId: Int) = {

    val addTip = tips returning tips.map(_.id) += (0, amount, userId)
    val composedAction = addTip.flatMap { id => tips.filter(_.id === id).result.headOption }
    db.run(composedAction)
  }

  // get list of user who give tips
  val tipsUser = for {
    t <- tips
    u <- t.user
  } yield u


  // get tips total amount
  val tipsTotal = tips.map(_.amount).sum


  // get users with tips amount
  val tipsAmountByUsers = (for {
    t <- tips
    u <- t.user
  } yield (u, t)).groupBy(_._1)
    .map { case (u, t) =>
      (u, t.map(_._2.amount).sum)
    }

  def tipsAmountByUserId(userId: Rep[Int]) =
    tips.filter(_.userId === userId).map(_.amount).sum

  // get tips amount by user Id
  val tipsAmountByUserIdCompiled = Compiled(tipsAmountByUserId _)

  val route: Route =
    get {
      path("tips") {
        onComplete(db.run(tips.result)) {
          case Success(value) => complete(value.map(e => (Tip.apply _) tupled e))
          case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
        }
      } ~
      path("tips" / "users") {
        onComplete(db.run(tipsUser.distinct.result)) {
          case Success(value) => complete(value.map(e => (User.apply _) tupled e))
          case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
        }
      } ~
      path("tips" / "users" / "amount" / IntNumber) {
        id => {
          onComplete(db.run(tipsAmountByUserIdCompiled(id).result)) {
            case Success(value) => complete("{\"id\": " + id + ", \"tips\": " + value.getOrElse(false) + "}")
            case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
          }
        }
      } ~
      path("tips" / "users" / "amount") {
        onComplete(db.run(tipsAmountByUsers.result)) {
          case Success(value) => complete(value.map(e => (UserTips.apply _) tupled(e._1._1, e._1._2, e._1._3, e._1._4, e._2.get)))
          case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
        }
      } ~
      path("tips" / "total") {
        onComplete(db.run(tipsTotal.result)) {
          case Success(value) => complete("{\"total\": " + value.getOrElse(-1) + "}")
          case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
        }
      }
    } ~
    post {
      path("tips") {
        entity(as[Tip]) { tip =>
          complete(Created, createTip(tip.amount, tip.userId).map(e => (Tip.apply _) tupled e.get))
        }
      }
    }
}
