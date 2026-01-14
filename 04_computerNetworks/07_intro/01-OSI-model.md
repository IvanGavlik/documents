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

Physical layer
* converts binary signals into electric/radio and vice versa
    * Manchester encoding (0 - high to low transition 1 - low to high transition)
    * 4B/5B encoding
    * 8B/10B encoding
* moves signals between devices in the network
* defines characteristics of the hardware (cabel types, connector types, transmission modes) Ex: Frequency, Maximumm length for 1Gbps transmission
* clock synchronization: sender and receiver are operating at the same rate (preventing data loss or corruption)
    * syn: sender and receiver share a common clock signal
    * asy: start and stop bits 

Data carrying capacity in the physical layer
* bandwidth - capacity of the line (bp/s - bits per second kb/s - kilobit per second mb/s megabit per second)
* throughput - actual transferred data over time (usually less than bandwidth)
* goodput - actual useable data transferred over time

Devices
* hub transmit data one at time all devices on the network recieves it (performance issue)  one way communication
* switch each port on the switch has its own collision domain support for bidirectional communication (this is at data-link layer) 


Data link layer 
* converts data packets into frames (In the header source and destination MAC address in the trailer CRC value) 
* reads signals converts them into frames
    * character-oriented framing
    * bit oriented framing
    * clock based framing 
* frame
    * preamble (syn pattern) 7 bits
    * start frame delimiter (1 byte)
    * destincation/source MAC (6 byte)
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
    * Cyclic Redundancy Check (CRC)
        * math on the payload result added to frame reciver same 
    * Parity checking
    * Checksum
* flow control to prevent a fast sender from overwhelming a slow receiver
    * stop and wait 
    * sliding window
        * sender sends n frames before receiving ack
        * reciever ack frames and slides dor n frames 
* access control listen for carrier signal if channel is idle transmit if collision stop wait a random time and retry

Address Resolution Protocol (ARP)


TODO
* Explain network latency 
* Switches
* Docker bridge networks
* why localhost is faster then LAN
* Docker container to container communication
* packet loss, jitter

* Institute of Electrical and Electronic Engineers (IEEE) 802.2, 802.3, and 802.5; ANSI’s FDDI; Ethernet II; Asynchronous Transfer Mode (ATM), Frame Relay, High-Level Data Link Control (HDLC), Point-to-Point Protocol (PPP), Synchronous Data Link Control (SDLC), Serial Line Internet Protocol (SLIP), and X.25 are examples of data link layer protocols and standards.

Network interface controllers or cards (NICs), bridges, and switches are the primary networking components that function at the data link layer.

* Signal encoding: How bits become electrical/optical/radio signals (NRZ, Manchester
  encoding)
* Bandwidth vs throughput: Understanding real-world limitations
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
* ARP (Address Resolution Protocol): IP to MAC resolution, ARP cache, ARP poisoning
  attacks
* Practical Skills:
  - Debug broadcast storms and switching loops
  - Configure VLANs for network segmentation
  - Understand why containers share host MAC address
  - Troubleshoot ARP cache issues
  - Recognize symptoms of MAC table overflow attacks
  - Know when MTU mismatch causes "black hole" routes
  - Understand switch vs hub behavior in packet capture scenarios  
  
## Network / Internet Layer

Routing packets across networks using IP addresses

TODO
* Every device needs an IP
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
