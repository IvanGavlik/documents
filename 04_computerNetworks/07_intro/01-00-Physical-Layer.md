# Physical Layer

## Goals

1. Concepts Bandwidth is not same Throughput

2. Latency sources at the physical layer
   
   * signal propagation 
   * encoding / decoding
   * medium contention (wifi)

3. Signal degradation
   
   * distance
   * cheap cables
   * interference (power cables, bluetooth)
   * see this as
     * TCP trtansmissions
     * timeouts
     * random failures

4. Duplex and speed negotiation 
   
   * one side: full duplex
   * other side: half duplex

5. MTU and physical constraints

6. Hands-on
   
   * ethtool eth0
     * link speed
     * duplex
     * auto negotiation
   * ping -s 1472 <host>
     * mtu discovery
   * mtr <host>
     * spot packet loss patterns
   * iperf3

7. Raw throughput testing (bypasses your app)

8. Signal Encoding & Bandwidth
   
   Why it matters: Understanding throughput limitations and performance bottlenecks
   
   - Bandwidth limitations: Real-world constraints affect API design and data transfer
     strategies
   - Latency vs throughput: Know when each matters (streaming vs bulk transfers)
   - Signal degradation: Distance affects quality - impacts distributed system design
   - Practical impact:
     - Design chunked uploads for large files
     - Implement progressive loading strategies
     - Set realistic timeout values based on expected bandwidth

9. Physical Media Types
   
   Why it matters: Performance tuning and infrastructure decisions
   
   - Copper (Ethernet): 100m max distance, susceptible to interference
   - Fiber optic: Long distances, immune to EMI, higher bandwidth
   - Wireless: Variable latency, packet loss, design for unreliability
   - Practical impact:
   - Mobile-first designs handle high latency/packet loss
   - Implement retry strategies with exponential backoff
   - Cache aggressively for wireless clients

10. Network Interface Cards (NICs)
    
    Why it matters: Performance optimization and troubleshooting
    
    - MAC addresses: Hardware addressing, container networking, spoofing implications
    - Duplex modes: Full vs half-duplex affects collision detection
    - Offloading: TCP checksum offload, segmentation offload (TSO/GSO)
    - Practical impact:
    - Docker networking uses virtual MAC addresses
    - NIC queues can bottleneck - monitor ethtool stats
    - Offloading can mask bugs in packet construction

11. Signal Quality & Error Detection
    
    Why it matters: Reliability and error handling
    
    - Bit error rates (BER): Physical errors happen, upper layers must handle
    - CRC/checksums: Physical layer error detection (not correction)
    - Practical impact:
    - Never assume data integrity - always validate at application layer
    - End-to-end checksums (HTTP ETags, file hashes)
    - Implement application-level error correction for critical data

12. Cable Categories & Specs
    
    Why it matters: Infrastructure planning and troubleshooting
    
    - Cat5e: 1Gbps up to 100m
    - Cat6/6a: 10Gbps, better shielding
    - Fiber types: Single-mode (long distance) vs multi-mode (short distance)
    - Practical impact:
    - Data center rack design
    - Diagnose "works on my machine" issues related to office vs production networking
    - Budget for proper cabling in infrastructure planning

13. Physical Layer Protocols
    
    Why it matters: Understanding the foundation of network stacks
    
    - Ethernet (802.3): Frame structure, collision detection (CSMA/CD)
    - Wi-Fi (802.11): CSMA/CA, beacons, association
    - Practical impact:
    - Ethernet frame MTU (1500 bytes) affects packet fragmentation
    - Wi-Fi contention increases latency under load
    - Design APIs to minimize round trips

14. Power over Ethernet (PoE)
    
    Why it matters: IoT and edge device deployment
    
    - PoE standards: Different power budgets (15W, 30W, 60W, 100W)
    - Practical impact:
    - IoT device deployment constraints
    - Power budget affects processing capability
    - Edge computing architecture decisions

15. Practical Troubleshooting, what senior devs actually do:
    
    * Check physical interface status `ip link show` `ethtool eth0`
    
    * Monitor errors/drops `netstat -i` `ip -s link`
    
    * Cable quality tests `ethtool --test eth0`
    
    * Speed/duplex negotiation issues `ethtool eth0 | grep Speed`

