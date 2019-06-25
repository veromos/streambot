package models

import slick.driver.SQLiteDriver.api._

class Users(tag: Tag) extends Table[(Int, String, Int, Int)](tag, "USERS") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc) // This is the primary key column
  def username = column[String]("username")
  def isSub = column[Int]("is_sub")
  def isBlacklist = column[Int]("is_blacklist")
  // Every table needs a * projection with the same type as the table's type parameter
  def * = (id, username, isSub, isBlacklist)
}

