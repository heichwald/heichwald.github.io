---
layout: post
title: 'Beware of the closure memory leak in Javascript'
date: '2016-01-10'
author: Herve
tags: 'JAVASCRIPT, CLOSURE, MEMORY LEAK'
---

I discussed closures in javascript in a previous article and the impact hit that you can get if you use them too much; remember that in js, each function is an object, so the creation has a cost.  
This article focuses on some examples of memory leaks using closures.

# How to create a memory leak in a closure in Javascript -browser, node.js-
The major point to remember is that in a javascript closure, all inner functions share the same context.
{% highlight javascript %}
var res;

function outer() {
	var largeData = new Array(10000000);	
	var oldRes = res;

    /* Unused but leaks? */
	function inner() {
		if (oldRes) return largeData;
	}

	return function(){};
}

setInterval(function() {
	res = outer();
}, 10);
{% endhighlight %}

Let's explain what's going on. `function inner` is never called but keeps a reference to `oldRes`. The problem is all closure inner functions share the same context, so `inner` shares the same context than `function(){}` at the end which is returned. Now every 10 ms we call `outer` and reassign it to the global res variable. So as long as there will be a reference pointing to this `function(){}`, the shared context is kept and thus `largeData` is kept because it is part of the `inner` function even if there is no way for `inner` to be called.  
Each time we call `outer` we save the previous `function(){}` in `oldRes` of the new function. Therefore the previous shared context has to be kept. Because of the scripting nature of js, we don't know if `function(){}` would be called at a later point in the program. 
So in the second call of `outer`, `largeData` of the first call of `outer` cannot be garbage collected and this continues with every call of `outer` until you run out of memory. Ouch!  

Note this is only a problem because you keep a reference to `function(){}` alive; if you would actually call the function, there is no leak.
Therefore the following does not leak:
{% highlight javascript %}
setInterval(function() {
	res = outer()();
}, 10);
{% endhighlight %}

## This is only a problem because js is a scripting language.
Look at this equivalent dummy scala code. It does not leak.
{% highlight scala %}
object Closure extends App {

  var res: () => Int = null

  val outer = () => {
    val largeData = Array.fill(10000000)("a")
    val oldRes = res

    val notUsed = () => if (oldRes == true) largeData else null

    val inner2 = () => 5

    inner2
  }

  while (true) {
    res = outer()
    Thread.sleep(10)
  }
}
{% endhighlight %}

{% highlight scala %}
object Closure2 extends App {

  var res: () => Int = null

  def outer = {
    val largeData = Array.fill(10000000)("a")
    val oldRes = res

    def notUsed = if (oldRes == true) largeData else null

    def inner2() = 5

    inner2 _
  }

  while (true) {
    res = outer
    Thread.sleep(10)
  }
}
{% endhighlight %}