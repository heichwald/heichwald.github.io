---
layout: post
title: Juniper network connect on Linux Ubuntu 64 Bits Ubuntu 14.04 LTS Trusty Tahr
date: '2014-05-10T12:43:00.006-07:00'
author: Hervé
tags: 
modified_time: '2014-05-10T14:06:00.985-07:00'
thumbnail: http://3.bp.blogspot.com/-JAXomb2kzyU/U25xe7cLIzI/AAAAAAAABRQ/kSgA3tLD4_s/s72-c/java_version.jpg
blogger_id: tag:blogger.com,1999:blog-5235607229457101085.post-2492342434141378669
blogger_orig_url: http://hervedev.blogspot.com/2014/05/juniper-network-connect-on-linux-ubuntu.html
---
#Juniper network connect on Linux Ubuntu 64 Bits Ubuntu 14.04 LTS Trusty Tahr
**This is a step by step guide with clear explanations and screen shots to explain the set-up of Juniper network connect on Ubuntu 64 bits -tested on 14.04-**

Ubuntu is pretty straightforward when it comes to connecting to a regular vpn server with a clear and easy menu in the right of the main Ubuntu toolbar.

Setting up Network Connect from Juniper requires software from Juniper and as a result is a completely different beast.

There is already documentation out there to install Juniper on Ubuntu but I still struggled as some steps either did not work and were not exhaustive or descriptive. I hope this tutorial will help in this regard.

The following guide starts from a fresh Ubuntu install, though it is not required obviously.

##Install java and plugins
![No java installed](http://3.bp.blogspot.com/-JAXomb2kzyU/U25xe7cLIzI/AAAAAAAABRQ/kSgA3tLD4_s/s1600/java_version.jpg)

As we use Ubuntu 64 bits, we will first install Java7 64 bits as well as plugins for your browsers (Chrome, Firefox)  
`tester@tester-VirtualBox:~$ sudo apt-get install openjdk-7-jdk icedtea-7-plugin`

![Install JDK7](http://3.bp.blogspot.com/-42Ll58AVG2w/U25yrG5nrbI/AAAAAAAABRc/3VPT1z83Cak/s1600/install_java.jpg)

Now check that you have correctly installed a java 64 bits version:  
{% highlight bash %}
tester@tester-VirtualBox:~$ java -version
java version "1.7.0_55"
OpenJDK Runtime Environment (IcedTea 2.4.7) (7u55-2.4.7-1ubuntu1)
OpenJDK 64-Bit Server VM (build 24.51-b03, mixed mode)
{% endhighlight %}

Juniper needs a Java 32 bits version to run:  
`tester@tester-VirtualBox:~$ sudo apt-get install openjdk-7-jdk:i386`  

Check all your installed versions: 
{% highlight bash %}
tester@tester-VirtualBox:~$ update-java-alternatives -l 
java-1.7.0-openjdk-amd64 1071 /usr/lib/jvm/java-1.7.0-openjdk-amd64
java-1.7.0-openjdk-i386 1071 /usr/lib/jvm/java-1.7.0-openjdk-i386
{% endhighlight %}
>amd64 stands for 64 bits and i386 for 32 bits version Your default java version is still the 64 bits (you can recheck with `java -version`) 

##Download Juniper software from vpn site

![Login to vpn website](http://2.bp.blogspot.com/-l9BctEhj5ek/U252qv2v5CI/AAAAAAAABRo/Wsf89F9fVjc/s1600/vpn_home.jpg)
![Start network connect](http://1.bp.blogspot.com/-VIO63E_TYyk/U253R_iNcGI/AAAAAAAABRw/MnzfxeU5DHU/s1600/start_network_connect.jpg)
![Accept pop-ups](http://3.bp.blogspot.com/-OFqky7-sx3k/U254DZY0U2I/AAAAAAAABR8/Ptao0s890BE/s1600/warning.jpg)  
Be sure to accept Java warning which may appear just below or above the search toolbar while the page is showing "Please wait progress bar".  

Eventually  download/accept the file shown on the last applet and you will be prompt with something like:
![applet](http://4.bp.blogspot.com/-WF8kKzmdWH4/U255QLN6PTI/AAAAAAAABSI/tvWaEPqTXVg/s1600/please_wait.jpg)  
You won't be able to enter your password as Ubuntu does not have root password. By entering once and then exits by Ctrl+D, the software is still downloaded which was our goal here.  
Check that the following folder exists: `~/.juniper_networks/network_connect/`

##Download https certificate
Click on the lock -I use firefox but Chrome as also certificates option in the settings-  
![](http://4.bp.blogspot.com/-Esc4HYJoNKg/U2567c8VWuI/AAAAAAAABSU/AAIy71mb6DQ/s1600/https_lock.jpg)

![](http://2.bp.blogspot.com/-Xdbb4Sp39go/U258OaTwfYI/AAAAAAAABSo/SUgJVB7g9tU/s1600/certificate0.jpg)

![](http://1.bp.blogspot.com/-S0QZeX6Lhqw/U258Nq8vcuI/AAAAAAAABSg/U5Uwrxib7UA/s1600/certificate1.jpg)  
**Be sure to select the certificate at the vpn url level (not at the root of the tree) and export it as a .der**

##Additional dependencies
Install 32 bits libs:  
`tester@tester-VirtualBox:~$ sudo apt-get install libc6-i386 lib32z1 lib32nss-mdns` 

Install perl dependencies   
`tester@tester-VirtualBox:~$ sudo apt-get install libgtk2-perl libwww-perl`

Download madscientist script:
{% highlight bash %}
tester@tester-VirtualBox:~$ wget -q -O /tmp/msjnc https://raw.github.com/madscientist/msjnc/master/msjnc 
tester@tester-VirtualBox:~$ chmod 755 /tmp/msjnc
tester@tester-VirtualBox:~$ sudo cp /tmp/msjnc /usr/bin
{% endhighlight %}

Executes the script:
`tester@tester-VirtualBox:~$ msjnc`
>it is not required for you to read it: http://mad-scientist.us/juniper.html

##Find realm type
Open the vpn login html page (like `../dana-na/auth/url_default/welcome.cgi`)

Right click -> view source, then search for the text "realm"  
It should look like: 
`<input type="hidden" name="realm" value="Users">`
 
##Final scripts
![](http://3.bp.blogspot.com/-ydJxC6ttxFw/U26AB-4FbvI/AAAAAAAABTA/cBKidX7oqgw/s1600/start_script.jpg)

 **Be sure to choose the i386 java (32 bits)**
 ![](http://3.bp.blogspot.com/-3AZv61-CULA/U26ACmRUUtI/AAAAAAAABTI/KPRN-n_gO6U/s1600/start_ui.jpg)
 
###Troubleshooting
IVE errors type when trying to connect: Your certificate is wrong, try to export it again (try different browsers or different types (it worked for me with DER extension

###DNS resolution note

When connecting to the vpn, you dns resolution might be altered.  
If you manage to ping the ip, but the name of the host is not resolved, you may want to check `/etc/resolv.conf`   
Additional name servers and search domain names must have been written to the file. You may need to remove nameserver 127.0.1.1 or change the name servers order.  