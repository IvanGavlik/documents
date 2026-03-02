# Application Layer

## HTTP/HTTPs

Stateless protocol for fetching resources (such as HTML documetns)

Client server protocol exchange individual messages

Uses TCP or TLS (for encryped connections)

**Components**

Request sent by **client/user-agent** (or a proxy on behave of it) most of the time is a Web browser to a server which handles it and provides an answer response

Server appears as only a single machine virtually but it may actually be collection of servers shating the load (load balancing) or other software (cacjes, db, e-commerce server)

Between server and clicnet we can have **proxies**

+ caching (public or private)

+ filteting (antivirus scan)

+ load balancing

+ authentication

+ logging

Stateless - no link between two request (HTTP Cookies are added to the workflow allowing sesstion creattion on each HTTP request to share the same context/state)

**Features**

* caching

* origin constraint

* basic authentication

* proxy and tunneling

* sessions

### Request reponse cycle

Before HTTP request/response msg client and server must establish a **TCP connection** (several round-trips)

* HTTP/1.1 TCP connection can be partially controlled using Connection header 

* HTTP/2 multiplexing message over a single conenction (keep the connection more efficient)

* QUIC protocol experiments 

### Flow

1. Open TCP connection (TCP level) 

2. Send HTTP reques message (HTTP/2, these messages are encapsulated in frames, making them impossible to read directly)
   
   ```
   GET / HTTP/1.1
   Host: developer.mozilla.org
   Accept-Language: fr
   ```

3. Read response sent by server
   
   ```
   HTTP/1.1 200 OK
   Date: Sat, 09 Oct 2010 14:28:02 GMT
   Server: Apache
   Last-Modified: Tue, 01 Dec 2009 20:18:22 GMT
   ETag: "51142bc1-7449-479b075b2891b"
   Accept-Ranges: bytes
   Content-Length: 29769
   Content-Type: text/html
   
   <!doctype html>
   
   … (here come the 29769 bytes of the requested web page)
   ```

## Messages

TODO [HTTP messages - HTTP | MDN](https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/Messages)

### Reques message

```
GET / HTTP/1.1
Host: developer.mozilla.org
Accept-Language: fr
```

First line

* HTTP method (GET, POST, OPTIONS, HEAD)

* Path on the server

* Protocol version

Next lines headers (additional info for the server)

Body for some method (POST)

### Response message

```
HTTP/1.1 200 OK
date: Sat, 09 Oct 2010 14:28:02 GMT
cache-control: public. max-age=3600
content-type: text/html
```

First line

* Protocol version

* Status code

* Status message 

Next lines headers (like for the request)

Body contating the fetched resources

### HTTP headers

Client and server pass additional info 

Format case insensitive name colon (:) value Example `Allow: POST`

