package routes

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import models._
import scala.util.{Failure, Success}
import slick.jdbc.SQLiteProfile.api._
import spray.json._

object SurveysRouter extends JsonSupport {

  val db = Database.forConfig("sqlite")
  val surveys = TableQuery[Surveys]
  val surveyDetails = TableQuery[SurveyDetails]

  def answersCount(surveyId: Int, answerId: Int) =
    db.run(
      surveyDetails
      .filter(_.surveyId === surveyId)
      .filter(_.answerId === answerId)
      .length
      .result
    )

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
      } ~
      path("surveys" / IntNumber / "result") {
        id => {
          onComplete(answersCount(id, 1)) {
            case Success(answerOneCount) => {
              onComplete(answersCount(id, 2)) {
                case Success(answerTwoCount) =>
                  val floatOne = answerOneCount.toFloat
                  val floatTwo = answerTwoCount.toFloat
                  if (answerOneCount + answerTwoCount == 0)
                    complete(0, 0)
                  else
                    complete(floatOne / (floatOne + floatTwo) * 100, floatTwo / (floatOne + floatTwo) * 100)
                case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
              }
            }
            case Failure(ex) => complete((500, s"An error occured: ${ex.getMessage}"))
          }
        }
      }
    }
}
