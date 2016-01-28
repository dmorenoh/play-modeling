package example.videoclub


import example.videoclub.repository.{DVDRepository, Repository}

import scala.concurrent.Future
import scalaz.Scalaz._
import scalaz._
import play.api.libs.concurrent.Execution.Implicits.defaultContext


package object services {


  type Errors = String

  type Valid[A] = Errors \/ A

  type AsyncResult[A] = EitherT[Future, Errors, A]

  type ServiceResult[A, Repo <: Repository] = Kleisli[AsyncResult, Repo, A]


  object $ {
    def <~[A](a: Future[Valid[A]]): AsyncResult[A] = EitherT(a)
    def <~[A](a: Valid[A])        : AsyncResult[A] = <~(Future.successful(a))
    def <~[A](a: A)               : AsyncResult[A] = <~(a.right)

    def <~~[A, Repo <: Repository](a: Repo => AsyncResult[A]) : ServiceResult[A, Repo] = Kleisli.kleisliU((r: Repo) => a(r))
    def <~~[A, Repo <: Repository](a: AsyncResult[A])         : ServiceResult[A, Repo] = <~~(_ => a)
    def <~~[A, Repo <: Repository](a: Future[Valid[A]])       : ServiceResult[A, Repo] = <~~(<~(a))
    def <~~[A, Repo <: Repository](a: Valid[A])               : ServiceResult[A, Repo] = <~~(<~(a))
    def <~~[A, Repo <: Repository](a: A)                      : ServiceResult[A, Repo] = <~~(<~(a))
  }

}
