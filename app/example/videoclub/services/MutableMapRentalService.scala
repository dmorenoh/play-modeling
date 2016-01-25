package example.videoclub.services

import java.util.UUID
import java.time._


import scala.collection.mutable.{Map => MMap}
import scala.collection.mutable.{Set => MSet}
import scalaz._
import Scalaz._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
  * Dummy implementation with in-memory maps
  */
class MutableMapRentalService extends RentalService[ServiceResult, String, UUID, UUID, ZonedDateTime]{

  private val availableDvds: MMap[String, Set[UUID]] = MMap()
  private val dvdMovies: MMap[UUID, String] = MMap()
  private val rentedDvds: MSet[UUID] = MSet()


  override def addMovie(movie: String, qty: Int): ServiceResult[Set[UUID]] = for {
    dvds <- $ <~ List.fill(qty)(UUID.randomUUID()).toSet
    _ = availableDvds += movie -> (availableDvds.getOrElse(movie, Set()) ++ dvds)
    _ = dvdMovies ++= dvds.map(_ -> movie)
  } yield dvds

  override def rentDVD(customer: UUID, dvd: UUID, timestamp: ZonedDateTime): ServiceResult[Unit] = if(rentedDvds.contains(dvd)) {
    $ <~ -\/(s"$dvd is already rented")
  } else dvdMovies.get(dvd) match {
    case None => $ <~ -\/(s"$dvd is not in the inventory")
    case Some(movie) =>
      rentedDvds += dvd
      availableDvds += movie -> (availableDvds.getOrElse(movie, Set()) - dvd)
      $ <~ Unit
  }

  override def findDVD(movie: String): ServiceResult[Option[UUID]] = $ <~ availableDvds.get(movie).flatMap(_.headOption)

  override def returnDVD(customer: UUID, dvd: UUID, timestamp: ZonedDateTime): ServiceResult[Unit] = dvdMovies.get(dvd) match {
    case None => $ <~ -\/(s"$dvd is not in the inventory")
    case Some(movie) =>
      rentedDvds -= dvd
      availableDvds += movie -> (availableDvds.getOrElse(movie, Set()) + dvd)
      $ <~ Unit
  }
}
