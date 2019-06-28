package models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonSupport extends DefaultJsonProtocol with SprayJsonSupport {

  implicit val userFormat = jsonFormat4(User.apply)
  implicit val userTipsFormat = jsonFormat5(UserTips.apply)
  implicit val tipFormat = jsonFormat3(Tip.apply)
  implicit val surveyFormat = jsonFormat4(Survey.apply)
  implicit val giveawayFormat = jsonFormat3(Giveaway.apply)
}