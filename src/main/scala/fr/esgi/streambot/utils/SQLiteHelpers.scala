package fr.esgi.streambot.utils

import java.sql.{Connection, DriverManager, ResultSet}

import scala.util.{Failure, Success, Try}

object SQLiteHelpers{

  /***
    * Request your database
    * @param url URL to yours SQLite sever (file)
    * @param sql SQL request
    * @param cols Sequence of string containing the name of all the columns
    * @param driver Driver (default: "org.sqlite.JDBC")
    * @return
    */
  def request(url: String,
              sql: String,
              cols: Seq[String],
              driver: String = "org.sqlite.JDBC"): Option[Vector[Map[String, Object]]] = {
    Class.forName(driver)
    val conn: Connection = DriverManager.getConnection(url)
    val statement = conn.createStatement()

    val result: Option[ResultSet] = Try {
      statement.executeQuery(sql)
    } match {
      case Success(output) => Some(output)
      case Failure(_) => None
    }

    val resultFormat = result match{
      case Some(rs) => Some(Iterator.continually(buildMap(rs, cols)).takeWhile(!_.isEmpty).map(_.get).toVector)
      case None => None
    }

    if(conn != null) conn.close()
    resultFormat
  }

  private def buildMap(queryResult: ResultSet, colNames: Seq[String]): Option[Map[String, Object]] =
    if (queryResult.next()){
      Some(colNames.map(n => n -> queryResult.getObject(n)).toMap)
    }
    else{
      None
    }
}
