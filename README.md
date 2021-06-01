# An Akka based microservice REST Template

This project aims to show how to put together different modules of the stack [Akka](https://akka.io) to realize a scalable microservice with a persistent state.
It's a REST-based microservice following a "contract first" approach: from an OpenAPI 3.0 interface specification file, a code generator generates all the stubs, including the data transfer classes.

This template uses the following [Akka](https://akka.io) modules:

* [Akka Actors](https://doc.akka.io/docs/akka/current/typed/index.html)
* [Akka HTTP](https://doc.akka.io/docs/akka-http/current/introduction.html)
* [Akka Cluster](https://doc.akka.io/docs/akka/current/typed/cluster.html)
* [Akka Cluster Sharding](https://doc.akka.io/docs/akka/current/typed/cluster-sharding.html)
* [Akka Persistence](https://doc.akka.io/docs/akka/current/typed/persistence.html)
* [Akka Projections](https://doc.akka.io/docs/akka-projection/current/)
* [Akka Management](https://doc.akka.io/docs/akka-management/current/)
* [Akka Streams](https://doc.akka.io/docs/akka/current/stream/index.html)

Combining all those modules, this microservice provides the following features:
 1. A persistent actor manages the state.
 2. The state is "sharded" across multiple instances of the persistent actor.
 3. Each shard is automatically associated with a cluster node.
 4. The shards are automatically redistributed in case of cluster shrinking or expanding.
 5. Each shard is associated with independent journal and snapshot store.
 6. For each shard, a stream of event is generated for feeding an external database (projection)

These features collectively make the microservice able to scale to manage the persistence of millions of objects in memory,
and to evenly distribute the client requests among all the cluster nodes.
The state's persistence and reliability is guaranteed by the powerful event sourcing mechanism provided by [Akka Persistence](https://doc.akka.io/docs/akka/current/typed/persistence.html).

This project shows, as well, how to version the events in the journal, using [protobuf](https://developers.google.com/protocol-buffers) as a serialization mechanism.

This project provides everything is needed to deploy on Kubernetes with [Cassandra](https://doc.akka.io/docs/akka-persistence-cassandra/current/) as the persistence storage. It's also possible to run it in standalone mode with the in-memory journal.

## Project Structure

Under:
`src/main/resources`

there are the following files:
* interface-specification.yml

	The OpenAPI 3.0 interface specification
* logback.xml

	The logging configuration file
* reference.conf

	The Akka reference configuration file with Cassandra as a target for the persistence module and Kubernetes as the deployment platform

* reference-standalone.conf

	The Akka reference configuration file for a single node cluster and the in-memory journal
	

The code generation phase, triggered under sbt with the generateCode command, generates code under two directories:

*   `generated`

	It contains all the server-side generated code.

*   `client`

	It contains the generated code for the client-side. This code is also published as a dependency, and it allows the interaction with the microservice using pure Scala code.

The directory:

`templates`

contains two sub-directories:

*   `scala-akka-http-client`
*   `scala-akka-http-server`

Those directories contain some [mustache](https://mustache.github.io/) templates you can use for customizing the generated code.

For example, in the scala-akka\-http\-server there is a template: 

`controller.mustache`

It shows how to intercept any http invocation/request and pass it through the [openapi4j](https://github.com/openapi4j/openapi4j) validation function. In this way it's possible to validate the payload against the full openapi specification before reaching the user-defined code.

## How to make running the standalone version

You need to have installed:
* [sbt](https://www.scala-sbt.org/) version 1.5.2
* [openapi-generator](https://github.com/OpenAPITools/openapi-generator)

Clone the project: 

`git clone https://github.com/pagopa/pdnd-uservice-rest-template.git`

then, run the following commands:

`cd pdnd-uservice-rest-template`

`sbt packageXzTarball`

`tar -zxvf target/universal/pdnd-uservice-rest-template-latest.txz`

`export CASSANDRA_USER=cassandra`

`export CASSANDRA_PWD=cassandra`

`./pdnd-uservice-rest-template-latest/bin/pdnd-uservice-rest-template -Dconfig.resource=reference-standalone.conf`

at this point you can create 1000 objects (Pet):

> for ((i=0; i<1000; ++i))
do
echo $i ; curl -X POST "http://127.0.0.1:8088/pdnd-uservice-rest-template/v1/pet" -H  "accept: */*" -H  "Content-Type: application/json" -d "{\"id\":\"$i\",\"name\":\"CICCIO$i\"}"
done

You can also access the Swagger UI at: 

http://127.0.0.1:8088/pdnd-uservice-rest-template/v1/swagger-ui/index.html#/pet/addPet
