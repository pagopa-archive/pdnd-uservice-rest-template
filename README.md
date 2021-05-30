# PDND Micro Service REST Template

sbt packageXzTarball

tar -zxvf target/universal/pdnd-uservice-rest-template-latest.txz

export CASSANDRA_USER=cassandra

export CASSANDRA_PWD=cassandra

./pdnd-uservice-rest-template-latest/bin/pdnd-uservice-rest-template -Dconfig.resource=reference-standalone.conf

http://localhost:8088/pdnd-uservice-rest-template/v1/swagger-ui/index.html
