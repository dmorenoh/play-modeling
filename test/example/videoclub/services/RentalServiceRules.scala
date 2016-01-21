package example.videoclub.services

import org.scalacheck.{Arbitrary, Gen}
import org.scalatest._
import org.scalatest.concurrent.{AsyncAssertions, ScalaFutures}
import org.scalatest.prop.PropertyChecks
import org.typelevel.scalatest.DisjunctionMatchers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random
import scalaz.Scalaz._
import scalaz._

abstract class RentalServiceRules[Movie: Arbitrary, DVD: Arbitrary, Customer: Arbitrary, Timestamp: Arbitrary] extends WordSpec
  with Matchers
  with PropertyChecks with ScalaFutures with DisjunctionMatchers with AsyncAssertions {

  def service: RentalService[ServiceResult, Movie, Customer, DVD, Timestamp]

  private val qtys = Gen.choose(1, 1000)

  private val movies = implicitly[Arbitrary[Movie]].arbitrary
  private val customers = implicitly[Arbitrary[Customer]].arbitrary
  private val timestamps = implicitly[Arbitrary[Timestamp]].arbitrary

  private val qtyCustomers = for {
    qty <- qtys
    custs <- Gen.listOfN(qty, customers)
  } yield (qty, custs)


  "After adding DVDs, you should be able to find at least one" in forAll(movies -> "movie", qtys -> "qty") { (movie, qty) =>

    val respones = for {
      dvds <- service.addMovie(movie, qty)
      found <- service.findDVD(movie)
    } yield (dvds, found)

    whenReady(respones.run) { result =>

      result shouldBe right

      val \/-((dvds, foundDvd)) = result

      foundDvd shouldBe defined

      val Some(dvd) = foundDvd

      dvds should contain(dvd)
    }
  }


  "After adding DVDs, you should be able to rent at least one" in forAll(movies -> "movie", qtys -> "qty", customers -> "customer", timestamps -> "timestamp") {
    (movie, qty, customer, timestamp) =>

      val respones = for {
        dvds <- service.addMovie(movie, qty)
        aDvd = Random.shuffle(dvds.toList).head
        _ <- service.rentDVD(aDvd, customer, timestamp)
      } yield ()

      whenReady(respones.run) { result =>
        result shouldBe right

      }
  }


  "You should be able to rent all available DVDs" in forAll(movies -> "movie", qtyCustomers -> "qty", timestamps -> "timestamp") {
    case (movie, (qty, customers), timestamp) =>

      val respones = for {
        dvds <- service.addMovie(movie, qty)
        resp <- dvds.toList.zip(customers).traverseU { case (dvd, customer) =>
          service.rentDVD(dvd, customer, timestamp)
        }
      } yield resp

      whenReady(respones.run) { result =>
        result shouldBe right
      }

  }


  "You should not be able to find any more availabel DVDs after they are all rented" in forAll(movies -> "movie", qtyCustomers -> "qty", timestamps -> "timestamp") {
    case (movie, (qty, customers), timestamp) =>

      val respones = for {
        dvds <- service.addMovie(movie, qty)
        _ <- dvds.toList.zip(customers).traverseU {
          case (dvd, customer) =>
            service.rentDVD(dvd, customer, timestamp)
        }
        findDvd <- service.findDVD(movie)
      } yield findDvd

      whenReady(respones.run) {
        result =>
          result shouldBe right
          val \/-(foundDvd) = result

          foundDvd shouldBe empty

      }

  }

}
