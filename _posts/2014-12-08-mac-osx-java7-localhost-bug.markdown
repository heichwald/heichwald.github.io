---
layout: post
title: 'Bug with mac osx and Java 7 with localhost entry'
date: '2014-12-08'
author: Herve
tags: 'OSX, JAVA, PLAY'
---

After a fresh install of Yosemite yesterday, I installed the JDK 1.7 from the oracle website and started an existing Play application of mine and got the 
following unpleasant error message:
`Caused by: java.net.UnknownHostException: [HOSTNAME_NAME]: nodename nor servname provided, or not known`

I was very surprised as all my systems (like the database) are local on my computer.

To fix the issue I had to enter the following entry to my hosts file in `/etc/hosts`
`127.0.0.1	HEichwald-iMac27.local`

> *HEichwald-iMac27.local* is the name of the computer which shows when you open a terminal and type the command `hostname`.
{% highlight bash %}
HEichwald-iMac27:~ heichwald$ hostname
HEichwald-iMac27.local
{% endhighlight %}