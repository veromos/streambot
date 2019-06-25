import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.Done
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import slick.basic.DatabaseConfig
import spray.json.DefaultJsonProtocol._
import utils.SQLiteHelpers
import utils.FromMap.to
import akka.stream.scaladsl._
import models._
import slick.jdbc.GetResult

import scala.concurrent.Future
import scala.io.StdIn
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.SQLiteDriver.api._

object WebServer {

  // needed to run the route
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  // needed for the future map/flatmap in the end and future in fetchItem and saveOrder
  implicit val executionContext = system.dispatcher

  def main(args: Array[String]) {


    /*val users = TableQuery[Users]

    val db = Database.forConfig("sqlite")
    try {
      val setup = DBIO.seq(
        (users.schema).create
      )
      val setupFuture = db.run(setup)
      println("Done")

      val q1 = for(m <- users) yield m.username
      db.stream(q1.result).foreach(println)
      val insert = DBIO.seq(
        users += (1, "Foo", 0, 0)
      )
      val insertFuture = db.run(insert)
      insertFuture.onComplete( _ => db.stream(q1.result).foreach(println))
    } finally db.close*/

    /*val route: Route =
      get {
        pathPrefix("col") {
          val req = SQLiteHelpers.request(url, "SELECT * FROM user", Seq("key"))
          req match {
            case Some(r) => val users = r.flatMap(v => to[User].from(v))
              complete(users)
            case None => complete("mauvaise table")
          }
        }
      }*/
    /*
    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ ⇒ system.terminate()) // and shutdown when done
      */
  }
}