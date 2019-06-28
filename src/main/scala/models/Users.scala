package models

import slick.driver.SQLiteDriver.api._

final case class Users(tag: Tag) extends Table[(Int, String, Int, Int)](tag, "users") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc) // This is the primary key column
  def username = column[String]("username")
  def isSub = column[Int]("is_sub")
  def isBlacklist = column[Int]("is_blacklist")
  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id, username, isSub, isBlacklist)
}

case class User(
  id: Int,
  username: String,
  isSub: Int,
  isBlacklist: Int)

object User {
  val users: TableQuery[Users] = TableQuery[Users]
}