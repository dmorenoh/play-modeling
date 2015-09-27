package model

import java.util.UUID

import algebra.Repository
import model.VideoStoreModel._

import scala.concurrent.{ExecutionContext, Future}
import scalaz._
import Scalaz._

case class Movie(id: MovieID, title: String, year: Int, genre: Genre)

case class DVD(id: DvdID, movie: Movie, rented: Boolean)

object Movie {

  private def validTitle(value: String): Valid[String] = {
    val trimmed = value.trim

    if (trimmed.isEmpty) "Title was empty".failureNel else trimmed.successNel
  }

  private def validYear(value: Int): Valid[Int] = if (value > 1880 && value <= 2100)
    value.successNel
  else
    s"invalid year $value".failureNel

  def create(title: String, year: Int, genre: Genre): Valid[Movie] =
    (validTitle(title) |@| validYear(year)) {
      (t, y) => Movie(newMovieId, t, y, genre)
    }
}

object Genre {

  private def validGenre(value: String): Valid[String] = {
    val trimmed = value.trim

    if (trimmed.isEmpty) "Genre name was empty".failureNel else trimmed.successNel
  }

  def create(genre: String): Valid[Genre] = validGenre(genre)
}

object DVD {

  def create(movie: Movie): Valid[DVD] = DVD(newDvdId, movie, false).successNel
}

object VideoStoreModel {

  type MovieID = UUID
  type Genre = String
  type DvdID = UUID

  type Error = NonEmptyList[String]

  type Valid[A] = ValidationNel[String, A]

  def newMovieId = UUID.randomUUID()
  def newDvdId = UUID.randomUUID()

  type Result[A] = EitherT[Future, Error, A]

  type Repo = Repository[Future, Movie, MovieID, Genre, DVD, DvdID]

  type OP[A] = Kleisli[ Result, Repo, A]

}

object res {
  def <~[A](f: Future[Error \/ A])                        : Result[A] = EitherT(f)
  def <~[A](a: Future[A])(implicit ctx: ExecutionContext) : Result[A] = EitherT(a.map(_.right))
  def <~[A](r: Error \/ A)                                : Result[A] = EitherT(Future.successful(r))
  def <~[A](v: Valid[A])                                  : Result[A] = EitherT(Future.successful(v.disjunction))
  def <~[A](a: A)                                         : Result[A] = EitherT(Future.successful(a.right)) // a.point[Result] ?

}
