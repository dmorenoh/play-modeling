package interpeter

import algebra.VideoStore
import model.VideoStoreModel._
import model._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future
import scalaz.Kleisli._
import scalaz.Scalaz._
import scalaz._

trait VideoStoreInterpreter extends VideoStore[OP, Movie, Genre, DVD]{

  override def addNewDVDs(movie: Movie, genre: Genre, qty: Int): OP[List[DVD]] = kleisliU {
    repo: Repo =>
      val oneDvd = for {
        dvd <- res <~ DVD.create(movie)
        _   <- res <~ repo.saveDvd(dvd)
      } yield dvd

      List(1 to qty).traverseU(_ => oneDvd)
  }

  override def searchByGenre(genre: Genre): OP[List[Movie]] = kleisliU {
    repo: Repo =>

      res <~ repo.getMoviesByGenre(genre)
  }

  override def rent(movie: Movie): OP[Option[DVD]] = kleisliU {
    repo: Repo =>

      for {
        dvds    <- res <~ repo.getDVDsForMovie(movie.id)
        dvdOpt  <- res <~ dvds.filterNot(_.rented).headOption
        fOp     <- res <~ dvdOpt.map(dvd => repo.updateDVDStatus(dvd.id, true).map(_ => dvd.copy(rented = true)))
        r       <- res <~ fOp.map(_.map(_.some)).getOrElse(Future.successful(None))
      } yield r

  }
}

