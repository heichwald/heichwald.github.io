---
layout: post
title: 'Bug with mac osx running Oracle Virtual Box when installing Ubuntu or derivatives + set up guest additions'
date: '2014-12-10'
author: Herve
tags: 'OSX, Virtual Box, Ubuntu'
---

#Colors bug

When trying to install Kubuntu/Ubuntu in a new VM in Oracle Virtual box on Mac OSX Yosemite, I got a distorted colored screen when the system 
is booting from the Virtual Optic drive with the ISO for the installation.

To fix it, once you are on the colored screen, press the following combination:  
`left command + fn + f1`  and then `left command + fn + f7`

#Guest additions

You want this, so you can have among other things a full screen VM
Simply open a new terminal in your linux guest OS and enter:

{% highlight bash %}
herve@crazycat:~$ sudo apt-get install virtualbox-guest-x11
{% endhighlight %}