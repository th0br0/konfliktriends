package org.konfliktriends.bolt

import java.util

import backtype.storm.task.{ OutputCollector, TopologyContext }
import backtype.storm.tuple.Tuple
import com.google.maps.{ GeocodingApi, GeoApiContext }
import org.apache.log4j.Logger
import org.konfliktriends.db.{ GeocodeResult, Table, Db }
import org.konfliktriends.utils.{ GoogleGeocoder, TwitterClient }
import storm.scala.dsl.StormBolt
import scala.collection.JavaConverters._
import scala.slick.driver.PostgresDriver.simple._
import scala.util.{ Failure, Success, Try }

/**
 * @author Andreas C. Osowski
 */
class GeocodeStatusBolt extends StormBolt(List("status")) with TwitterClient with Db with GoogleGeocoder {
  var logger: Logger = null

  override def prepare(conf: util.Map[_, _], context: TopologyContext, collector: OutputCollector): Unit = {
    logger = Logger.getLogger(getClass)
    super.prepare(conf, context, collector)

    initGeocoder()
  }

  def isGeocodableAddress(in: String) =
    !in.contains("@")

  def geocodeWithDb(address: String): Option[GeoCoordinate] = withDb { implicit session =>
    // Query db, return result if present.
    // XXX - chain these as separate functions...
    val inDb = Table.GeocodeResults.filter(r => r.address === address).firstOption
      .map(result => GeoCoordinate(result.lat, result.lon))
    if (!inDb.isEmpty) return inDb

    val result = geocodeAddress(address)
    logger.info(s"Geocoding: $address Result: $result")
    result match {
      case Some(res) => Table.GeocodeResults.map(p => p).insert(GeocodeResult(address, res.lat, res.lng))
      case None =>
    }

    result
  }

  override def execute(input: Tuple): Unit = {
    val status = input.getValueByField("status").asInstanceOf[StatusContent]
    logger.info("Incoming status: " + status)

    if (status.location.isEmpty) return

    val userLocation = status.location.get match {
      case Left(l) => Some(l)
      case Right(address) if isGeocodableAddress(address) => geocodeWithDb(address)
      case Right(_) => None

    }

    logger.info("Traced userLoc: " + userLocation)

    if (userLocation.isEmpty) return

    val (mentionIds, mentionGeo) = {
      val rights = status.mentions.filter(_.isRight)
      val lefts = status.mentions.filterNot(rights.contains)

      (lefts.map(_.left.get), rights.map(_.right.get))
    }

    val mentionUsers = twitter.lookupUsers(mentionIds.toArray).asScala.filterNot(_ == null)
      .filter(t => isGeocodableAddress(t.getLocation))
      .toSeq

    logger.info("mentionUsers: " + mentionUsers.map(u => (u.getScreenName, u.getLocation)))
    if (mentionUsers.isEmpty) return

    // Loosing user here because we're not really interested in them for now, not storing it anyway.
    // Also, geocodeAddress(String) is storing the result in the db.
    val locations = mentionUsers.map(t => geocodeWithDb(t.getLocation)).filterNot(_.isEmpty).map(_.get) ++ mentionGeo

    if (!locations.isEmpty)
      using anchor input emit status.copy(
        location = Some(Left(userLocation.get)),
        mentions = locations.map(c => Right(c))
      )
    else logger.info("Dropping for no mention loc found: " + status)

  }
}