16. Common Physical Layer Issues You'll Debug
    
    1. Duplex mismatch: Auto-negotiation failure causing 50% packet loss
    2. Cable length exceeded: Intermittent connectivity, high error rates
    3. EMI interference: Near power cables, motors, fluorescent lights
    4. Bad connectors: Intermittent connection, high retransmit rates
    5. NIC driver issues: Kernel versions, firmware updates needed
    6. MTU mismatches: Fragmentation issues, black hole routing

17. Performance Implications
    
    - Latency contributors:
      - Signal propagation: ~5μs/km in fiber
      - Serialization delay: Time to put bits on wire
      - Queuing delay: NIC buffers
    - When to care:
      - High-frequency trading: Microseconds matter
      - Real-time gaming: <50ms total latency budget
      - Video conferencing: Jitter and packet loss
      - Standard web apps: Focus on higher layers

18. Key Takeaway for Senior Developers
    
    The Physical Layer is abstracted away 99% of the time, but knowing it helps you:
    
    - Design resilient systems that handle unreliable networks
    - Debug production issues when "the network is slow"
    - Make infrastructure decisions about cloud vs on-prem, CDN placement
    - Set realistic expectations for latency, throughput, and reliability
    - Interview well when discussing system design

## Tasks

### Basic

1. Differences between (Focus on latency, reliability, and interference.):
   
   * Twisted-pair copper Ethernet
   * Fiber-optic cabling
   * Wireless transmission

2. Bandwidth vs Throughput
   
    Describe a real production scenario where Bandwidth is high Throughput is low Explain what could be happening at the physical layer.

3. List all network interfaces on your machine and identify:                                
   
   - Which are physical vs virtual                                                          
   - Their MAC addresses                                                                    
   - Current operational state (up/down)                                                    
   - Link speed and duplex mode                                                            

4. Without using `ping`:                                                                    
   
   - Verify if a network cable is plugged in                                                
   - Check if link is established                                                           
   - Determine the negotiated speed                                                         
   - Identify if interface has carrier signal                                              

5. MTU  Investigation                                                          
   
   - Find the MTU of your primary network interface                                         
   - Discover what happens when you try to send packets larger than MTU                     
   - Test different MTU sizes to a remote host                                              
   - Document where fragmentation occurs           

6. MAC Address Analysis                                                       
   
   - Find your NIC's MAC address                                                            
   - Identify the manufacturer from the OUI (first 3 octets)                                
   - Locate where MAC addresses are used in your OS                                         
   - Observe MAC address behavior in Docker containers

### Intermedia

1. Duplex Mismatch Scenario
   
    A service shows
   
   * Very low throughput
   
   * High CPU wait
   
   * No application errors
     
     Design a checklist to verify whether duplex mismatch is the root cause.

2. MTU Discovery
    You notice:
   
   * Small HTTP requests succeed
   
   * Large uploads hang
     
     Create a step-by-step investigation plan to validate an MTU issue.

3. Latency Breakdown
    Given two services One deployed in the same rack One deployed in another region
    List all physical-layer contributors to latency between them.

4. Match each tool to the physical-layer insight it provides:
   
   * `ping`
   
   * `iperf3`
   
   * `ethtool`
   
   * `mtr`
     
     Explain why each is useful beyond application logs.

5. Error Rate Monitoring Set up continuous monitoring to:                                                         
   
   - Track packet errors, drops, and collisions                                             
   - Calculate error rates over time                                                        
   - Correlate errors with network activity                                                 
   - Create alerts for abnormal error rates

6. Performance Baseline: Establish performance baselines for your network:                                        
   
   - Measure bandwidth capacity                                                             
   - Measure latency (propagation + serialization + queuing)                                
   - Test throughput with different packet sizes                                            
   - Document how performance varies by time of day  

7. Duplex Mismatch Simulation                                                 
   
   - Force a duplex mismatch between two devices                                            
   - Document the symptoms (packet loss, errors, throughput)                                
   - Measure the performance impact                                                         
   - Develop a diagnostic procedure to detect this issue

