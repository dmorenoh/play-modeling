package example.videoclub.controllers

import example.videoclub.repository._
import example.videoclub.services._
import play.api.libs.json._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scalaz._
import Scalaz._

abstract class RentalController[Movie: Writes, DVD: Writes, Customer, Timestamp, Repo <: Repository] extends Controller {

  type Result[A] = ServiceResult[A, Repo]

  def service: RentalService[Result, Movie, Customer, DVD, Timestamp]

  def repository: Repo

  def helloWorld() = Action {
    Ok("Hello World")
  }

  /**
    * Handle Result. It's ame thing for every controller action. So why not use implicit conversion.
    */
  private implicit def run[A: Writes](result: Result[A]): Action[AnyContent] = Action.async {
    result.run(repository).fold(
      error => BadRequest(error),
      res => Ok(Json.toJson(res))
    )
  }

  def addDVDs(movie: Movie, qty: Int): Action[AnyContent] = service.addMovie(movie, qty)

}

