package example.videoclub.services

import java.util.UUID
import java.time._


import example.videoclub.repository.DVDRepository

import scala.collection.mutable.{Map => MMap}
import scala.collection.mutable.{Set => MSet}
import scalaz._
import Scalaz._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import MutableMapRentalService._
/**
  * Dummy implementation with in-memory maps
  */
class MutableMapRentalService extends RentalService[DVDServiceResult, String, UUID, UUID, ZonedDateTime]{

  override def addMovie(movie: String, qty: Int): DVDServiceResult[Set[UUID]] = $ <~~ ((repo: MutableMapDVDRepository) => repo.createNewDVDs(movie, qty))

  override def rentDVD(customer: UUID, dvd: UUID, timestamp: ZonedDateTime): DVDServiceResult[Unit] = $ <~~ ((repo: MutableMapDVDRepository) => repo.updateDVDStatus(dvd, true, customer, timestamp))


  override def findDVD(movie: String): DVDServiceResult[Option[UUID]] = $ <~~ ((repo: MutableMapDVDRepository) => repo.findAvailableDVD(movie))


  override def returnDVD(customer: UUID, dvd: UUID, timestamp: ZonedDateTime): DVDServiceResult[Unit] = $ <~~ ((repo: MutableMapDVDRepository) => repo.updateDVDStatus(dvd, false, customer, timestamp))

}

object MutableMapRentalService {
  type DVDServiceResult[A] = ServiceResult[A, MutableMapDVDRepository]
}


class MutableMapDVDRepository extends DVDRepository[AsyncResult, String, UUID, Boolean, UUID, ZonedDateTime] {

  private val availableDvds: MMap[String, Set[UUID]] = MMap()
  private val dvdMovies: MMap[UUID, String] = MMap()
  private val rentedDvds: MSet[UUID] = MSet()



  override def createNewDVDs(movie: String, qty: Int): AsyncResult[Set[UUID]] = for {
    dvds <- $ <~ List.fill(qty)(UUID.randomUUID()).toSet
    _ = availableDvds += movie -> (availableDvds.getOrElse(movie, Set()) ++ dvds)
    _ = dvdMovies ++= dvds.map(_ -> movie)
  } yield dvds

  override def updateDVDStatus(dvdId: UUID, status: Boolean, customer: UUID, timestamp: ZonedDateTime): AsyncResult[Unit] = if(status) {
    if(rentedDvds.contains(dvdId)) {
      $ <~ -\/(s"$dvdId is already rented")
    } else dvdMovies.get(dvdId) match {
      case None => $ <~ -\/(s"$dvdId is not in the inventory")
      case Some(movie) =>
        rentedDvds += dvdId
        availableDvds += movie -> (availableDvds.getOrElse(movie, Set()) - dvdId)
        $ <~ Unit
    }
  } else {
    dvdMovies.get(dvdId) match {
      case None => $ <~ -\/(s"$dvdId is not in the inventory")
      case Some(movie) =>
        rentedDvds -= dvdId
        availableDvds += movie -> (availableDvds.getOrElse(movie, Set()) + dvdId)
        $ <~ Unit
    }
  }

  override def findAvailableDVD(movie: String): AsyncResult[Option[UUID]] = $ <~ availableDvds.get(movie).flatMap(_.headOption)

}
