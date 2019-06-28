package routes

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import models._
import slick.driver.SQLiteDriver.api._
import spray.json._
import services.HttpService._

import scala.concurrent.Future
import scala.util.{Failure, Success}

object GiveawaysRouter extends JsonSupport {

  val db = Database.forConfig("sqlite")
  val giveaways = TableQuery[Giveaways]
  val giveawayDetails = TableQuery[GiveawayDetails]
  val tips = TableQuery[Tips]
  val users = TableQuery[Users]

  def createGiveaway(name: String) = {
    val addGa = giveaways returning giveaways.map(_.id) += (0, name, 0)
    val composedAction = addGa.flatMap { id => giveaways.filter(_.id === id).result.headOption }
    db.run(composedAction)
  }

  def giveawayPickWinner(giveawayId: Int) = {
    val randomFunction = SimpleFunction.nullary[Double]("random")

    val query = for {
      g <- giveaways if g.id === giveawayId
      gd <- giveawayDetails if gd.giveawayId === g.id
      t <- tips if t.userId === gd.userId
      u <- users if u.id === gd.userId
    } yield (g, gd, t, u)

    query
      .filter(_._4.isBlacklist === 0)
      .sortBy(_ => randomFunction)
      .take(1)
      .result
  }


  val route: Route =
    get {
      path("giveaways") {
        onComplete(db.run(giveaways.result)) {
          case Success(value) => complete(value)
          case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
        }
      } ~
      path("giveaways" / "subscribe" / IntNumber / "userId" / IntNumber) { (giveawayId, userId) => {
        // if giveaways exists && user exists then subscribe
        onComplete(db.run(giveawayDetails += (0, giveawayId, userId))) {
          case Success(value) => complete(s"$value")
          case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
          }
        }
      } ~
      path("giveaways" / IntNumber / "pickWinner") { id =>
        onComplete(db.run(giveawayPickWinner(id))) {
          case Success(value) => complete(s"$value")
          case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
        }
      }
    } ~
    post {
      path("giveaways") {
        entity(as[Giveaway]) { g =>
          complete(Created, createGiveaway(g.name).map(e => (Giveaway.apply _) tupled e.get))
        }
      }
    }
}