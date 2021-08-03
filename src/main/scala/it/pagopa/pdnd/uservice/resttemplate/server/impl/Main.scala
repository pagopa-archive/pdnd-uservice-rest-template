package it.pagopa.pdnd.uservice.resttemplate.server.impl

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.ClusterEvent
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, ShardedDaemonProcess}
import akka.cluster.sharding.typed.{ClusterShardingSettings, ShardingEnvelope}
import akka.cluster.typed.{Cluster, Subscribe}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable.apply
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.directives.SecurityDirectives
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.persistence.typed.PersistenceId
import akka.projection.ProjectionBehavior
import akka.{actor => classic}
import io.github.resilience4j.ratelimiter.{RateLimiter, RateLimiterConfig, RateLimiterRegistry, RequestNotPermitted}
import it.pagopa.pdnd.uservice.resttemplate.api.PetApi
import it.pagopa.pdnd.uservice.resttemplate.api.impl.{PetApiMarshallerImpl, PetApiServiceImpl}
import it.pagopa.pdnd.uservice.resttemplate.common.system.Authenticator
import it.pagopa.pdnd.uservice.resttemplate.model.persistence.{Command, PetPersistentBehavior, PetPersistentProjection}
import it.pagopa.pdnd.uservice.resttemplate.server.Controller
import kamon.Kamon

import java.time.Duration
import scala.jdk.CollectionConverters._

@SuppressWarnings(
  Array(
    "org.wartremover.warts.StringPlusAny",
    "org.wartremover.warts.Nothing",
    "org.wartremover.warts.ImplicitConversion"
  )
)
object Main extends App {

  Kamon.init()

  locally {
    val _ = ActorSystem[Nothing](
      Behaviors.setup[Nothing] { context =>
        import akka.actor.typed.scaladsl.adapter._
        implicit val classicSystem: classic.ActorSystem = context.system.toClassic

        val cluster = Cluster(context.system)

        context.log.error(
          "Started [" + context.system + "], cluster.selfAddress = " + cluster.selfMember.address + ")"
        )

        val sharding: ClusterSharding = ClusterSharding(context.system)

        val petPersistentEntity: Entity[Command, ShardingEnvelope[Command]] = Entity(typeKey = PetPersistentBehavior.TypeKey) { entityContext =>
          PetPersistentBehavior(entityContext.shard, PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId))
        }

        val _ = sharding.init(petPersistentEntity)

        val settings: ClusterShardingSettings = petPersistentEntity.settings match {
          case None    => ClusterShardingSettings(context.system)
          case Some(s) => s
        }

        val persistence = classicSystem.classicSystem.settings.config.getString("pdnd-uservice-rest-template.persistence")
        if(persistence == "cassandra") {
          val petPersistentProjection = new PetPersistentProjection(context.system, petPersistentEntity)

          ShardedDaemonProcess(context.system).init[ProjectionBehavior.Command](
            name = "pet-projections",
            numberOfInstances = settings.numberOfShards,
            behaviorFactory = (i: Int) => ProjectionBehavior(petPersistentProjection.projections(i)),
            stopMessage = ProjectionBehavior.Stop)
        }

        val config = RateLimiterConfig.custom()
          .limitRefreshPeriod(Duration.ofMillis(10000))
          .limitForPeriod(10)
          .timeoutDuration(Duration.ofMillis(1000))
          .build()

        val rateLimiterRegistry = RateLimiterRegistry.of(config)

        val rateLimiterWithDefaultConfig = rateLimiterRegistry.rateLimiter("name1")

        abstract class ExtractContexts extends Directive1[Seq[(String, String)]]

        object ExtractContexts {
          implicit def apply(other: Directive1[Seq[(String, String)]]): ExtractContexts =
            inner =>
              other.tapply(
                seq =>
                  rc => {
                    val headers = rc.request.headers.map(header => (header.name(), header.value()))
                    inner(Tuple1(seq._1 ++ headers))(rc)
                  }
              )
        }

        abstract class Throttle extends Directive1[Seq[(String, String)]]

        object Throttle {
          implicit def apply(other: Directive1[Seq[(String, String)]]): Throttle =
            inner =>
              other.tapply(
                seq =>
                  rc => try {
                    RateLimiter.waitForPermission(rateLimiterWithDefaultConfig)
                    inner(seq)(rc)
                  } catch {
                    case _: RequestNotPermitted =>
                      complete(StatusCodes.TooManyRequests)(rc)
                  }
              )
        }

        val petApi = new PetApi(
          new PetApiServiceImpl(context.system, sharding, petPersistentEntity),
          new PetApiMarshallerImpl(),
          Throttle {
            ExtractContexts {
              SecurityDirectives.authenticateBasic("SecurityRealm", Authenticator)
            }
          }
        )

        val _ = AkkaManagement.get(classicSystem).start()

        val controller = new Controller(
          petApi,
          validationExceptionToRoute = Some(e => {
            val results = e.results()
            if(results != null) {
              results.crumbs().asScala.foreach { crumb =>
                println(crumb.crumb())
              }
              results.items().asScala.foreach { item =>
                println(item.dataCrumbs())
                println(item.dataJsonPointer())
                println(item.schemaCrumbs())
                println(item.message())
                println(item.severity())
              }
              val message = e.results().items().asScala.map(_.message()).mkString("\n")
              complete((400, message))
            } else
            complete((400, e.getMessage))
          })
        )

        val _ = Http().newServerAt("0.0.0.0", 8088).bind(controller.routes)

        val listener = context.spawn(
          Behaviors.receive[ClusterEvent.MemberEvent]((ctx, event) => {
            ctx.log.error("MemberEvent: {}", event)
            Behaviors.same
          }),
          "listener"
        )

        Cluster(context.system).subscriptions ! Subscribe(
          listener,
          classOf[ClusterEvent.MemberEvent]
        )

        val _ = AkkaManagement(classicSystem).start()
        ClusterBootstrap.get(classicSystem).start()
        Behaviors.empty
      }, "pdnd-uservice-rest-template")
  }
}
