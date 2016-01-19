package example.videoclub.filters

import play.api.Logger
import play.api.http.HeaderNames
import play.api.mvc.{Result, RequestHeader, Filter}

import scala.concurrent.Future

class LoggingFilter extends Filter {

  private val log = Logger(this.getClass)

  override def apply(nextFilter: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {

    if (rh.path.startsWith("/health-check")) {
      // do not litter logs with noise from health-check
      log.debug(s"[${getAddress(rh)}] $rh")
    } else {
      log.info(s"[${getAddress(rh)}] $rh")
    }

    nextFilter(rh)
  }

  private def getAddress(rh: RequestHeader): String = {
    // When you have a load balancer in front of your service, all requests appear like comming from the load balancer
    // LB can be configured to append standard X-Forwarded-For header to send the original address
    val forwardedAddr = rh.headers.get(HeaderNames.X_FORWARDED_FOR)

    forwardedAddr
      .map(fw => s"$fw (${rh.remoteAddress})")
      .getOrElse(rh.remoteAddress)
  }
}
