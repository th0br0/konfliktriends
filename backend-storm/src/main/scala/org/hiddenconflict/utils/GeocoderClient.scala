package org.hiddenconflict.utils

import twitter4j.GeoLocation

/**
 * @author Andreas C. Osowski
 */
sealed trait Geocoder {
  def geocodeAddress(address: String): Option[GeoLocation]
}

trait GoogleGeocoder extends Geocoder {
  private[this] val apiKey = "AIzaSyAKwmiHBkXg0CD4idMqbLhDBn5rh90SJFw"
  private[this] lazy val context = 0
  def geocodeAddress(address: String) = None

}
