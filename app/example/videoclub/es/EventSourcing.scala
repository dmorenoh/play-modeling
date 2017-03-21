package example.videoclub.es

import cats.implicits._

import scala.concurrent.{ExecutionContext, Future}

trait EventSourcing {

  type Failure
  type Domain
  type Event

  type CommandHandle[C] = (C, Domain) => Either[Failure, Seq[Event]]

  type EventApply = (Domain, Event) => Domain

  type EventPersist = Seq[Event] => Future[Unit]

  type Respond[C, R] = (C, Domain, Domain) => R


  type ES[C, T] = (C, Domain) => Either[Failure, Future[(Domain, T)]]


  def handler[C, R](
    command    : CommandHandle[C],
    eventApply : EventApply,
    persist    : EventPersist,
    respond    : Respond[C, R]
  )(implicit ec: ExecutionContext): ES[C, R] = (cmd, domain) => command(cmd, domain).map { events =>
      val newDomain = events.foldLeft(domain)(eventApply)
      persist(events).map(_ => (newDomain, respond(cmd, domain, newDomain)))
  }

}
