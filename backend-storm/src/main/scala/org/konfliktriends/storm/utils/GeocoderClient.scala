package org.konfliktriends.utils

import com.google.maps.{ GeocodingApi, GeoApiContext }
import org.apache.log4j.Logger
import org.konfliktriends.bolt.GeoCoordinate

import scala.util.{ Failure, Success, Try }

import scala.collection.JavaConverters._

/**
 * @author Andreas C. Osowski
 */
sealed trait Geocoder {
  def geocodeAddress(address: String): Option[GeoCoordinate]
}

trait GoogleGeocoder extends Geocoder {
  def logger: Logger
  private[this] val apiKey = "AIzaSyAKwmiHBkXg0CD4idMqbLhDBn5rh90SJFw"
  private[this] var context: GeoApiContext = null

  def initGeocoder() = context = new GeoApiContext().setApiKey("AIzaSyAKwmiHBkXg0CD4idMqbLhDBn5rh90SJFw")
    .setQueryRateLimit(5)

  def geocodeAddress(address: String) =
    Try(GeocodingApi.newRequest(context).address(address).await) match {
      case Success(arr) if arr.length > 0 => Some(arr(0).geometry.location).map(l => GeoCoordinate(l.lat, l.lng))
      case Success(a) =>
        logger.warn(s"Geocode Success but ? ${a.toList}"); None
      case Failure(ex) => logger.error(ex); None
    }

}
