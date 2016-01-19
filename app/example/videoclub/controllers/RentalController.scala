package example.videoclub.controllers

import javax.inject.Singleton

import play.api.mvc._

trait RentalController extends Controller {

  def helloWorld() = Action {
    Ok("Hello World")
  }

}

@Singleton
class RentalControllerImpl extends RentalController
