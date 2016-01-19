package example.videoclub.controllers

import play.api.mvc.Results
import play.api.test._
import play.api.test.Helpers._
import org.scalatestplus.play._

class RentalControllerTest extends PlaySpec with Results {

  "helloWorld" must {
    "return Hello World" in new App {
      val request = FakeRequest(GET, "/helloWorld ")

      val Some(result) = route(request)

      status(result) mustEqual OK

      contentAsString(result) mustEqual "Hello World"
    }
  }
}
