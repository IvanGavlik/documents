# Performance vs scalability

## Performance

how fast a system responds undere a given load 

Aspects 

* response time (latency)

* request per second (throughput)

* resource usage (CPU, memory)

Example

* An API responds in 50 ms vs 500 ms

* A query executes in 10 ms vs 2 secuds

## Scalability

How well system handles the increasing load

Aspects

* handling more users, requests, data

* maintaining acceptable performance as load increasses 

* ability to ecpand resources (hardware or instances)

Types

* Vertical 

* Horizontal

Example

System handles 100 users fine. System handles 10 000 users without crashing -> scalable but if System crashed at 500 users -> not scalable

Q: What happens when demand increases ? 

“but that doesn’t scale”

* architecture of their system limits their ability to grow their service

* when we increase the resources in a system, it results in increased performance
  in a manner proportional to resources added

Why is scalability hard ? 

* system that works perfectly for 100 users can completely break at 100,000—not because the logic is wrong, but because the assumptions no longer hold. At small scall you rely on single DB fast local calls no latency but at scale network latency mtters, disk IO becomes botteneck 

* magaging state becomes a problem exmples: keeping data consistend across replicas, handling partial failuers, deciding between consistency vs availability 

* concurrency explodes with more users more simultaneous request race condiitons deadlocks timing issues

* fix one bottlenec another appears: CPU -> memory -> DB -> network 

* data grows faster than expected slow queries, indexing challenges, partition/sharding complexity

For the systems we build we must carefully inspect along which axis we expect the
system to grow

## Key Diff

Focus 

* P: speed and efficienty

* S: growth and capacity

Question

* P: how fast

* S: how much can it handle

Scope

* P: current load

* S: future / increasing load

Metrics

* P: latency throughput

* S: load handling elasticity

### Practical Examples

DB queries

* P: optimize indexes, aviod N+1 queries

* S: sharding, replication, read replicas

BE services

* P: performance caching efficient algo

* S: statelees services load balacking

## Mesure

### Tools

* Prometheus

* Grafana

* OpenTelemetry

* Jaeger

### What

API Layer

* request duration

* status codes

* endpoint level metrics (for each endpoint)
  
  * latency (response time)
  
  * request rate (throughput)
  
  * error rate
  
  * status code distribution
  
  * in-flight reques (how many requests are being processed at the same time)
  
  * Note: Don’t use percentiles Averages hide slow requests

DB

* query duration

* connection pool usages

External services

* call duration

* failue rate

### Load testing

Tools

* k6

* Apace JMeter

* Gatling

## Scalability Availability & Stability Patterns




