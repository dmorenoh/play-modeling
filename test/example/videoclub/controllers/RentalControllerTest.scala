package example.videoclub.controllers

import play.api.libs.ws.WS
import play.api.mvc.Results
import play.api.test._
import play.api.test.Helpers._
import org.scalatestplus.play._

class RentalControllerTest extends PlaySpec with Results with OneServerPerSuite {

  "helloWorld" must {
    "return Hello World" in new App {
      val request = FakeRequest(GET, "/helloWorld ")

      val Some(result) = route(request)

      status(result) mustEqual OK

      contentAsString(result) mustEqual "Hello World"
    }
  }

  "server" must {
    "return Hello World" in {

      val response = await(WS.url(s"http://localhost:$port/helloWorld").get())

      response.status mustBe OK

      response.body mustEqual "Hello World"
    }

  }


}
