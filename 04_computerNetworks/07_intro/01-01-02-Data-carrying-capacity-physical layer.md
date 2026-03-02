# Data carrying capacity physical layer

## TODO Data carrying capacity in the physical layer

- bandwidth - capacity of the line (bp/s - bits per second kb/s - kilobit per second mb/s megabit per second)
- throughput - actual transferred data over time (usually less than bandwidth)
- goodput - actual useable data transferred over time

#### Agenda

- **Bandwidth** is capacity of the line
* **Throughput** actual transferred data over time

* **Goodput** actual usable data transfered over time

## Data units (smallest to largest)

* when used to describe **Memory size** are calculated as some exponent of 2

* **BIt (b) : 0 or 1**

* Nibble: 4 bits

* **Byte (B): 8 bits**

* **Kilobyte (KB) 1024 Bytes**

* Megabyte (MB) 1024 KB

* Gigabyte (GB) 1024 MB

* Terabyte TB 1024 DB

## Protocol data units

* Layer 4 - Transport: Segment (TCP) or Datagram (UDP)

* Layer 3  - Network: Packet (Datagram)

* Layer 2 - Data Link: Frame

* Layer 1 - Physical: Bit 

* when used to describe Data Transfer rate bits/bytes are calculate as in metric system (kilo (1000) , mega (milion) , giga (bilion) , tera (trilion) )
  
  * **bit (b): 0 or 1**
  
  * **kilobyte (kb)**: 1000 bit (b)
  
  * Megabit (Mb): milion bit (b)
  
  * Gigabit (Gb): bilion bit (b)

## Conversion

* B vs b is very important **B** is 8 bits

* also protocol data units are metric system and data units are exponent of 2

* **1 KB** = 1000 Bytes
  = 1000 x 8 bits
  = 8000 bits
  = **8 Kb**

## Bandwidth

* data carrying capacity  (maximum rate at which data can be transfered over connection in a give time)  determined by factors like cable type Wifi standart

* sata travels through internet cables like water in a pipe. Bandwidth is the width of that pipe

* speed is the number of megabits per second that can be downloaded by a given device using your home network -> throughput 

* guaranteed by providers

Types

* symmetric: uploding and downloadin are same 

* asymmetric downloading is higer

Populat testing options: Speedtest.net Fast.com

Values needed TODO

* streaming 3 - 5 Mbps

* music 500 Kbps

## Throughput

* measured bits transferred per second (usually 60 - 95 % bandwidth) can vary due the network congestion, errors and other factors 

* real performacne of the network

* influenced by latency 

## Latency

* how late data is coming to the user (speed)
  
  * affected by distance and processing
  
  * specified for critical appps

* measurment miliseconds (ms) , microsends (qs)
  
  * LAN: 0.5 - 2 ms
  
  * WAN 20 - 100 ms

* if bandwidth is limited latency may be increated TODO investigate
  
  * latency does not impact bandwidth
  
  * but bandwidth does affect latency 

* TODO https://www.zenarmor.com/docs/network-basics/what-is-network-latency
  
  

stao number in general 01 OSI model




