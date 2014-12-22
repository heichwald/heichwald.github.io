---
layout: post
title: 'Kill torrents downloader when VPN crashes'
date: '2014-12-21'
author: Herve
tags: 'LINUX, VPN, TORRENT, P2P, TRANSMISSION'
---

Downloading on a p2p network without a VPN can be dangerous now that we all know how torrents are ridiculously monitored.
On Ubuntu distributions, Transmission is the default installed torrent client.

You can run the following script to be sure that whenever your vpn dies, Transmission will be killed and such avoiding exposing
your real ip anytime.

OpenVPN uses a tunnel `tun0` but if you use other protocols like PPTP or SSTP, be sure to update the script accordingly.
Connect your vpn and run `ifconfig` to show your network interfaces.


{% highlight bash %}
herve@crazycat:~$ ifconfig
eth0      Link encap:Ethernet  HWaddr 92:de:80:e7:63:22  
          inet addr:192.168.0.5  Bcast:192.168.0.255  Mask:255.255.255.0
          inet6 addr: fe80::96ee:80ff:fee6:6325/64 Scope:Link
          UP BROADCAST RUNNING MULTICAST  MTU:1500  Metric:1
          RX packets:279060140 errors:0 dropped:0 overruns:0 frame:0
          TX packets:170553501 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:1000 
          RX bytes:334082968419 (334.0 GB)  TX bytes:54266807132 (54.2 GB)

lo        Link encap:Local Loopback  
          inet addr:127.0.0.1  Mask:255.0.0.0
          inet6 addr: ::1/128 Scope:Host
          UP LOOPBACK RUNNING  MTU:65536  Metric:1
          RX packets:159117 errors:0 dropped:0 overruns:0 frame:0
          TX packets:159117 errors:0 dropped:0 overruns:0 carrier:0
          collisions:0 txqueuelen:0 
          RX bytes:10453388 (10.4 MB)  TX bytes:10453388 (10.4 MB)

tun0      Link encap:UNSPEC  HWaddr 00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00  
          inet addr:10.4.150.180  P-t-P:10.4.150.180  Mask:255.255.255.255
          UP POINTOPOINT RUNNING NOARP MULTICAST  MTU:1500  Metric:1
          RX packets:249373757 errors:0 dropped:0 overruns:0 frame:0
          TX packets:153240695 errors:0 dropped:23153 overruns:0 carrier:0
          collisions:0 txqueuelen:100 
          RX bytes:276238616641 (276.2 GB)  TX bytes:35171054912 (35.1 GB)
{% endhighlight %}

The killing script:
{% highlight bash %}
herve@crazycat:~$ vim killvpn.bash
#!/bin/bash
PID=`pidof transmission-gtk`
while :
do
        FOUND=`grep "tun0" /proc/net/dev`
        if  [ -n "$FOUND" ] ; then
                date
        else
                echo "PID $PID"
                kill -s HUP $PID
                echo "killed transmission"
                exit
        fi
        echo "running"
        sleep 0.1
done

herve@crazycat:~$ sudo bash killvpn.bash
{% endhighlight %}