# Commands 

`ip link show`
* show all network interfaces



`ip addr show`
* show IP addresses


`ip -s link show eth0`
* show interface statistics


`ip neigh show`
* ARP cache table 


`sudo ip neigh flush all`
* ARP cache table clear


`sudo tcpdump -i eth0 arp`
* monitor network packet analyzer
* tcpdump command line packet analyzer (captures and displays network packets in real-time)
    * shows source/destination IPs ports, protocols
    * works at layer 2/3/4
    * params
        * host X to/from IP
        * src X form IP
        * dst x to IP
        * by protocol (just say protocol name) icmp, tcp, udp, arp
        * port X
        * combine with and
    * output
        * -n dont resolve hostnames
        * -nn don't resolve hostnames or port names
        * -v more packet details
        * -vv very verbose 
        * -vvv maximum verbosity
        * -X show packet in HEX and ASCII
        * -c xy caputer xy packets
    * -w save to file (pcap format)
    * -r read from file 
    * TODO explain this sudo tcpdump -i lo -A -s 0 port 3453
    
`sudo tshark -i eth0 -f "arp"`
    * command-line version of Wireshark - code and display network protocols in detail (tcpdupm with better protocol understanding)
    * tshark is slower but have human readable protocol 
    * TODO learn about tshark and practice with it    
