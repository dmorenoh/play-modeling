package example.videoclub.services

import java.time._
import java.util.UUID

import org.scalacheck.{Arbitrary, Gen}

import scala.concurrent.duration._

sealed class DummyRentalServiceSpecsRun(
  implicit val arbUUID: Arbitrary[UUID] = Arbitrary(Gen.uuid),
  implicit val arbZonedDateTIme: Arbitrary[ZonedDateTime] = Arbitrary(for {
    day     <- Gen.choose(0L, 3650L)
    hour    <- Gen.choose(0L, 23L)
    minute  <- Gen.choose(0L, 59L)
    second  <- Gen.choose(0L, 59L)
    tz      <- Gen.oneOf(ZoneId.of("America/Los_Angeles"), ZoneId.of("America/New_York"), ZoneId.of("America/Chicago"))
  } yield ZonedDateTime.of(2016, 1, 1, 0, 0, 0, 0, tz).plusDays(day).plusHours(hour).plusMinutes(minute).plusSeconds(second)
  )

) extends RentalServiceRules[String, UUID, UUID, ZonedDateTime] {

  override implicit val patienceConfig = PatienceConfig(2 seconds)

  override def service: RentalService[ServiceResult, String, UUID, UUID, ZonedDateTime] = DummyRentalService
}

class DummRentalServiceSpecs extends DummyRentalServiceSpecsRun



