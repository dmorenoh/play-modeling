package algebra

trait Repository[M[_], Movie, MovieID, Genre, DVD, DvdID] {

  def saveMovie(movie: Movie): M[Unit]

  def saveGenre(genre: Genre): M[Unit]

  def saveDvd(dvd: DVD): M[Unit]

  def updateDVDStatus(id: DvdID, rented: Boolean): M[Unit]

  def getMovie(id: MovieID): M[Option[Movie]]

  def getDVD(id: DvdID): M[Option[DVD]]

  def getDVDsForMovie(id: MovieID): M[List[DVD]]

  def getMoviesByGenre(genre: Genre): M[List[Movie]]

}
