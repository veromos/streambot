package routes

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import models._
import slick.driver.SQLiteDriver.api._
import scala.util.{Failure, Success}

object SurveysRouter {

  val db = Database.forConfig("sqlite")
  val surveys = TableQuery[Surveys]
  val surveyDetails = TableQuery[SurveyDetails]

  val route: Route =
    get {
      path("surveys") {
        onComplete(db.run(surveys.result)) {
          case Success(value) => complete(value)
          case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
        }
      } ~
      path("surveys" / IntNumber) { id => {
          onComplete(db.run(surveys.filter(_.id === id).result)) {
            case Success(value) => complete(value)
            case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
          }
        }
      }
    }
}
