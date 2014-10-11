package org.hiddenconflict.bolt

import java.util

import backtype.storm.task.{OutputCollector, TopologyContext}
import backtype.storm.tuple.Tuple
import com.google.maps.{GeocodingApi, GeoApiContext}
import org.hiddenconflict.utils.TwitterClient
import storm.scala.dsl.StormBolt
import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

/**
 * @author Andreas C. Osowski
 */
class GeocodeStatusBolt extends StormBolt(List("statusGeo")) with TwitterClient {
  var geoContext: GeoApiContext = null

  override def prepare(conf: util.Map[_, _], context: TopologyContext, collector: OutputCollector): Unit = {
    super.prepare(conf, context, collector)

    geoContext = new GeoApiContext().setApiKey("AIzaSyAKwmiHBkXg0CD4idMqbLhDBn5rh90SJFw")
      .setQueryRateLimit(5)
  }

  def isGeocodableAddress(in: String) =
    !in.contains("@")

  def geocodeAddress(address: String): Option[GeoCoordinate] = {
    // Query db, return result if present.

    val req = GeocodingApi.newRequest(geoContext).address(address)
    val result = Try(req.await()) match {
      case Success(array) => Some(array(0).geometry.location).map(l => GeoCoordinate(l.lat, l.lng))
      case Failure(ex) => None
    }

    if(!result.isEmpty) {
      // persist to db
    }

    result
  }

  override def execute(input: Tuple): Unit = {
    val status = input.getValueByField("status").asInstanceOf[StatusContent]
    if (status.location.isEmpty) return

    val userLocation = status.location.get match {
      case Left(l) => Some(l)
      case Right(address) if isGeocodableAddress(address) => geocodeAddress(address)
      case Right(_) => None

    }
    if (userLocation.isEmpty) return

    val (mentionIds, mentionGeo) = {
      val rights = status.mentions.filter(_.isRight)
      val lefts = status.mentions.filterNot(rights.contains)

      (lefts.map(_.left.get), rights.map(_.right.get))
    }

    val mentionUsers = twitter.lookupUsers(mentionIds.toArray).asScala.filterNot(_ == null)
      .map(u => (u, u.getLocation()))
      .filter(t => isGeocodableAddress(t._2))

    if (mentionUsers.isEmpty) return

    // Loosing user here because we're not really interested in them for now, not storing it anyway.
    // Also, geocodeAddress(String) is storing the result in the db.
    val locations = mentionUsers.map(t => geocodeAddress(t._2)).filterNot(_.isEmpty).map(_.get) ++ mentionGeo

    using anchor input emit status.copy(
      location = Some(Left(userLocation.get)),
      mentions = locations.map(c => Right(c))
    )




  }
}
