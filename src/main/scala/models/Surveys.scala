package models

import slick.driver.SQLiteDriver.api._

case class Surveys(tag: Tag) extends Table[(Int, String, String, String)](tag, "surveys") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def question = column[String]("survey_name")
  def answer_1 = column[String]("answer_1")
  def answer_2 = column[String]("answer_2")
  def * = (id, question, answer_1, answer_2)
}

case class SurveyDetails(tag: Tag) extends Table[(Int, Int, Int, Int)](tag, "survey_details") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def surveyId = column[Int]("survey_id")
  def userId = column[Int]("user_id")
  def answerId = column[Int]("answer_id")
  def * = (id, surveyId, userId, answerId)

  def survey = foreignKey("survey_fk", surveyId, TableQuery[Surveys])(_.id)
  def user = foreignKey("user_fk", userId, TableQuery[Users])(_.id)
}
