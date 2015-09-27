package algebra

trait VideoStore[M[_], Movie, Genre, DVD] {

  def addNewDVDs(movie: Movie, genre: Genre, qty: Int): M[List[DVD]]

  def rent(movie: Movie): M[Option[DVD]]

  def returnDvd(dvd: DVD): M[DVD]

  def searchByGenre(genre: Genre): M[List[Movie]]

}
