# Linux Network interface

* point of connection between a computer and physical network
* APPS -> OS -> Network interface driver (software) -> Network interface card (NIC) (Hardware) -> Physical network

* types 
    * Physical 
        * Ethernet
        * Fiber Optic
        * USB Ethernet Adapter
        * Wi-Fi
    * Logical/Virtual
        * Loopback (127.0.0.1) localhost
        * Virtual Ethernet  (veth0, Docker bridges)
        * Bridge 
        * VLAN Interfaces

* identification: MAC Address 

* connects Physical and DataLink layer
    * for physical it transmits/receives raw bits
    * for data link manages access to the physical medium by
        * MAC address controller
        * frame formatting
        * collision detection
* typical NIC architecture
    * MAC controller 
        * manage MAC adress
        * create/parese frames
        * buffer management
    * PHY Chip (Layer 1)
        * singnal processing
        * modulation/demodulation
        * timing/synchronizatio
        
## Linux Network interface naming        
* when Linux kernel refers to wlp0s20f3 (example) it's creating an abstraction that represents
    * physical layer hardware
    * data link layer logic (MAC addressing, frame handling)
    * driver software (bridges between hardware and OS network stack)
* Example form ARP cahge table 10.40.113.219 dev wlp0s20f3 lladdr 3e:57:11:86:2e:1d REACHABLE
    * dev wlp0s20f3 network interface 
        * dev short for device
        * wlp0s20f3 on Linux Predictable Network Interface Names endodes info about
            * wl Wirless LSN WiFi
            * p0 PCI bus number 0
                * connected via the PCI bus system (bus #0)
                * name reflect the actual hardware location
            * s20 slot number 20
                * physical/logical slot position where your WIFI card is connected
                * each PCI bus have multiple slots/devices (0-31)
            * f3 function number 3 
                * each device/slot can have multiple functions (0-7)
                * slot is building address the function is the apartment number
                * lspci -nn -> shows all PCI devices (each has bus slot and function number)
* naming patterns by connection type (first two letters)
    * en Ethernet
    * wl Wireless LAN (WiFi)
    * ww Wirless WAN (cellular/mobile broadband)
    * sl Serial line IP (runs IP over serial connections -> dial-up modems)
    * lo Loopback (virtual network interaface routes traffic back to the same machine) 
        * exist on every system
        * IPv4 127.0.0.1 (any address in 127.0.0.0/8) IPv6 ::1 known as localhost
        * no physical hardware purely software based
            * no hardware latency, just memory copies within kernel
        * securtiy practie for DB, cahces (only local processes can connect)
            * 127.0.0.0 - accessible from anywhere
        * ss -tlnp | grep 127.0.0.1 -> see all services listening on localhost
        * ping 127.0.0.1 -> test loopback connectivity
* naming patterns by location (third and fourth letter)
    * o[index] - onboard device Ex: eno1
        * build directly into the motherboard
    * s[slot] - Hotplug slot Ex: ens1, wlp3so
        * PCIs slots that support hot-swapping (in virtual machines)
    * x[MAC] - MAC address Ex: enx78e7d1ea46da
        * names after its MAC address fallback when system cannot determine PCI bus location (USB network adapters)
    * p[bus]s[slot] - PCI location Ex enp0s24, wlp0s20F3
* Windows uses friendly names (Etherent/Wi-Fi...)
* Examples 

  Desktop PC:

  `$ ip link show`
  lo: loopback
  eno1: onboard ethernet (motherboard)
  wlp3s0: WiFi card in PCIe slot (bus 3, slot 0)

  Laptop:

  `$ ip link show`
  lo: loopback
  wlp0s20f3: integrated WiFi (bus 0, slot 20, function 3)
  enx00e04c123456: USB ethernet dongle (MAC-based)

  Server with multiple NICs:

  `$ ip link show`
  lo: loopback
  eno1: first onboard port
  eno2: second onboard port
  enp5s0f0: quad-port NIC, port 1 (bus 5, slot 0, function 0)
  enp5s0f1: quad-port NIC, port 2 (bus 5, slot 0, function 1)
  enp5s0f2: quad-port NIC, port 3 (bus 5, slot 0, function 2)
  enp5s0f3: quad-port NIC, port 4 (bus 5, slot 0, function 3)

  Virtual Machine:

  `$ ip link show`
  lo: loopback
  ens33: virtual NIC (hotplug slot 33)
  
  Docker TODO

* `ip link show` - list all your network interfaces with current state and MAC adresses
    * TODO run I need explanation of results
