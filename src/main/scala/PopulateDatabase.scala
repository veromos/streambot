import models._

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import slick.basic.DatabasePublisher
import slick.jdbc.SQLiteProfile.api._
import slick.jdbc.meta.MTable

// The main application
object PopulateDatabase extends App {

  val db = Database.forConfig("sqlite")

  try {
    /*def createTableIfNotExists(tables: TableQuery[_ <: Table[_]]*): Future[Seq[Unit]] = {
      Future.sequence(
        tables map { table =>
          db.run(MTable.getTables(table.baseTableRow.tableName)).flatMap { result =>
            if (result.isEmpty) {
              db.run(table.schema.create)
            } else {
              Future.successful(())
            }
          }
        }
      )
    }

    Await.result(createTableIfNotExists(users, tips), Duration.Inf)*/

    val users = TableQuery[Users]
    val tips = TableQuery[Tips]
    val surveys = TableQuery[Surveys]
    val surveyDetails = TableQuery[SurveyDetails]
    val giveaways = TableQuery[Giveaways]
    val giveawayDetails = TableQuery[GiveawayDetails]

    val setupAction: DBIO[Unit] = DBIO.seq(
      // Create the schema by combining the DDLs for the Users and Tips
      // tables using the query interfaces
      (users.schema
        ++ tips.schema
        ++ surveys.schema
        ++ surveyDetails.schema
        ++ giveaways.schema
        ++ giveawayDetails.schema
        ).create,

      // Insert some users
      users += (1, "Chris", 0, 0),
      users += (2, "Alex", 1, 0),
      users += (3, "Iliasse", 0, 1),
      giveaways += (1, "My first giveaway!", 0),
      giveaways += (2, "My second giveaway!", 0),
      giveawayDetails += (1, 1, 1),
      giveawayDetails += (2, 2, 2),
      surveys += (0, "What is the best language ever ?", "Scala", "Java"),
      surveyDetails += (0, 1, 1, 1),
      surveyDetails += (0, 1, 2, 2),
      surveyDetails += (0, 1, 3, 1)
    )

    val setupFuture: Future[Unit] = db.run(setupAction)
    val f = setupFuture.flatMap { _ =>

      //#insertAction
      // Insert some tips (using JDBC's batch insert feature)
      val insertAction: DBIO[Option[Int]] = tips ++= Seq (
        (1, 0.99, 1),
        (2, 0.99, 1),
        (3, 2.99, 1),
        (4, 1.99, 2),
        (5, 200, 1)
      )

      val insertAndPrintAction: DBIO[Unit] = insertAction.map { usersInsertResult =>
        // Print the number of rows inserted
        usersInsertResult foreach { numRows =>
          println(s"Inserted $numRows rows into the Tips table")
        }
      }
      //#insertAction

      val allUsersAction: DBIO[Seq[(Int, String, Int, Int)]] = users.result

      val combinedAction: DBIO[Seq[(Int, String, Int, Int)]] = insertAndPrintAction andThen allUsersAction

      val combinedFuture: Future[Seq[(Int, String, Int, Int)]] = db.run(combinedAction)

      combinedFuture.map { allUsers =>
        allUsers.foreach(println)
      }

    }.flatMap { _ =>

      val maxPrice: Rep[Option[Double]] = tips.map(_.amount).max

      db.run(maxPrice.result)

    }
    Await.result(f, Duration.Inf)

  } finally db.close
}