```
HTTP Methods 

Status codes 

Headers

Cookies, Sessions, Tokens

HTTPS/TLS handshake

HTTP/1.1 vs HTTP/2 vs HTTP/3

### HTTPs

### Web Browser (how they work)

### Task

Use `curl`

* inspect HTTP request

* send POST request with JSON

* check headers

* follow redirects

* test api with authentication

* handle cookies

* test cors

Benchmark HTTP performance

Debug TSL/SSL issues

Test HTTP/2 support

analay full htto conversation using `tcpdump`

Load test with custom scenarion `wrl`

Implement HTTP server fromn scratch

Optimize connection polling

Understand TCP slow start impact on HTTP

Implement HTTP caching strategies

Debug performance bottlenecks using Wireshark****

**Continue in 07-01-HTTP**

## DNS

Resolution process

Record types

TTL

DNS cachin

Recursive vs Iterative queries

### Tasks

DNS lookup `nslookup`

 `dig`

+ Query specific record type

+ check nameservers

+ full dns query with trace

+ check dns propagation

+ reverse dns lookuip

+ check cache

+ diagones dns issues 

+ check dns respose time

+ test dns load balancing

+ debug dns resolutiuon

+ monitor dns queris

Set up local DNS server 

implement DNS caching in app

Configure DNS based load balacing

Understand DNSESEC

Debug split horizon DNS ISSUES

## TCP/UDP at app level

TCP 3-way handshake

TCP connection states

Port numbers and socket binding

Connection pooling 

UDP vs TCP trade-offs

### Tasks

Check listetning ports

Test port connectivity

Check establishde connections

Create Simple TCP client

Create Simple TCP server

Test UDP connectivity

Monitor network connection `warch -n 1`

Check connection states

Test connection timeput

Analyze TCP handsake

Monitor connection pooling 

Test connection limit

Debug socket options

Implement custom socket server with connection pooling

Optimize TCP parameters 

Handle half-open connections

Implement graceful shutdoen 

Debug TIME_WAIT issues

## WebSockets

WebSocket handshake

full duplex communication 

Frames and messages

Hearbeat / pin-poing

Scalling web sockets servers

### Tasks

`wscat`

Build simpe Web socket server

* implement nreconnection logic

* handlem message queuing

* monitor web socket connections

* tese web socket load balancing

Advanced Tasks:

- Implement WebSocket with authentication
- Handle binary data (ArrayBuffer/Blob)
- Implement custom protocol over WebSocket
- Scale WebSocket with Redis pub/sub
- Debug WebSocket connection issues

## TLS/SSL (Critical for Security)

Core Knowledge:

- Certificate chain validation

- Cipher suites

- TLS handshake

- Certificate pinning

- Perfect Forward Secrecy

### Tasks:

`openssl s_client`

View certificate details `echo | openssl s_client -connect google.com:443 2>/dev/null | \
openssl x509 -noout -text`

Check certificate expiry

Check supported cipher suites `nmap --script ssl-enum-ciphers -p 443 example.com`

Test SSL/TLS vulnerabilities `sslscan example.com`

Verify certificate chain openssl s_client -connect example.com:443 -showcerts | \
openssl x509 -noout -issuer -subject

Test certificate revocation (OCSP)

openssl ocsp -issuer issuer.pem -cert cert.pem \
-url http://ocsp.example.com

Generate self-signed certificate openssl req -x509 -newkey rsa:4096 -keyout key.pem \
-out cert.pem -days 365 -nodes

Debug TLS handshake openssl s_client -connect example.com:443 -debug -msg

Capture and analyze TLS traffic `tcpdump -i any -s 0 -w tls.pcap 'tcp port 443'`

Test SNI (Server Name Indication)

Check HSTS headers curl -I https://example.com | grep -i strict

Test TLS resumption openssl s_client -connect example.com:443 -reconnect

Master Level:

- Implement mutual TLS (mTLS)

- Configure perfect forward secrecy

- Implement certificate pinning

- Debug mixed content issues

- Optimize TLS performance

  ---

## REST API Design (Critical)

Core Knowledge:

- RESTful principles

- Resource naming conventions

- Idempotency

- Versioning strategies

- Rate limiting

- Pagination

- HATEOAS

  Beginner Tasks:

- Design simple CRUD API

- Document API with OpenAPI/Swagger

- Implement proper status codes

- Handle query parameters

- Implement basic error responses

  Intermediate Tasks:

- Implement API versioning (URL/header/content negotiation)

- Add pagination (cursor vs offset)

- Implement filtering and sorting

- Add rate limiting (token bucket/leaky bucket)

- Implement HATEOAS links

- Add request validation

  Advanced Tasks:

- Design idempotent operations

- Implement conditional requests (ETags, If-Modified-Since)

- Add bulk operations

- Implement partial updates (PATCH)

- Design long-running operations (async processing)

- Implement API gateway patterns

  Master Level:

- Design versioning strategy for breaking changes

- Implement distributed rate limiting

- Design API for high scalability

- Implement GraphQL or gRPC alternatives

- Optimize API performance (caching, compression)

  ---

## Authentication & Authorization (Critical)

Core Knowledge:

- Basic Auth

- OAuth 2.0 flows

- JWT (JSON Web Tokens)

- API Keys

- Session-based auth

- SAML

- OpenID Connect

Tasks:

Test Basic Auth `curl -u username:password https://api.example.com`

Test API key authentication curl -H "X-API-Key: your-api-key" https://api.example.com

Decode JWT echo "eyJhbGc..." | cut -d. -f2 | base64 -d | jq

Implement JWT authentication

Build OAuth 2.0 client

Implement refresh token flow

Add role-based access control (RBAC)

Implement session management

Handle token expiration

Advanced Tasks:

- Implement OAuth 2.0 authorization server

- Add multi-factor authentication (MFA)

- Implement token introspection

- Design permission system

- Implement SSO (Single Sign-On)

- Add audit logging

Master Level:

- Design distributed authentication system

- Implement fine-grained authorization (ABAC)

- Build secure token storage

- Implement OAuth 2.0 PKCE flow

- Design zero-trust architecture

## Caching Strategies (Important)

Core Knowledge:

- HTTP cache headers (Cache-Control, ETag, Last-Modified)

- CDN caching

- Application-level caching (Redis, Memcached)

- Cache invalidation strategies

- Cache-aside pattern

  Beginner Tasks:

  # 1. Check cache headers

  curl -I https://example.com | grep -i cache

  # 2. Test conditional requests

  curl -I -H "If-None-Match: \"etag-value\"" https://example.com

  # 3. Test with Redis

  redis-cli SET mykey "Hello"
  redis-cli GET mykey
  redis-cli EXPIRE mykey 60

  Intermediate Tasks:

