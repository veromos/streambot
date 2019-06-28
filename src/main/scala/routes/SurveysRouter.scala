package routes

import models._
import services.HttpService._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes._
import routes.UsersRouter.{db, users}
import slick.driver.SQLiteDriver.api._
import scala.concurrent.Future
import scala.util.{Failure, Success}

object SurveysRouter extends JsonSupport {

  val db = Database.forConfig("sqlite")
  val surveys = TableQuery[Surveys]
  val surveyDetails = TableQuery[SurveyDetails]

  def createSurvey(question: String, answer_1: String, answer_2: String) = {
    val addS = surveys returning surveys.map(_.id) += (0, question, answer_1, answer_2)
    val composedAction = addS.flatMap { id => surveys.filter(_.id === id).result.headOption}
    db.run(composedAction)
  }

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
    } ~
    post {
      path("surveys") {
        entity(as[Survey]) { s =>
          complete(Created, createSurvey(s.question, s.answer_1, s.answer_2).map(e => (Survey.apply _) tupled e.get))
        }
      }
    }
}
