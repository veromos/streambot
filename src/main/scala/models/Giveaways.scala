package models

import slick.driver.SQLiteDriver.api._

case class Giveaways(tag: Tag) extends Table[(Int, String, Int)](tag, "giveaways") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("giveaway_name")
  def winnerId = column[Int]("winner_id")
  def * = (id, name, winnerId)
}

case class GiveawayDetails(tag: Tag) extends Table[(Int, Int, Int)](tag, "giveaway_details") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def giveawayId = column[Int]("giveaway_id")
  def userId = column[Int]("user_id")
  def * = (id, giveawayId, userId)

  def giveaway = foreignKey("giveaway_fk", giveawayId, TableQuery[Giveaways])(_.id)
  def user = foreignKey("user_fk", userId, TableQuery[Users])(_.id)
}

case class Giveaway(id: Int, name: String, winnerId: Int)

case class GiveDetail(id: Int, giveawayId: Int, userId: Int)

case class FullClass(giveaway: Giveaway, giveawayDetail: GiveDetail, tips: Tip, user: User)