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

case class UserTips(
                 id: Int,
                 username: String,
                 isSub: Int,
                 isBlacklist: Int,
                 Tips: Double)

object User {
  val users: TableQuery[Users] = TableQuery[Users]
}

object UserTips {
  val users: TableQuery[Users] = TableQuery[Users]
  val tips: TableQuery[Tips] = TableQuery[Tips]
}