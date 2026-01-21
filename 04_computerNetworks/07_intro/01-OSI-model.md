# OSI Model & TCP/IP stack

OSI model 
* 7 layers
* theoretical framework 

OSI MODEL       -> TCP/IP Model  -> what you work with 
7. Application
6. Presentation -> 4. Aplication -> HTTP, DNS, SSH, FTP
5. Session 

4. Transport    -> 3. Transport  -> TCP, UDP

3. Network      -> 2. Internet  -> IP, ICMP, routing

2. Data Link    -> Network Access -> WIFI, cables
1. Physical

## How data is transferred

So it depends on if you’re looking at the OSI layer from the top down or bottom up but it really doesn’t matter.

Think about how the data is transferred:

PC-A wants to talk to PC-B which is on the other side of the world. The user opens up an application which gets wrapped in the presentation layer, which gets wrapped in the session layer (as network admins/engineers we don’t really care about these layers) which then gets wrapped in the transport layer where ports and protocols like TCP 80 or UDP 53 are used. All of this is the wrapped up by the network layer where the a source and destination IP address are inserted into the IP header. After this, the packet is wrapped up by the data link layer where the destination and src MAC address are inserted into the frame. At this point we encode, the frame into ones and zeros to be sent across whatever physical medium is used to transfer the data (RF/light pulses/electrical pulses).

On the far side this all happens in reverse. Ones and zeros are received on the physical interface. Which is reconstructed as a frame. We look at the frame header and if the destination MAC is our MAC address we remove the frame header and expose the network header where we look at the destination IP address. If it is ours we remove that and expose the transport header and use the ports and protocol to continue to pass it up the OSI model.

If after we remove the L2 header we see that the IP is not ours( think of what a router does) we look at the routing table to see if we have a route to the destination is so we generate a new L2 header to wrap the packet in to create a new frame and then re-encode the 1s and 0 on the physical layer towards the destination. 

## Physical + Data Link / Network Access

Physical transmission of bits: MAC addresses, switches

### Physical layer
* converts binary signals (0s and 1s) into physical signals (electric/radio) and vice versa
    * NRZ: direct voltage mapping high = 1 low = 0 (long sequences of same bit no transitions)
    * NRZ-I (inverted) 
    * Manchester encoding (0 - high to low transition 1 - low to high transition)
        * self-clocking
        * 2x bandwidth (two transitions per bit)
    * Differential Manchester
    * 4B/5B 4 data bits to 5 bit patterns 
        * first: 4 data bits -> 5 code bits (use lookup table) 
        * second: 5 code bits into -> Physical signal (1 = transition, 0 = transition)
        * cost mesium
        * dominate 100Mbps networks in the 1990s - 2000s replaced by 8B/10 for speeds > 1Gbps
    * 8B/10B 8 data bits to 10 code bits
        * 12 contorol codes for protocol contorl 
    * 64B/66B for speeds > 10Gbps
    
