package example.videoclub


import cats.data._
import example.videoclub.repository.Repository

import scala.concurrent.Future

package object services {


  type Errors = String

  type Valid[A] = Either[Errors, A]

  type AsyncResult[A] = EitherT[Future, Errors, A]

  type ServiceResult[A, Repo <: Repository] = Kleisli[AsyncResult, Repo, A]


  object $ {
    def <~[A](a: Future[Valid[A]]): AsyncResult[A] = EitherT(a)
    def <~[A](a: Valid[A])        : AsyncResult[A] = <~(Future.successful(a))
    def <~[A](a: A)               : AsyncResult[A] = <~(Right(a))

    def <~~[A, Repo <: Repository](a: Repo => AsyncResult[A]) : ServiceResult[A, Repo] = Kleisli((r: Repo) => a(r))
    def <~~[A, Repo <: Repository](a: AsyncResult[A])         : ServiceResult[A, Repo] = <~~(_ => a)
    def <~~[A, Repo <: Repository](a: Future[Valid[A]])       : ServiceResult[A, Repo] = <~~(<~(a))
    def <~~[A, Repo <: Repository](a: Valid[A])               : ServiceResult[A, Repo] = <~~(<~(a))
    def <~~[A, Repo <: Repository](a: A)                      : ServiceResult[A, Repo] = <~~(<~(a))
  }

}
