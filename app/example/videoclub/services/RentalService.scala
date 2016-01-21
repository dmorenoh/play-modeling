package example.videoclub.services

trait RentalService[M[_], Movie, Customer, DVD, Timestamp] {

  def addMovie(movie: Movie, qty: Int): M[Set[DVD]]

  def findDVD(movie: Movie): M[Option[DVD]]

  def rentDVD(customer: Customer, dvd: DVD, timestamp: Timestamp): M[Unit]

  def returnDVD(customer: Customer, dvd: DVD, timestamp: Timestamp): M[Unit]

}
