# Scalability for dummies

## Clones

Servers hidden behing a load balancer.

Load balancer evenlydistributes load (request from users) into your group/cluster of app services. Example: first request may be served at his first request by server 2 then second request by server 9 

User always get the same results of his request independent what server he "landed on"

* every server contains same codebase

* does not store user-related data (sessions) on local disc or memory

Sessions can be stored in a centralized data store accessible to all your app

* extenal DB

* external presistent cache like Redis



## Database

Path 1

* master - slave replication
  
  * read from slaves write to master

* sharding

* denormalizaiton

* SQL tuning

Path 2

* denormalize right from the befinning 

* no not necessary joins in database quey

* stay with MySSQL and used it like NoSQL db 

* if still slow introduce a cache

## Cache

Neve do file-based caching go with memory caches (Memcached Redis)

It is key values store

**Cached DB queries**

* hashed version of your query is the cache key
  
  * hard to reason about is expiration

**Cached Objects**

* user sessions

* activity streams

* user <-> friend relationships

## Asynchronism

Option 1: do the working during night/low working hours (cron jobs)

Option 2: queue of tasks/jobs that a worker can process 
