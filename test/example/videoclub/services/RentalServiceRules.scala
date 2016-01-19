package example.videoclub.services

import org.scalacheck.Arbitrary
import org.scalatest._
import org.scalatest.concurrent.{AsyncAssertions, ScalaFutures}
import org.scalatest.prop.PropertyChecks
import org.typelevel.scalatest.DisjunctionMatchers

import scala.concurrent.ExecutionContext.Implicits.global
import scalaz.Scalaz._
import scalaz._

abstract class RentalServiceRules[Movie: Arbitrary, DVD: Arbitrary, Customer: Arbitrary, Timestamp: Arbitrary] extends WordSpec
  with Matchers
  with PropertyChecks with ScalaFutures with DisjunctionMatchers with ParallelTestExecution with AsyncAssertions {

  def service: RentalService[FutureValid, Movie, DVD, Customer, Timestamp]

  "After adding DVDs, you should be able to find at least one" in forAll { (movie: Movie, n: Int) =>

    val result = for {
      dvds <- service.addMovie(movie, n)
      found <- service.findDVD(movie)
    } yield (dvds, found)

    whenReady(result.run) { result =>

      result shouldBe right

      val \/-((dvds, foundDvd)) = result

      foundDvd shouldBe defined

      val Some(dvd) = foundDvd
    }
  }


}
