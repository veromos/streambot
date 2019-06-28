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

  /*def surveysResult(id: Int) = {
    val a = surveyDetails.filter(_.id === id).result.map(_.answ)

      .filter(_.answerId === "1").result.
    val b = surveyDetails.filter(_.id === id).filter(_.answerId === "2").result.map(_.length)

    val total = a.toInt + b.toInt
    if (total == 0)
      (0, 0)
    else
      (a / total, b / total)
  }*/

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
      } ~
      path("surveys" / IntNumber / "answer" / IntNumber / "userId" / IntNumber) {
        (surveyId, answerId, userId) => {
          // if giveaways exists && user exists then subscribe
          onComplete(db.run(surveyDetails += (0, surveyId, userId, answerId))) {
            case Success(value) => complete(s"$value")
            case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
          }
        }
      }
      /*path("surveys" / IntNumber / "result") {
        id => {
          onComplete(db.run(surveyDetails.filter(_.id === id).filter(_.answerId === "1"))) {
            case Success(value) =>
              onComplete(surveyDetails.filter(_.id === id).filter(_.answerId === "1").length.result)) {
                case Success(value) => complete(value)
                case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
            }
            case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
          }
        }
      }*/
    }
}
