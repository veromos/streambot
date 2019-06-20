package fr.esgi.streambot

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
import akka.stream.alpakka.slick.scaladsl._
import akka.stream.scaladsl._
import slick.jdbc.GetResult

import scala.concurrent.Future
import scala.io.StdIn

object WebServer {

  // needed to run the route
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  // needed for the future map/flatmap in the end and future in fetchItem and saveOrder
  implicit val executionContext = system.dispatcher

  case class User(id: Int, username: String, isSub: Boolean, isBlacklist: Boolean)
  case class Users(vec: Vector[User])

  case class Tip(id: Int, userID: Int, amount: Int)
  case class Tips(vec: Vector[Tip])

  case class Key(key: Int)
  case class Keys(vec: Vector[Key])

  implicit val userFormat = jsonFormat4(User)
  implicit val usersFormat = jsonFormat1(Users)

  implicit val tipFormat = jsonFormat3(Tip)
  implicit val tipsFormat = jsonFormat1(Tips)

  implicit val keyFormat = jsonFormat1(Key)
  implicit val keysFormat = jsonFormat1(Keys)

  def main(args: Array[String]) {

    implicit val session = SlickSession.forConfig("slick-sqlite")
    system.registerOnTermination(session.close())

    // This import brings everything you need into scope
    import session.profile.api._

    // Stream the results of a query
    val done: Future[Done] =
      Slick
        .source(TableQuery[Users].result)

        .runWith(Sink.ignore)

    /*
    val route: Route =
      get {
        pathPrefix("col") {
          val req = SQLiteHelpers.request(url, "SELECT * FROM user", Seq("key"))
          req match {
            case Some(r) => val users = r.flatMap(v => to[User].from(v))
              complete(users)
            case None => complete("mauvaise table")
          }
        }
        /*pathPrefix("sub") {
          complete("sub")
        }
        pathPrefix("giveaway") {
          complete("giveaway")
        }
        pathPrefix("blacklist") {
          complete("blacklist")
        }
        pathPrefix("poll") {
          complete("poll")
        }*/
      }
      */
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
