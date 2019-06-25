import models._

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import slick.basic.DatabasePublisher
import slick.jdbc.SQLiteProfile.api._

// The main application
object HelloSlick extends App {

  val users = TableQuery[Users]
  val tips = TableQuery[Tips]

  val db = Database.forConfig("sqlite")
  try {
    // The query interface for the Users table
    val users: TableQuery[Users] = TableQuery[Users]

    // the query interface for the Tips table
    val tips: TableQuery[Tips] = TableQuery[Tips]

    val setupAction: DBIO[Unit] = DBIO.seq(
      // Create the schema by combining the DDLs for the Users and Tips
      // tables using the query interfaces
      (users.schema ++ tips.schema).create,

      // Insert some users
      users += (1, "Chris", 0, 0),
      users += (2, "Alex", 1, 0),
      users += (3, "Iliasse", 0, 1)
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
          println(s"Inserted $numRows rows into the Coffees table")
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

      /* Streaming */

      val userNamesAction: StreamingDBIO[Seq[String], String] = users.map(_.username).result

      val userNamesPublisher: DatabasePublisher[String] = db.stream(userNamesAction)

      userNamesPublisher.foreach(println)

    }.flatMap { _ =>

      /* Filtering / Where */

      // Construct a query where the price of Coffees is > 9.0
      val filterQuery: Query[Tips, (Int, Double, Int), Seq] =
        tips.filter(_.amount > 0.99)

      // Print the SQL for the filter query
      println("Generated SQL for filter query:\n" + filterQuery.result.statements)

      // Execute the query and print the Seq of results
      db.run(filterQuery.result.map(println))

    }.flatMap { _ =>

      /* Update */

      // Construct an update query with the sales column is_blacklist the one to update
      val updateQuery: Query[Rep[Int], Int, Seq] = users.map(_.isBlacklist)

      val updateAction: DBIO[Int] = updateQuery.update(1)

      // Print the SQL for the Users update query
      println("Generated SQL for Coffees update:\n" + updateQuery.updateStatement)

      // Perform the update
      db.run(updateAction.map { numUpdatedRows =>
        println(s"Updated $numUpdatedRows rows")
      })

    }.flatMap { _ =>

      /* Delete */

      // Construct a delete query that deletes tips with a price less than 8.0
      val deleteQuery: Query[Tips,(Int, Double, Int), Seq] =
        tips.filter(_.userId < 1)

      val deleteAction = deleteQuery.delete

      // Print the SQL for the Coffees delete query
      println("Generated SQL for Coffees delete:\n" + deleteAction.statements)

      // Perform the delete
      db.run(deleteAction).map { numDeletedRows =>
        println(s"Deleted $numDeletedRows rows")
      }

    }.flatMap { _ =>

      /* Sorting / Order By */

      val sortByPriceQuery: Query[Tips, (Int, Double, Int), Seq] =
        tips.sortBy(_.amount)

      println("Generated SQL for query sorted by price:\n" +
        sortByPriceQuery.result.statements)

      // Execute the query
      db.run(sortByPriceQuery.result).map(println)

    }.flatMap { _ =>

      /* Query Composition */

      val composedQuery: Query[Rep[String], String, Seq] =
        users.sortBy(_.username).take(3).filter(_.isBlacklist > 0).map(_.username)

      println("Generated SQL for composed query:\n" +
        composedQuery.result.statements)

      // Execute the composed query
      db.run(composedQuery.result).map(println)

    }.flatMap { _ =>

      /* Joins */

      // Join the tables using the relationship defined in the Coffees table
      val joinQuery: Query[(Rep[Double], Rep[String]), (Double, String), Seq] = for {
        c <- tips if c.amount > 1.0
        u <- c.user
      } yield (c.amount, u.username)

      println("Generated SQL for the join query:\n" + joinQuery.result.statements)

      // Print the rows which contain the coffee name and the supplier name
      db.run(joinQuery.result).map(println)

    }.flatMap { _ =>

      /* Computed Values */

      //#maxPrice
      // Create a new scalar value that calculates the maximum price
      val maxPrice: Rep[Option[Double]] = tips.map(_.amount).max
      //#maxPrice

      println("Generated SQL for max price column:\n" + maxPrice.result.statements)

      // Execute the computed value query
      db.run(maxPrice.result).map(println)

    }
    Await.result(f, Duration.Inf)

  } finally db.close
}