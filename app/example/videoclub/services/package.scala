package example.videoclub

import scala.concurrent.Future
import scalaz.Scalaz._
import scalaz._

package object services {


  type Errors = String

  type Valid[A] = Errors \/ A

  type ServiceResult[A] = EitherT[Future, Errors, A]


  object $ {
    def <~[A](a: Future[Valid[A]]): ServiceResult[A] = EitherT(a)
    def <~[A](a: Valid[A]): ServiceResult[A] = <~(Future.successful(a))
    def <~[A](a: A): ServiceResult[A] = <~(a.right)
  }

}