8. NIC Statistics Deep Dive
   
   - Identify what each counter measures                                                    
   - Find counters for TX/RX errors, drops, overruns, frame errors                          
   - Correlate counter changes with specific network activities                             
   - Create a dashboard to visualize key metrics

9. Compare physical layer characteristics:                                                  
   
   - Latency variation (jitter) over 1000 packets                                           
   - Packet loss rates under normal and heavy load                                          
   - Throughput consistency                                                                 
   - Impact of distance and obstacles on wireless  

10. MTU Optimization For a specific application (file transfer, video stream, or gaming):                     
    
    - Determine optimal MTU size                                                             
    - Measure impact of different MTU values                                                 
    - Handle Path MTU Discovery                                                              
    - Document trade-offs between large and small MTUs                                      

11. NIC Offloading Investigation Research and test NIC offloading features:                                               
    
    - Identify available offload features (TSO, GSO, GRO, LRO, checksum)                     
    - Measure CPU usage with offloading enabled vs disabled                                  
    - Test throughput impact                                                                 
    - Identify scenarios where offloading causes problems 

12. Optimize NIC interrupt handling:                                                         
    
    - Monitor interrupt rates and CPU usage                                                  
    - Implement interrupt coalescing                                                         
    - Test NAPI (New API) vs legacy interrupts                                               
    - Measure latency vs throughput trade-offs

13. Multi-Queue NIC Configuration Configure and optimize multi-queue NICs:                                                 
    
    - Identify number of RX/TX queues                                                        
    - Map queues to CPU cores                                                                
    - Implement RSS (Receive Side Scaling)                                                   
    - Measure performance improvements

14. Deploy jumbo frames in a test environment:                                               
    
    - Configure end-to-end jumbo frame support                                               
    - Measure performance benefits for bulk transfers                                        
    - Identify compatibility issues                                                          
    - Document when jumbo frames help vs hurt

15. Implement and test NIC bonding:                                                          
    
    - Configure different bonding modes (round-robin, active-backup, 802.3ad)                
    - Measure failover times                                                                 
    - Test throughput improvements                                                           
    - Simulate cable failure scenarios 

16. Latency Budget Analysis For a distributed application:                                                           
    
    - Break down total latency into components                                               
    - Measure serialization delay for different packet sizes                                 
    - Calculate propagation delay for known distances                                        
    - Identify queuing delays in the stack

### Advanced

1. You are choosing between two cloud instance types:
    General-purpose
    Network-optimized
    List physical-layer and NIC-related factors that should influence your decision.

2. Noisy Neighbor Problem
    Your service performance degrades only during peak hours.
    Propose three physical or hardware-level causes unrelated to code.

3. Fiber vs Copper in Data Centers

4. You are designing a latency-sensitive system.
    Decide where to use:
   
   * Fiber
   
   * Copper
     
     Justify based on physical-layer characteristics.

5. Hidden Retransmissions
    A system shows:
   
   * Normal CPU
   
   * Normal memory
   
   * Slow request times
     
     Design an investigation plan to detect hidden physical-layer retransmissions.

6. Environment Parity
    Your app behaves differently in:
   
   * Local Docker
   
   * CI pipeline
   
   * Production
     
     Identify which physical-layer differences might exist between these environments.

7. Physical Constraints in System Design
   
    Design a backend system assuming:
   
   * Unstable wireless clients
   
   * High packet loss
   
   * Variable bandwidth
     
     List architectural decisions influenced directly by physical-layer realities.

8. Observability Without Physical Access
    You cannot access switches or cables.
    Explain how you would infer physical-layer problems using only:
    Metrics Logs Traces

9. MTU in Multi-Hop Networks
    You operate:
   
   * Containers
   
   * VPN
   
   * Cloud load balancer
     
     List all places where MTU can break and how that impacts data transmission.

10. Incident Postmortem
    Write a postmortem outline for an outage caused by
    
    * Faulty cable
    * Incorrect NIC auto-negotiation
      Focus on detection, impact, and prevention.

