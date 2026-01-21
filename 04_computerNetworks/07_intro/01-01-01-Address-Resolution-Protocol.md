# Address Resolution Protocol (ARP)

* ARP maps IP addresses (Layer 3) to MAC addresses (Layer 2) in local networks. When a device wants to communicate with
  another device on the same subnet, it needs the target's MAC address.
* Request uses broadcast communication (one-to-all) in order to ask all end clients within a LAN, what is the physical address of a given IP. 
* Reply is encapsulated in a unicast frame. Thus it is a one-to-one communication between the requestor and the replier.
* Operation in a network: enables a host to send packet to another nod in the local network by providing a protocol to get MAC address associated with IP address
* the host broadcast a request containing the targets node's IP address ant the node with that IP address replies with its MAC address
* Typically, a network node maintains a lookup cache (on computer and router) that associates IP and MAC addressees An ARP cache size is limited by design, and addresses tend to stay in the cache for only a few minutes. It is purged regularly to free up space. 
* works within the same subnet
* Proxy ARP
    * proxy device on a give network answers the ARP request for an IP address that is 
    not on that network
    * router sends an ARP Reply to PC 1 with its own interface MAC address. means
    that: “This IP Address is not in this network. But, I know how to go there. 
    This is my MAC Address.”
    * rooter sends a broadcast ARP Request to the second network.
* Inverse ARP uses MAC address to find an IP adress
* Reverse ARP Used by a device to discover its own IP address when it only knows its MAC address (This protocol is now considered obsolete, as it has been replaced by the Dynamic Host Configuration Protocol (DHCP))
* Gratuitous ARP: to check if any duplicate IP exists in the network
* identfy ARP frames with is Ethertype value: 0x0806 2 bytes value after the destination MAC and source MAC part in an ethernet frame. 

* ARP probe TODO
* ARP announcements
* ARP mediation
* ARP spoofing and proxy ARP
* offers a minimal seq risk because it lacks authentication mechanisms - ARP Spoofing / ARP poison routing (fake ARP message to a target LAN with the intention of linking their MAC address with the IP address of legitimate device)
    * implement static ARP entries in high security zones
    * enable dynamic arp insepction
    * configure port security for max address control
    * use encryption and VPS to protect data-in transit

## Tasks

### View ARP Cache
`ip neigh show`

* displays the neighbor table (ARP cache) which maps IP address to MAC addresses on your local network

```
10.40.113.219 dev wlp0s20f3 lladdr 3e:57:11:86:2e:1d REACHABLE 
10.40.113.235 dev wlp0s20f3 FAILED
```

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

