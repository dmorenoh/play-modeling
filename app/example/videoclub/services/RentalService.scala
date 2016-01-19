package example.videoclub.services

trait RentalService[M[_], Movie, Customer, DVD, Timestamp] {

  def addMovie(movie: Movie, qty: Int): M[Set[DVD]]

  def findDVD(movie: Movie): M[Option[DVD]]

  def rentDVD(dvd: DVD, customer: Customer, timestamp: Timestamp): M[Unit]

  def returnDVD(customer: Customer, dVD: DVD, timestamp: Timestamp): M[Unit]

}