11. Teaching Moment (Senior Signal)
    You need to explain to a junior dev:
    “This is not a backend bug, it’s a physical-layer issue.”
    Draft a clear, non-condescending explanation using a real example.

12. Given intermittent packet loss:                                                          
    
    - Determine if loss is at physical, data link, or higher layers                          
    - Distinguish between cable issues, NIC issues, and buffer overruns                      
    - Use statistical analysis to identify patterns                                          
    - Create reproducible test cases

13. High-Frequency Network Optimization Optimize network for ultra-low latency:                                                  
    
    - Bypass kernel network stack where possible                                             
    - Implement kernel bypass techniques (DPDK or similar)                                   
    - Optimize CPU affinity and IRQ handling                                                 
    - Measure improvements in P50, P99, P99.9 latency

14. Custom Physical Layer Monitoring Build a comprehensive monitoring system:
    
    - Collect metrics from multiple layers simultaneously                                    
    - Correlate physical layer events with application performance                           
    - Implement anomaly detection for physical layer issues                                  
    - Create automated diagnostics and remediation

15. Network Interface Benchmarking Suite Create a benchmarking tool that:
    
    - Tests all aspects of physical interface performance                                    
    - Identifies NIC capabilities and limitations                                            
    - Compares different NIC models objectively                                              
    - Generates detailed performance reports

### Chalanges

1. Zero-Copy Networking                                                    
    Implement zero-copy data transfer:                                                       
   
   - Minimize CPU involvement in data movement                                              
   - Use DMA effectively                                                                    
   - Optimize for maximum throughput                                                        
   - Measure CPU savings

2. Rate Limiting Implementation Implement precise rate limiting:                                                         
   
   - Account for physical layer overhead (preamble, IFG, headers)                           
   - Measure actual wire rate vs application rate                                           
   - Handle burst traffic                                                                   
   - Implement token bucket at physical layer awareness

## Physical layer theory

Defines characteristics of the hardware like: cabel types, conector trypes, transmission modes

Moves signals between devices in the network

Clock synchronization Sender and reciver are operating at the same rate preventing data loss and corruption

Syn

* sender and reciver share common clock signal on the start 
* continous data stramat a constant rate (no idle periods)
* efiecnt for buld data transfer

Asy

* start and stop bits 
* legacy systems, simple embedded devices

Converts binary signals (0s and 1s) into physical signals (electric/radio) and vice versa

* **NRZ** direct voltage mapping hight 1 low 0 (long sequences of same it no transitions)

* **NRZ-I interverted** 

* **Manchester encoding**  
  
  * 0 - high to low transition 1 - low to high transition
  * self-clocking
  * 2x bandwidth (two transitions per bit)         

* **Differential Manchester**

* **4B/5B 4 data bits to 5 bit patterns**
  
  * Steps
    
        1. 4 data bits into 5 code bits using lookup table     
        2. 5 code bits into Physical signal 
  
  * cost: medum 
  
  * dominate 100Mbps networks in the 1990s - 200s

* **8B/10B**
  
  * 8 data bits to 10 code bits
  * 12 control codes for protocol control

* **64B/66B**: seppeds > 10Gbps

### Devices

**Hub** 

* transmit data one at time 
* all devices on the network recives it (performance issue)
* one way communication

**Switch** each port on the switch has its own collision domain support for biderectional communicaiton

### Data carrying

01-01-02-Data-carrying-capacity-physical layer.md

### Linus Network interface naming

01-01-00-Network-interface-naming.md

## Task solution

## Checklist

  You've mastered Physical Layer concepts when you can:

1. **Diagnose** any physical layer issue in production within minutes
2. **Design** network architectures that account for physical constraints
3. **Optimize** applications based on physical layer characteristics
4. **Predict** performance bottlenecks before deployment
5. **Build** tools that abstract physical layer complexity for other developers
6. **Teach** others how physical layer impacts their daily work
7. **Make** informed infrastructure decisions with cost/performance trade-offs
8. **Debug** cross-layer issues by understanding the full stack
9. **Architect** resilient systems that gracefully handle physical layer failures
10. **Innovate** new approaches to networking problems using physical layer knowledge

## Article
