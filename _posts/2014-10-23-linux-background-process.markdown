---
layout: post
title: 'How to put a process in the background and keep it running'
date: '2014-10-23'
author: Herve
tags: 'linux, process, nohup, background'
---


#Starting a process in the background

You will often want to run programs or tasks in the background, meaning it won't keep your terminal unusable for other commands.
Simply append **`&`** to the command and it will detach it from the terminal.

{% highlight bash %}
#Notice the & at the end of the command
$ python foo.py &
[1] 52447
#52447 is the pid
[1]+  Done                    python foo.py
{% endhighlight %}

That's it. Cannot be more simple than that.

> Note that you can still [redirect outputs to files](/linux-files-redirection) and it is strongly advised.
If you do not redirect, you will lose standard output and errors will still be displayed to the console.

{% highlight bash %}
$ python foo.py > app.log 2>&1 &
{% endhighlight %}

#Keep a process running while closing terminal, logging out or disconnecting from ssh

Disconnecting will usually kill your process even when running in the background, this is 
because it receives a **HUP** signal and terminates.  
To avoid this behaviour -usually you want to avoid it when sending a command through ssh-, you need to use **nohup**.

{% highlight bash %}
$ nohup python foo.py

#You can also combine with &
$ nohup python foo.py &
{% endhighlight %}