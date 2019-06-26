package models

import slick.driver.SQLiteDriver.api._

case class Tips(tag: Tag) extends Table[(Int, Double, Int)](tag, "tips") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def amount = column[Double]("amount")
  def userId = column[Int]("user_id")
  def * = (id, amount, userId)

  // A reified foreign key relation that can be navigated to create a join
  def user = foreignKey("user_fk", userId, TableQuery[Users])(_.id)
}
