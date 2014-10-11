package org.hiddenconflict.utils

import com.google.maps.{ GeocodingApi, GeoApiContext }
import org.hiddenconflict.bolt.GeoCoordinate

import scala.util.{ Failure, Success, Try }

/**
 * @author Andreas C. Osowski
 */
sealed trait Geocoder {
  def geocodeAddress(address: String): Option[GeoCoordinate]
}

trait GoogleGeocoder extends Geocoder {
  private[this] val apiKey = "AIzaSyAKwmiHBkXg0CD4idMqbLhDBn5rh90SJFw"
  private[this] var context: GeoApiContext = null

  def initGeocoder = context = new GeoApiContext().setApiKey("AIzaSyAKwmiHBkXg0CD4idMqbLhDBn5rh90SJFw")
    .setQueryRateLimit(5)

  def geocodeAddress(address: String) =
    Try(GeocodingApi.newRequest(context).address(address).await) match {
      case Success(arr) if arr.length > 0 => Some(arr(0).geometry.location).map(l => GeoCoordinate(l.lat, l.lng))
      case Success(_) => None
      case Failure(ex) => None
    }

}
