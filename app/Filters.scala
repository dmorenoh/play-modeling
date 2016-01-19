import javax.inject.Inject

import example.videoclub.filters.LoggingFilter
import play.api.http.HttpFilters

class Filters @Inject()(log: LoggingFilter) extends HttpFilters {

  override val filters = Seq(log)

}