* moves signals between devices in the network
* defines characteristics of the hardware (cabel types, connector types, transmission modes) Ex: Frequency, Maximumm length for 1Gbps transmission
* clock synchronization: sender and receiver are operating at the same rate (preventing data loss or corruption)
    * syn
        * sender and receiver share a common clock signal (on the start it has clock sync)
        * continous data stream at a constant rate (no idle periods
        * efficent for bulk data transfer
    * asy
        * start and stop bits 
        * legacy systems, simple embedded devices
    * communication protocol dictates mode hardware has to have it implemented (sometime software configuration/drivers)

Data carrying capacity in the physical layer
* bandwidth - capacity of the line (bp/s - bits per second kb/s - kilobit per second mb/s megabit per second)
* throughput - actual transferred data over time (usually less than bandwidth)
* goodput - actual useable data transferred over time

Devices
* hub transmit data one at time all devices on the network receives it (performance issue)  one way communication
* switch each port on the switch has its own collision domain support for bidirectional communication (this is at data-link layer) 

Linux Network interface naming
* 01-01-00-Network-interface-naming.md

### Data link layer 
* converts data packets into frames (In the header source and destination MAC address in the trailer CRC value) 
* reads signals converts them into frames
    * character-oriented framing
    * bit oriented framing
    * clock based framing 
* frame
    * preamble (syn pattern) 7 bits
    * start frame delimiter (1 byte)
    * destination/source MAC (6 byte)
    * ether type (2 bits)
    * payload (45-1500 byte)
    * CRC frame check sequence (4 bytes)
* physical addressing MAC address
    * every network device has a permanent MAC address from the factory.
    * 48 bits (6 bytes)
    * hexadecimal
    * first 3 bytes organizationally unique identifier
    * last 3 bytes network interface controller 
    * ARP map IP to MAC (Address Resolution Protocol) 
* error detection (does not preform error correction) - uses CRC value to confirm that frame is good if not good it is deleted
    * Parity checking Add to the end 1 bit so the number of 1s is even (or odd)
    * Two-dimensional parity 
        * data in rows and colums 
        * parity for each row and column
    * Checksum
        * IP, TCPM UDO
        * data into fixed size then words are added using 1's complement sum is checksum
        * TODO complement arithmetic
    * Cyclic Redundancy Check (CRC)
        * Ethernet Wi-Fi
        * data as binary polynomial TODO
        * math on the payload result added to frame reciver same 
    * what happens when we detect the error TODO ? 

* flow control to prevent a fast sender from overwhelming a slow receiver
    * stop and wait 
    * sliding window
        * sender sends n frames before receiving ack
        * reciever ack frames and slides dor n frames 
* access control listen for carrier signal if channel is idle transmit if collision stop wait a random time and retry

Address Resolution Protocol (ARP)
* 01-01-01-Address-Resolution-Protocol.md

Neighbor Discovery Protocol (TODO)
* same as ARP but for IPv6

TODO
* Explain network latency 
* Switches
* Docker bridge networks
* Docker container to container communication
* packet loss, jitter

* Institute of Electrical and Electronic Engineers (IEEE) 802.2, 802.3, and 802.5; ANSI’s FDDI; Ethernet II; Asynchronous Transfer Mode (ATM), Frame Relay, High-Level Data Link Control (HDLC), Point-to-Point Protocol (PPP), Synchronous Data Link Control (SDLC), Serial Line Internet Protocol (SLIP), and X.25 are examples of data link layer protocols and standards.

Network interface controllers or cards (NICs), bridges, and switches are the primary networking components that function at the data link layer.

* Signal encoding: How bits become electrical/optical/radio signals (NRZ, Manchester
  encoding)
* Latency sources: Propagation delay, transmission delay, processing delay
* Cable specifications: When to use shielded vs unshielded, distance limitations
* Physical topologies: Star, mesh, bus implications for redundancy and failure modes
* Practical Skills:
  - Debug "cable unplugged" vs actual Layer 1 issues
  - Understand why 100m is Ethernet's copper limit
  - Know when fiber is necessary (distance, EMI, security)
  - Recognize duplex mismatch symptoms
  - Understand PoE (Power over Ethernet) capabilities and limitations
* MAC addresses: 48-bit addressing, OUI vendor identification, broadcast
  (FF:FF:FF:FF:FF:FF)
* Ethernet frames: Structure, MTU (typically 1500 bytes), jumbo frames (9000 bytes)  
* Switching: MAC address tables, learning, flooding, aging
* VLANs: 802.1Q tagging, trunk vs access ports, native VLAN
* Spanning Tree Protocol (STP): Loop prevention, convergence time, RSTP improvements
* Practical Skills:
  - Debug broadcast storms and switching loops
  - Configure VLANs for network segmentation
  - Understand why containers share host MAC address
  - Troubleshoot ARP cache issues
  - Recognize symptoms of MAC table overflow attacks
  - Know when MTU mismatch causes "black hole" routes
  - Understand switch vs hub behavior in packet capture scenarios  

### Bandwidth vs Throughput vs Latency 

Bandwidth 
* data carrying capacity measured in bits per second bps, megabits per second Mbps or gigabits per second Gbps (it is not about speed)
* determined by factors like cable type, Wifi standard, guaranteed by providers
* maximum rate at which data can be transferred over a connection in a give time 
*   Kilobits per second (Kbps): 1,000 bits per second
    Megabits per second (Mbps): 1,000,000 bits per second
    Gigabits per second (Gbps): 1,000,000,000 bits per second
    Terabits per second (Tbps): 1,000,000,000,000 bits per second
* types
    symmetric: uploading and downloading are same
    asymmetric downloading is higher
* Popular testing options:
    Speedtest.net
    Fast.com
    Google Speed Test
* values needed 
    steaming video 3 - 5 Mbps
    music 500 Kbps
    gaming 10 Mbps
    conferences 3 Mbps 

Throughput
* data that is reaching a user (actual data transferred successfully)
* reflect the real world performance of the network (it is realistic measurement)
* can vary due to the network congestion, errors and other factors
* influenced by latency 
* measured bits transferred per second (work an any of the layers)
* usually 60-95% bandwidth

Latency
* how late data is coming to the user (speed) 
* measurement milliseconds (ms) microseconds (qs)
* typical values 
    * LAN: 0.5 - 2 ms 
    * WAN 20 - 100 ms
* affected by distance and processing
* often is specified for critical apps
* If your bandwidth is limited, latency may be increased. (TODO: investigate)
* Latency does not impact bandwidth, but bandwidth does affect latency
    * bandwidth width of the road
* TODO stao https://www.zenarmor.com/docs/network-basics/what-is-network-latency    

### Numbers in general TODO
You should instantly know: TODO
* 100 KB ≈ 0.1 MB
* 1 MB ≈ 8 Mb
* 10 Mbps ≈ 1.25 MB/s


#### Numbers Bandwidth
* 3G 1-5 Mbps
* 4G 10-50 Mbps
* 5G 50 - 300 Mbps
* Wi-Fi 50 - 500 Mbps
* LAN 1 Gbps
* Data center 10 - 10 Gbps

Typical payload size
* HTML page 10-50 KB
* REST JSON reponse 1-20 KB 
* JWT token 0.5 - 2 KB
* CSS 5 - 50 KB
* JS bundle 50 - 500 KB 
* image 50 - 300 KB
* font file 30 - 100 KB

Raise alarm if
* API response > 100 KB
* JWT > 2 KB
* HTML > 100 KB
* initial js bundle > 500 KN
* page load > 2 MB
* pre request headers > 5kb 

If app needs more than 10 Mpsp per user something is wrong 
* 10 Mbps = ~1.25 MB per second
* Is my app sending or receiving more than ~1.25 MB every second, per active user, for more than a short burst?
    * check if usage is bursty or continuous (Bursty = page load, export, improt) but if continuous then it is read flag
* Take one typical user action and estimate:
    (Total bytes sent + received) / Time window (seconds)
* Example:
  HTML: 30 KB
  JS/CSS cached
  API calls: 5 × 10 KB = 50 KB
  Images: 100 KB
  Total: 180 KB
  So in 1 second: 180 KB ≈ 0.18 MB ≈ 1.4 Mbps

* how to do this 
    * Open DevTools -> Network 
    * Reload page
    * look at total transferred and finish time
    * Mbps (Total MB x 8) / seconds
    * Example 6MB tansfered 3 seconds (6 × 8) / 3 = 16 Mbps -> Why so much data 

* how to check api response > 100 KB
    * Open DevTools -> Network 
    * make api request 
    * Look at Size / Transferred (Units are usually KB/MB)
    * Compare to 100 KB threshold

* how to check api response > 100 KB on BE (server side logging)
    * Log Content-Length header for every response:
    * anything > 100 KB for a single API call is usually too big unless streaming a file.

* JWT > 2 KB
    * in browser DevTools → Network, look at request headers
    * Inspect Authorization header
    * Count bytes:
        Each Base64 character = 1 byte
        Example: 172-character token ≈ 172 bytes → fine
* HTML > 100 KB
    * Network tab → check index.html file → Size / Transferred
    * curl -s https://example.com | wc -c
* Initial JS bundle > 500 KB
    * DevTools → Network → look for main .js bundle → Size / Transferred
* Page load > 2 MB
    * Total page weight = sum of HTML + JS + CSS + images + fonts.
    * Network tab → look at “Transferred” at bottom → total bytes

* server side calculation (use logs or metrics)
    * avr response size x request per second per user
    * Ex; avr response 200 KB req/seq 5 -> 200 KB × 5 = 1000 KB ≈ 1 MB/sec ≈ 8 Mbps (Close to the danger zone)
    
Microservices 
* internal service calls 
    * request size < 5KB
    * response size < 20 KB
    * calls per request < 10
* Kafka / messaging
    * event size < 1 KB (1-10 KB is stil normal)

* If your app is: Not streaming Not transferring files Not doing real-time video
Then: Your problem is data shape, not network capacity.

### Numbers 
You should instantly know: TODO
* 100 KB ≈ 0.1 MB
* 1 MB ≈ 8 Mb
* 10 Mbps ≈ 1.25 MB/s

Core formula (What Bandwidth Means in Time) TODO practice tasks

time (seconds) = data size (bits) / bandwidth (bits per second)

Everything else is unit conversion.
* Bandwidth is measured in bits per second (bps)
* Data size is usually measured in bytes (B)
* 1 byte = 8 bits

Data	Bytes	Bits
10 KB	10,000 B	80,000 bits
100 KB	100,000 B	800,000 bits
1 MB	1,000,000 B	8,000,000 bits
10 MB	10,000,000 B	80,000,000 bits

Rule of thumb
10 Mbps ≈ 1.25 MB per second because 10 Mb / 8 = 1.25 MB

Key insight
* Most API responses are: <100 KB
* Transfer time: <100 ms
* Latency: 50–300 ms

Latency dominates, not bandwidth. That’s why:
* Reducing payload size helps
* But reducing round trips helps more

#### Numbers Throughput

#### Numbers Latency

TODO have task for numbers

## Network / Internet Layer

Routing packets across networks using IP addresses

TODO
* Every device needs an IP
* what is subnet
* How packets find their destination
* subnetting: grouping IPs into networks
* NAT and why services can't see each other
* ICMP 
* IPv4 vs IPv6
* public vs private IPs
* How containers get IPs
* VPC/subnet configuration in cloud
* one way connectivity 
* firewall / securtiy group issuess

## Transport Layer

End to end communication, reliability, ports

TODO
* TCP handshake and teardown
* congestion controll and backoff
* retransmissions and timeouts
* head of line blocking
* high councurrency kills performance
* retry storms
* learn about latency 

TCP - transmission control protocol
* reliable: guarantees delivery
* connection-oriented: handshake required
* ordered: packets arrive sequence
* used for http, ssh, database
* slower but safe

UDP - user datagram protocol
* unreliable: fire and forget
* connectionless : no handshake
* unordered: packets may arrive out of the order
* used for video, streaming, dns, gaming
* faster but risky

### TCP handshake
Client -> Server
-> SYN (synchronize)
<- SYN-ACK (acknowledge)
-> ACK
connection established

### Ports
* 0 - 65535 (identify app)
* well know ports: 0 - 1023 (80 http, 433 https 22 ssh)
* registered ports; 1024 - 49151 (dev, servers)
* dynamic ports (49152 - 65535)

## Application Layer

Application level protocols: HTTP, FTP, SMTP, DNS

TODO
* TLS handshake
* certificate chains and expistaion 
* serialization trade-off (Json VS protobuf)
* why first request slow
* http and status codes
* idempotency and retry safety 
* timeouts and backpressure


You should be able to explain:
* Why HTTP retries can corrupt data
* Why TCP throughput collapses under packet loss
* Why load balancers cause 504s
* Why TLS affects cold-start latency
* Why networks fail partially, not completely

### HTTP request flow through layers

When you do fetch("hhtps://api.example.com/users")

Layer 7 (Application): "get /users HTTP/1.1"
Layer 4 (Transport): add source and destination port establish TCP connection (SYN, SYN-ACK, ACK)
Layer 3 (Network): Add source and destination IP and do routing
Layer 2 (Data Link): Add MAX addressed, frame the data
Layer 1 (Physical): Convert to el. signals

## When debugging:
Application not working?
  ↓
Is DNS resolving? (Layer 3/7) → nslookup
  ↓
Can I reach the host? (Layer 3) → ping
  ↓
Is the port open? (Layer 4) → telnet/nc
  ↓
Is the application responding? (Layer 7) → curl -v

## When designing systems:
  - Layer 4 LB: Fast, IP/port only, no HTTP inspection
  - Layer 7 LB: Slower, can route by URL, headers
  - Use TCP for reliability (APIs, databases)
  - Use UDP for speed (streaming, real-time games)


## Tasks

1. Visualize HTTP through layers
Install tcdump or wireshardk make a simple http request and capture it (curl -v xxxx)

2. Understand TCP handshake
Use netcat or telnet to manually observe TCP connection

3. Explore ports 
See what's listening on your machine sudo netstat -tuln
Check which process is using port 3000 (sudo lsof -i :3000)

4. Test TCP vs UDP
Create two simple servers one TCP other UDP
Send data with netcat: nc localhost 5000 (TCP) vs nc -u localhost 5001 (UDP)
What's the difference in behavior

5. Packet capture analysis
Install wireshar capture http traffic (sudo tcpdump -i any -n port 80 -w capture.pcap)
In another terminal make request curl http://neverssl.com
then stop tcpdump and analyze with wireshark capture.pcap

6. Build a simple proxy server
- how proxies work at Layer 4 (TCP) level

7. Implement TCP connection pooling
- why connection pooling matters 

8. Analyze layer overhead
See how much overhead each layer adds sudo tcpdump -i any -n -v host example.com
Make a request curl http://example.com
  # Analyze packet sizes:
  * Ethernet header:
  + IP header: 
  * TCP header:
  * Your actual data (Layer 7)
Why small packets are inefficient

9. Implement app-layer protocol Create a custom protocol on top of TCP
How HTTP, gRPC are build on top of TCP
message framing and parsing 
layer separation

10. Debug Docker networking layers
  # Create custom Docker network
  docker network create --driver bridge my-network

  # Inspect network (see Layer 3 subnet allocation)
  docker network inspect my-network

  # Run containers
  docker run -d --name web --network my-network nginx
  docker run -d --name app --network my-network node:alpine sleep 3600

  # Exec into container
  docker exec -it app sh

  # Inside container, install tools
  apk add curl tcpdump

  # See Layer 3: Can ping by container name (DNS)
  ping web

  # See Layer 4: TCP connection
  curl http://web

  # See Layer 2: Check network interfaces
  ip addr

  # See routing table (Layer 3)
  ip route

  What to learn: How container networking maps to OSI model
  
11. Implement TCP state machine Learn TCP state transitions, why TIME_WAIT exists 
12. Layer 7 load balancer (Layer 7 vs Layer 4 (TCP) - loadin balancing
13. Build a packet sniffer
14. Impl congestion control


 📚 Resources by Level

  Beginner Resources

  1. Interactive & Visual
    - https://www.cloudflare.com/learning/network-layer/
    - https://www.youtube.com/watch?v=nomyRJehhnM (YouTube)
    - https://hpbn.co/ by Ilya Grigorik (FREE!)
  2. Practical
    - https://www.pearson.com/en-us/subject-catalog/p/computer-networking/P200000003334 (Kurose & Ross) - Chapter 1-4
    - https://beej.us/guide/bgnet/ (FREE, C examples)
  3. Hands-on
    - https://www.wireshark.org/docs/wsug_html_chunked/
    - Play with netcat: man nc

  ---
  Intermediate Resources

  1. Books
    - https://www.amazon.com/TCP-Illustrated-Vol-Addison-Wesley-Professional/dp/0201633469 (Stevens) - THE classic
    - https://www.amazon.com/Computer-Networks-5th-Andrew-Tanenbaum/dp/0132126958 (Tanenbaum) - More comprehensive
  2. RFCs (actual specs)
    - https://tools.ietf.org/html/rfc793
    - https://tools.ietf.org/html/rfc791
    - https://tools.ietf.org/html/rfc2616
  3. Online Courses
    - Stanford CS144: https://www.youtube.com/playlist?list=PLvFG2xYBrYAQCyz4Wx3NPoYJOFjvU7g2Z
    - Coursera: https://www.coursera.org/learn/computer-networking

  ---
  Advanced Resources

  1. Deep Dives
    - http://www.tcpipguide.com/ - Encyclopedic
    - https://wiki.linuxfoundation.org/networking/kernel_flow
    - https://www.oreilly.com/library/view/understanding-linux-network/0596002556/ (Benvenuti)
  2. Research Papers
    - https://ee.lbl.gov/papers/congavoid.pdf (Jacobson)
    - https://queue.acm.org/detail.cfm?id=3022184 (Google)
  3. Tools & Practice
    - https://packetlife.net/ - Packet captures & cheat sheets
    - Build your own TCP/IP stack: https://www.saminiir.com/lets-code-tcp-ip-stack-1-ethernet-arp/
    - https://github.com/topics/linux-network-programming

  ---
  Reference Materials

  1. Cheat Sheets
    - https://packetlife.net/media/library/23/OSI.pdf
    - https://packetlife.net/media/library/38/TCP.pdf
    - https://packetlife.net/media/library/23/Common_Ports.pdf
  2. Tools Documentation
    - man tcpdump
    - man netstat
    - man ss (modern netstat)
    - https://www.wireshark.org/docs/wsug_html_chunked/




https://www.quora.com/What-networking-knowledge-should-all-senior-developers-have
https://dev.to/enter?state=new-user&bb=239387
https://www.cloudflare.com/learning/