- Implement HTTP caching with proper headers

- Build cache-aside pattern

- Implement cache warming

- Add cache invalidation logic

- Monitor cache hit rates

- Implement distributed caching

  Advanced Tasks:

- Implement write-through cache

- Design cache eviction policies

- Add cache stampede protection

- Implement multi-layer caching

- Optimize CDN configuration

- Handle cache consistency

  Master Level:

- Design distributed cache invalidation

- Implement probabilistic data structures (Bloom filters)

- Optimize cache performance

- Design cache for high availability

- Handle cache poisoning attacks

  ---
9. API Security (Critical)

   Core Knowledge:
- OWASP API Security Top 10

- Input validation

- SQL injection prevention

- XSS prevention

- CSRF protection

- Rate limiting

- API throttling

  Beginner Tasks:

  # 1. Test for common vulnerabilities

  curl -X POST https://api.example.com/login \
  -d "username=admin' OR '1'='1"

  # 2. Test rate limiting

  for i in {1..100}; do curl https://api.example.com; done

  # 3. Check security headers

  curl -I https://example.com | grep -i "x-frame-options\|x-content-type"

  Intermediate Tasks:

- Implement input validation

- Add request sanitization

- Implement rate limiting per user

- Add CORS configuration

- Implement API key rotation

- Add request signing

  Advanced Tasks:

- Implement WAF rules

- Add DDoS protection

- Implement request throttling

- Add anomaly detection

- Implement IP whitelisting/blacklisting

- Add security monitoring

  Master Level:

- Design zero-trust API architecture

- Implement advanced threat detection

- Build API security gateway

- Implement circuit breakers

- Design for resilience against attacks

  ---
10. Practical Debugging Scenarios

    Scenario 1: Slow API Response

    # 1. Check DNS resolution time

    time dig example.com

    # 2. Check connection time

    curl -w "@curl-format.txt" -o /dev/null -s https://example.com

    # curl-format.txt:

    # time_namelookup: %{time_namelookup}

    # time_connect: %{time_connect}

    # time_starttransfer: %{time_starttransfer}

    # time_total: %{time_total}

    # 3. Trace route

    traceroute example.com

    # 4. Check for packet loss

    ping -c 100 example.com

    Scenario 2: Connection Refused

    # 1. Check if port is open

    nc -zv example.com 443

    # 2. Check firewall rules

    iptables -L -n

    # 3. Check if service is listening

    ss -tuln | grep :443

    # 4. Check DNS resolution

    dig example.com +short

    Scenario 3: SSL/TLS Errors

    # 1. Check certificate validity

    openssl s_client -connect example.com:443 | openssl x509 -noout -dates

    # 2. Verify certificate chain

    openssl s_client -connect example.com:443 -CApath /etc/ssl/certs

    # 3. Check supported protocols

    nmap --script ssl-enum-ciphers -p 443 example.com

    Scenario 4: CORS Issues

    # 1. Test CORS preflight

    curl -H "Origin: http://localhost:3000" \
    -H "Access-Control-Request-Method: POST" \
    -H "Access-Control-Request-Headers: X-Requested-With" \
    -X OPTIONS --verbose \
    https://api.example.com/endpoint

    # 2. Check CORS headers in response

    curl -H "Origin: http://localhost:3000" \
    -I https://api.example.com

    ---

    Key Tools Every Senior Developer Should Master

11. curl - HTTP client testing

12. dig/nslookup - DNS queries

13. netstat/ss - Network connections

14. tcpdump/wireshark - Packet capture

15. openssl - SSL/TLS debugging

16. nc (netcat) - TCP/UDP testing

17. jq - JSON processing

18. ab/wrk - Load testing

19. postman/insomnia - API testing

20. browser DevTools - Network inspection

    ---

    Progressive Learning Path

    Week 1-2: HTTP Fundamentals
- Master curl and HTTP methods

- Understand headers and status codes

- Learn about cookies and sessions

  Week 3-4: DNS & TCP/IP

- Practice DNS lookups

- Understand connection management

- Debug network issues

  Week 5-6: Security

- Learn TLS/SSL basics

- Implement authentication

- Study OWASP guidelines

  Week 7-8: Performance

- Implement caching

- Optimize API responses

- Practice load testing

  Week 9-10: Advanced Topics

- WebSockets and real-time

- API gateway patterns

- Distributed systems concepts

  Week 11-12: Master Level

- Build complete API from scratch

- Implement all security best practices

- Performance tuning and optimization

- Debug complex production issues

  This guide provides a structured approach to mastering Application Layer concepts
  with practical, hands-on tasks that progress from basic to advanced levels.
```
