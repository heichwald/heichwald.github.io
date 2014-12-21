---
layout: post
title: 'Know your public ip with one command'
date: '2014-12-20'
author: Herve
tags: 'LINUX, NETWORK, IP'
---

#wget or curl, you choose

{% highlight bash %}
#If curl not installed on the system -wget is installed by default-
herve@crazycat:~$  wget -qO- http://ipecho.net/plain ; echo
184.70.200.150

#If curl installed on the system
herve@crazycat:~$ curl ident.me
184.70.200.150
{% endhighlight %}