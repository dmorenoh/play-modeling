
package example.videoclub.services

import cats.data.Xor
import cats.scalatest.XorMatchers._
import example.videoclub.repository.Repository
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.Matchers._
import org.scalatest.concurrent.ScalaFutures._
import org.scalatest.prop.PropertyChecks._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.util.Random
import cats.implicits._
import org.scalatest.WordSpec

abstract class RentalServiceRules[Movie: Arbitrary, DVD: Arbitrary, Customer: Arbitrary, Timestamp: Arbitrary, Repo <: Repository] extends WordSpec {

  type RentalServiceResult[A] = ServiceResult[A, Repo]

  def createService: RentalService[RentalServiceResult, Movie, Customer, DVD, Timestamp]

  def repo: Repo

  private val qtys = Gen.choose(1, 100)

  private val movies = implicitly[Arbitrary[Movie]].arbitrary
  private val customers = implicitly[Arbitrary[Customer]].arbitrary
  private val timestamps = implicitly[Arbitrary[Timestamp]].arbitrary

  private val qtyCustomers = for {
    qty     <- qtys
    custs   <- Gen.listOfN(qty, customers)
  } yield (qty, custs)


  private def withService[A](f: RentalService[RentalServiceResult, Movie, Customer, DVD, Timestamp] => A) = f(createService)

  "After adding DVDs, you should be able to find at least one" in forAll(movies -> "movie", qtys -> "qty") { (movie, qty) =>

    withService { service =>
      whenever(qty > 0) {
        val respones = for {
          dvds <- service.addMovie(movie, qty)
          found <- service.findDVD(movie)
          found <- service.findDVD(movie)
        } yield (dvds, found)

        whenReady(respones.run(repo).value) { result =>

          result shouldBe right

          val Xor.Right((dvds, foundDvd)) = result

          foundDvd shouldBe defined

          val Some(dvd) = foundDvd

          dvds should contain(dvd)
        }
      }
    }
  }


  "After adding DVDs, you should be able to rent at least one" in forAll(movies -> "movie", qtys -> "qty", customers -> "customer", timestamps -> "timestamp") {
    (movie, qty, customer, timestamp) =>

      withService { service =>

        whenever(qty > 0) {

          val respones = for {
            dvds <- service.addMovie(movie, qty)
            aDvd = Random.shuffle(dvds.toList).head
            r <- service.rentDVD(customer, aDvd, timestamp)
          } yield r

          whenReady(respones.run(repo).value) { result =>
            result shouldBe right

          }
        }
      }
  }

  "If movie is not in inventory, you should not be able to find it" in forAll(movies -> "movie") { movie =>
    withService { service =>
      val respones = service.findDVD(movie)

      whenReady(respones.run(repo).value) { result =>

        result shouldBe right

        val Xor.Right(foundDvd) = result

        foundDvd shouldBe empty
      }
    }
  }


  "You should be able to rent all available DVDs" in forAll(movies -> "movie", qtyCustomers -> "qty", timestamps -> "timestamp") {
    case (movie, (qty, custs), timestamp) =>

      withService { service =>

        whenever(qty > 0 && custs.nonEmpty) {

          val respones = for {
            dvds <- service.addMovie(movie, qty)
            resp <- dvds.toList.zip(custs).traverseU { case (dvd, customer) =>
              service.rentDVD(customer, dvd, timestamp)
            }
          } yield resp

          whenReady(respones.run(repo).value) { result =>
            result shouldBe right
          }
        }
      }
  }


  "You should not be able to find any more available DVDs after they are all rented" in forAll(movies -> "movie", qtyCustomers -> "qty", timestamps -> "timestamp") {
    case (movie, (qty, custs), timestamp) =>

      withService { service =>

        whenever(qty > 0 && custs.nonEmpty) {

          val respones = for {
            dvds <- service.addMovie(movie, qty)
            _ <- dvds.toList.zip(custs).traverseU {
              case (dvd, customer) =>
                service.rentDVD(customer, dvd, timestamp)
            }
            findDvd <- service.findDVD(movie)
          } yield findDvd

          whenReady(respones.run(repo).value) {
            result =>
              result shouldBe right
              val Xor.Right(foundDvd) = result

              foundDvd shouldBe empty

          }
        }
      }
  }


  "After DVD is returned, it should be available in search" in forAll(movies -> "movie", qtyCustomers -> "qty", timestamps -> "timestamp") {
    case (movie, (qty, custs), timestamp) =>

      withService { service =>

        whenever(qty > 0 && custs.nonEmpty) {

          val respones = for {
            dvds <- service.addMovie(movie, qty)
            dvdCustomerPairs = dvds.toList.zip(custs)
            _ <- dvdCustomerPairs.traverseU {
              case (dvd, customer) =>
                service.rentDVD(customer, dvd, timestamp)
            }
            (aDvd, aCustomer) = dvdCustomerPairs.head
            _ <- service.returnDVD(aCustomer, aDvd, timestamp)
            findDvd <- service.findDVD(movie)
          } yield (aDvd, findDvd)

          whenReady(respones.run(repo).value) {
            result =>
              result shouldBe right
              val Xor.Right((returnedDvd, foundDvd)) = result

              foundDvd shouldBe defined

              val Some(dvd) = foundDvd

              dvd shouldEqual returnedDvd

          }
        }
      }
  }

  "You should get error if trying to rent same dvd twice" in forAll(movies -> "movie", qtys -> "qty", customers -> "customer1", customers -> "customer2", timestamps -> "timestamp") {
    (movie, qty, customer1, customer2, timestamp) =>

      withService { service =>

        whenever(qty > 0) {

          val respones = for {
            dvds <- service.addMovie(movie, qty)
            aDvd = Random.shuffle(dvds.toList).head
            r1 <- service.rentDVD(customer1, aDvd, timestamp)
            r2 <- service.rentDVD(customer2, aDvd, timestamp)
          } yield r2

          whenReady(respones.run(repo).value) {
            result =>
              result shouldBe left
          }
        }
      }
  }
}


