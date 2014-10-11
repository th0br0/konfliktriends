package org.hiddenconflict.db

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.lifted.{ TableQuery, Tag }

/**
 * @author Andreas C. Osowski
 */

object Table {
  val GeocodeResults = TableQuery[GeocodeResults]
}

trait Db {
  // XXX - will break.
  def withDb[T](fun: Session => T) = Database.forURL("jdbc:postgresql://172.17.0.3/konfliktriend?user=test&password=pass", driver = "org.postgresql.Driver") withSession (fun)
}

case class GeocodeResult(address: String, lat: Double, lon: Double)

class GeocodeResults(tag: Tag) extends Table[GeocodeResult](tag, "GeocodeResult") {
  def address = column[String]("address", O.PrimaryKey, O.NotNull)
  def lat = column[Double]("lat", O.NotNull)
  def lon = column[Double]("lon", O.NotNull)

  def * = (address, lat, lon) <> (GeocodeResult.tupled, GeocodeResult.unapply)
}