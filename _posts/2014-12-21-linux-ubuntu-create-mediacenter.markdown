---
layout: post
title: 'Create a simple mediacenter in Ubuntu'
date: '2014-12-21'
author: Herve
tags: 'LINUX, UBUNTU, SAMBA, XBMC, KODI'
---

I have a small computer that I recycled acting as a mediacenter; low-end cpu and 1GB of RAM is enough, a
dedicated graphic card to decode high bitrate 1080p movies is preferred or use a decent CPU (>= Intel i3) to do it.
I installed Xubuntu instead of Ubuntu because it requires less resources (CPU and RAM). I use this computer for VPN and p2p as well.

#Share hard drives over your local network

We will copy movies or content from our local network (windows or linux computers) to this mediacenter, so we use samba to share the drives.

##Install Samba

{% highlight bash %}
herve@crazycat:~$ sudo apt-get update
herve@crazycat:~$ sudo apt-get install samba
{% endhighlight %}


{% highlight bash %}
herve@crazycat:~$ ls -l /dev/disk/by-uuid/
lrwxrwxrwx 1 root root 10 Dec 19 19:27 668b1402-aaaa-4709-96fd-b93e975ed17b -> ../../sdb5
lrwxrwxrwx 1 root root 10 Dec 19 19:27 74bd2ac2-aaaa-4366-b441-5b3983babad3 -> ../../sda1
lrwxrwxrwx 1 root root 10 Dec 19 19:27 e1482e5e-bbbb-4b73-be5f-b9d74bfcef18 -> ../../sdb1
{% endhighlight %}

sda, sdb correspond to drives, numbers are for partitions.
Now if you have different hard drives or usb drives, maybe you do not know which drive is mapped to sda or sdb.
We can use the following command to find more information about one drive. This information is usually printed on the sticker on your hard drive.

{% highlight bash %}
herve@crazycat:~$ sudo hdparm -I /dev/sda
ATA device, with non-removable media
	Model Number:       ST5000DM000-XXXXX                      
	Serial Number:      XXXXX
	Firmware Revision:  CC44    
	Transport:          Serial, ATA8-AST, SATA 1.0a, SATA II Extensions, SATA Rev 2.5, SATA Rev 2.6, SATA Rev 3.0
{% endhighlight %}


##Auto-mount your drives at startup

{% highlight bash %}
herve@crazycat:~$ sudo vim /etc/fstab
# Add a line like the following at the end of the file
# /media/seagate5tb1 is the location where you want the disk to be mounted
# ext4 is the partition format, it might be fat32 or ntfs depending on your case
UUID=74bd2ac2-aaaa-4366-b441-5b3983babad3       /media/seagate5tb1      ext4    defaults        0       0
{% endhighlight %}

##Configure Samba to share the hard drives

{% highlight bash %}
herve@crazycat:~$ sudo vim /etc/samba/smb.conf
# Add the following block at the end of the file
[seagate5tb1]
        path = /media/seagate5tb1
        force user = media
        writeable = yes
        browseable = yes
        guest ok = yes
{% endhighlight %}

**Reboot your computer and you are done**

#Install mediacenter software to organize your movies/tv shows
A very nice and popular tool is Kodi -formerly known as xbmc-  
[Kodi](http://kodi.tv/about/) manages our library of movies/tv shows/music, organize them in categories and downloads the covers.

{% highlight bash %}
herve@crazycat:~$ sudo apt-get install python-software-properties pkg-config
herve@crazycat:~$ sudo apt-get install software-properties-common
herve@crazycat:~$ sudo add-apt-repository ppa:team-xbmc/ppa
herve@crazycat:~$ sudo apt-get install kodi
#Start by clicking the new Kodi entry in the menu or simply type in a terminal
herve@crazycat:~$ kodi
{% endhighlight %}