package example.videoclub.services

import java.util.UUID
import java.time._

object DummyRentalService extends RentalService[ServiceResult, String, UUID, UUID, ZonedDateTime]{

  override def addMovie(movie: String, qty: Int): ServiceResult[Set[UUID]] = $ <~ List.fill(qty)(UUID.randomUUID()).toSet

  override def rentDVD(dvd: UUID, customer: UUID, timestamp: ZonedDateTime): ServiceResult[Unit] = $ <~ Unit

  override def findDVD(movie: String): ServiceResult[Option[UUID]] = $ <~ Some(UUID.randomUUID())

  override def returnDVD(customer: UUID, dVD: UUID, timestamp: ZonedDateTime): ServiceResult[Unit] = $ <~ Unit
}
