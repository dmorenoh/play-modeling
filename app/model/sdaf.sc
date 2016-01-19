import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scalaz._
import Scalaz._
import scala.concurrent.ExecutionContext.Implicits.global

type Error = String
type Valid[A] = Error \/ A
type ValidF[A] = EitherT[Future, Error, A]
object X {
  def <~[A](x: Future[Valid[A]]): ValidF[A] = EitherT(x)
  def <~[A](x: Valid[A]): ValidF[A] = <~ (Future.successful(x))
  def <~[A](x: A): ValidF[A] = <~ (x.right)
}

def check(i: Int) = Future {
  println(s"checking $i")
  if (i<0) s"$i is less than 0".left else i.right
}

def add(a: Int, b: Int) = for {
  x <- X <~ check(a)
  y <- X <~ check(b)
} yield x + y

val r = add(2, 3)

val r2 = r.fold(
  e => s"Something blew u: $e",
  r => s"Sum is $r"
)



Await.result(r2, 1.second)


// A \/ B               fold(A => C, B => C): C
// Either[M, A, B]      fold(A => C, B => C): M[C]
