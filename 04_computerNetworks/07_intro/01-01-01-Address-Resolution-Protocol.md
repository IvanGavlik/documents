# Address Resolution Protocol (ARP)

* ARP maps IP addresses (Layer 3) to MAC addresses (Layer 2) in local networks. When a device wants to communicate with
  another device on the same subnet, it needs the target's MAC address.
* Request uses broadcast communication (one-to-all) in order to ask all end clients within a LAN, what is the physical address of a given IP. 
* Reply is encapsulated in a unicast frame. Thus it is a one-to-one communication between the requestor and the replier.
* Operation in a network: enables a host to send packet to another nod in the local network by providing a protocol to get MAC address associated with IP address
* the host broadcast a request containing the targets node's IP address ant the node with that IP address replies with its MAC address
* Typically, a network node maintains a lookup cache (on computer and router) that associates IP and MAC addressees An ARP cache size is limited by design, and addresses tend to stay in the cache for only a few minutes. It is purged regularly to free up space. 
* works within the same subnet

## Proxy ARP (mediation)

* proxy device on a give network answers the ARP request for an IP address that is not on that network
* router sends an ARP Reply to PC 1 with its own interface MAC address. means
    that: “This IP Address is not in this network. But, I know how to go there. 
    This is my MAC Address.”
* rooter sends a broadcast ARP Request to the second network.
* can be abused - attacker enables Proxy ARP on their device and intercepts traffic meant for other hosts
* TODO investigate from security perspective

## Inverse ARP

uses MAC address to find an IP adress

## Reverse ARP

* Used by a device to discover its own IP address when it only knows its MAC address (This protocol is now considered obsolete, as it has been replaced by the Dynamic Host Configuration Protocol (DHCP))

## Gratuitous ARP

* to check if any duplicate IP exists in the network
* identfy ARP frames with is Ethertype value: 0x0806 2 bytes value after the destination MAC and source MAC part in an ethernet frame. 

## ARP announcements

* device broadcasts its own IP-to-MAC mapping without being asked 
* send when you connect to WiFi or Ethernet
* also how ARP spoofing works 

## ARP probe

* check if IP address is already in use before claiming it
* this was before IPv4 ACD Address Conflict Detection)
  * sends 3 ARP probes msg 
  * listen for conflict (If someone responds I have xyz IP -> conflit
  * if no response -> IP is free
  * do ARP announcements 
  * device use IP

## ARP Spoofing / ARP poison routing

* (fake ARP message to a target LAN with the intention of linking their MAC address with the IP address of legitimate device)
  * implement static ARP entries in high security zones
  * enable dynamic arp insepction
  * configure port security for max address control
  * use encryption and VPS to protect data-in transit
* how attacker can send fake ARP response 
* easy to execute but require local network access making them common in the public WiFi
  * Attacker needs physical/network access (not remote)
  * ARP spoofing is Layer 2 → must be on same local network
  * use tools like Ettercap, Arpspoof, Bettercap, Cain & Abel

### How to do attach

Coffee shop scenario:

* 20 people on same WiFi
* Attacker runs: ettercap -T -M arp:remote /192.168.1.1// /192.168.1.0-255//
* Intercepts all traffic from everyone
* Tools are free and easy to use
* Attack Explanation: 01-01-01-01-ARP-snoofing-atack.md

When you connect to public WiFi you are part of the local network

* you authenticate (caffe shop) or it is open 

* router gives you IP vis DHCP 

* you are nor on the same Layer 2 brodcast domain as everyone else

* Consumer/small business routes dont support DAI because it requires managed enterpries switched

* TODO write clojure program on Detection: How to Know if It's Happening ask chat gpt first - this program has to run on local/your laptop is this possible ? 

### Static ARP entries

* manually configured IP-to-MAC address mapping that are permanently stored in the ARP cache
* state PERMANENT
* critical infrastructure stability 
  * servers, routers
* drawbacks
  * manual maintenance
  * not scalable - 

### Dynamic ARP inspection

* on network switched security feature to prevent spoofing/poisoning attacks by validating ARP packets (check that ARP messages are legitimate)
* problem is that ARP has no authentication 
  * anyone can send fake ARP replies
* DAI use a trused DB to validate ARP packets it checks IP-MAC, VLANm INTERFACE
  with DHcp Snooping Database, Static ARP ACLs
* Validation process
  * check does the source MAC match the senders MAC
  * is the IP-to-MAC binding valid (checks DHCP snooping DB)
  * is ARP comming from a truste port ? 
  * are there any invalid or unexpected fields
    Setup 
* only on the switch
* enable DHCP snooping on switch
  * `ip dhcp snooping`
* configure trusted ports on switch
  * TODO
* enable DAI on VLANs on switch
  * `ip arp inspection vlan 10`
* configure rate limiting on switch
  * `interface range GigabitEthernet0/2-24`
    `ip arp inspection limit rate 15`
    If a port exceeds 15 ARP packets/second → put in error-disabled state.

Additional steps

* create static binding for servers with static IPs
* enable logging to monitor attacks
* 

### Port security

## Tasks

### View ARP Cache

`ip neigh show`

* displays the neighbor table (ARP cache) which maps IP address to MAC addresses on your local network

```
10.40.113.219 dev wlp0s20f3 lladdr 3e:57:11:86:2e:1d REACHABLE 
10.40.113.235 dev wlp0s20f3 FAILED
```

format of the response <IP address> dev <interface> lladdr <MAC address> <STATE>

10.40.113.219 dev wlp0s20f3 lladdr 3e:57:11:86:2e:1d REACHABLE

* 10.40.113.219 - IP address of a device on your network
* dev wlp0s20f3 - Network interface (your wireless adapter)
  * dev is short for device
  * predictable network interface names encodes info about the hardware
    * wl  Wirless LAN WiFI
    * p0 PCI bus number 0
    * s20 slot numer 20
    * f3 function number 3 
* lladdr 3e:57:11:86:2e:1d - Link layer address (MAC address) of that device
* REACHABLE - The entry is valid and the host is reachable
  * possible values
    * REACHABLE
    * STALE - exist hasn't been verified recently
    * DELAY - waiting to verify reachability
    * PROBE - probing
    * FAILED - resultion failed
    * PERMANENT - manually configured entry 

### Clear ARP Cache

`sudo ip neigh flush all`

### Monitor ARP Traffic

`sudo tcpdump -i eth0 arp`
`sudo tshark -i eth0 -f "arp"`

### Add static ARP entry

`sudo ip neigh add 192.168.1.100 lladdr aa:bb:cc:dd:ee:ff dev eth0`

* if/when the IP changes
  * device has now diff IP (MAC is the same)
  * when you try to reac the device at its new IP dynamic ARP learns the new IP -> MAC mapping 
  * now you have two entries (old static becomes stale garbage)
  * use static ARP for devices with static IPs
