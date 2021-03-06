[![Build Status](https://travis-ci.org/dragisak/play-modeling.svg?branch=master)](https://travis-ci.org/dragisak/play-modeling)  [![Codacy Badge](https://api.codacy.com/project/badge/grade/ae7f4a6531594e18abf285c41148df5b)](https://www.codacy.com/app/dragishak/play-modeling) [![Codacy Badge](https://api.codacy.com/project/badge/coverage/ae7f4a6531594e18abf285c41148df5b)](https://www.codacy.com/app/dragisak/play-modeling)

# Functional Domain Modeling in Play

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
- [Functional Domain Modeling in Play](#functional-domain-modeling-in-play)
  - [Intro](#intro)
  - [Goals](#goals)
  - [Evolution of The Functional Domain Model](#evolution-of-the-functional-domain-model)
    - [1. Parametrize Types In The Service Trait](#1-parametrize-types-in-the-service-trait)
    - [2. Add Higher-kind Wrapper `M[_]` Around Results](#2-add-higher-kind-wrapper-m_-around-results)
      - [What are all the choices for `M[_]` ?](#what-are-all-the-choices-for-m_-)
    - [4. Start Defining Business Domain Rules](#4-start-defining-business-domain-rules)
    - [5. Define Repository Trait](#5-define-repository-trait)
    - [3. Implement Controllers](#3-implement-controllers)
    - [6. Naive Implementation of The Domain](#6-naive-implementation-of-the-domain)
    - [7. Implement Domain Using Repository Backed by Relational Database](#7-implement-domain-using-repository-backed-by-relational-database)
  - [Resources](#resources)
<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## Intro

The is is an example of a Play project that uses functional domain modeling approach in it's design.
This is a, very simplified, REST API for a fictional DVD rental application.

## Goals

1. When you start a new project, it often takes months to gain enough domain knowledge to properly implement it.
Often it's too late at that point and you are stuck with design decisions you made early in the project.
As a developer, you want to *delay* making decisions on concrete implementation until you gain enough
domain knowledge.
1. You want to separate domain complexity from [accidental complexity](https://en.wikipedia.org/wiki/No_Silver_Bullet) that comes from using a particular framework
 or a library.

## Evolution of The Functional Domain Model

### 1. Parametrize Types In The Service Trait

```scala
trait RentalService[Movie, Customer, DVD, Timestamp] {

  def addMovie(movie: Movie, qty: Int): Set[DVD]
  def findDVD(movie: Movie): Option[DVD]
  def rentDVD(customer: Customer, dvd: DVD, timestamp: Timestamp): Unit
  def returnDVD(customer: Customer, dvd: DVD, timestamp: Timestamp): Unit

}
```

Using Domain Driven Design (DDD) terminology, types are the _nouns_ and function names are the _verbs_.

### 2. Add Higher-kind Wrapper `M[_]` Around Results

```scala
trait RentalService[M[_], Movie, Customer, DVD, Timestamp] {

  def addMovie(movie: Movie, qty: Int): M[Set[DVD]]
  def findDVD(movie: Movie): M[Option[DVD]]
  def rentDVD(customer: Customer, dvd: DVD, timestamp: Timestamp): M[Unit]
  def returnDVD(customer: Customer, dvd: DVD, timestamp: Timestamp): M[Unit]

}
```

The purpose is to allow defining "context". Error handling. Is it async or sync ?

#### What are all the choices for `M[_]` ?

`M[_]`                  | Description
----------------------- | -----------------------------------------------------------
`Id`                    | A do-nothing monad: `type Id[A] = A`. Same as not having `M`
`Error \/ A`            | Adds error handling: `type Valid[A] = Error \/ A`
`Future`                | Async service
`Future[Error \/ A]`    | Best of the above two. Can be turned into a Monad using `EitherT[Future, Error, A]`

### 4. Start Defining Business Domain Rules
```scala
  "After adding DVDs, you should be able to rent at least one" in forAll(movies -> "movie", qtys -> "qty", customers -> "customer", timestamps -> "timestamp") {
    (movie, qty, customer, timestamp) =>

      withService { service =>

        whenever(qty > 0) {

          val respones = for {
            dvds  <- service.addMovie(movie, qty)
            aDvd  = Random.shuffle(dvds.toList).head
            r     <- service.rentDVD(customer, aDvd, timestamp)
          } yield r

          whenReady(respones.run) { result =>
            result shouldBe right
          }
        }
      }
  }
```

Notice that, at this point, we still don't have any concrete implementation of what "Movie", "DVD", etc are.
We can't introduce any accidental complexity from the implementation because there, simply, isn't any
implementation yet.

This is what Test Driven Development (TDD) should really be like. Tests should verify and, in a way, document business logic.


### 5. Define Repository Trait

Make dependency on repository explicit. As a part of your return type. Then, it's easy to see what part of the framework a function needs. This is a form
of dependency injection (DI).

Our `M[_]` becomes a function: `Repository => Future[Error \/ A]`. Function of type `A => F[B]` are also called Kelisli. If `F` is a monad, then
`Kleisli[F, A, B]` is a monad too.

```scala
type ServiceResult[A, Repo <: Repository] = Kleisli[AsyncResult, Repo, A]
```

### 3. Implement Controllers

At this point, event if you don't have any implementation, you can implement a simple controller that returns Json.

For example:

```scala
// Abstract class because traits don't allow context bounds on types.
abstract class RentalController[Movie: Writes, DVD: Writes, Customer, Timestamp, Repo <: Repository] extends Controller {

  type Result[A] = ServiceResult[A, Repo]

  def service: RentalService[Result, Movie, Customer, DVD, Timestamp]

  def repository: Repo

  /**
    * Handle Result. It's same thing for every controller action. So why not use implicit conversion.
    */
  private implicit def run[A: Writes](result: Result[A]): Action[AnyContent] = Action.async {
    result.run(repository).fold(
      error => BadRequest(error),
      res => Ok(Json.toJson(res))
    )
  }

  def addDVDs(movie: Movie, qty: Int): Action[AnyContent] = service.addMovie(movie, qty)

}
```

When the types are known and we have concrete implementation of repository, we'll just
create a concrete controller class extending this class.

### 6. Naive Implementation of The Domain

### 7. Implement Domain Using Repository Backed by Relational Database

## Resources

* [Functional and Reactive Domain Modeling](https://manning.com/books/functional-and-reactive-domain-modeling) by  Debasish Ghosh
* [Functional Programming In Scala](https://manning.com/books/functional-programming-in-scala) by Paul Chiusano and Rúnar Bjarnason


