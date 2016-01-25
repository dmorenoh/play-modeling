package example.videoclub

import example.videoclub.repository.AsyncRepository

import scala.concurrent.Future
import scalaz.Scalaz._
import scalaz._
import play.api.libs.concurrent.Execution.Implicits.defaultContext


package object services {


  type Errors = String

  type Valid[A] = Errors \/ A

  type AsyncResult[A] = EitherT[Future, Errors, A]

  type ServiceResult[A] = Kleisli[AsyncResult, AsyncRepository, A]


  object $ {
    def <~[A](a: AsyncRepository => AsyncResult[A]): ServiceResult[A] = Kleisli.kleisliU((r: AsyncRepository) => a(r))
    def <~[A](a: Future[Valid[A]]): ServiceResult[A] = <~(_ => EitherT(a))
    def <~[A](a: Valid[A]): ServiceResult[A] = <~(Future.successful(a))
    def <~[A](a: A): ServiceResult[A] = <~(a.right)
  }

}
