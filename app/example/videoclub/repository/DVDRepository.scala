package example.videoclub.repository

trait DVDRepository[M[_], Movie, DVDId, DVDStatus, Customer, Timestamp] extends Repository {

  def createNewDVDs(movie:Movie, qty: Int): M[Set[DVDId]]

  def updateDVDStatus(dvdId: DVDId, status: DVDStatus, customer: Customer, timestamp: Timestamp): M[Unit]

  def findAvailableDVD(movie: Movie): M[Option[DVDId]]

}
