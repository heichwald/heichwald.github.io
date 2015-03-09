---
layout: post
title: 'Javascript inner functions and closures performance in Node.js'
date: '2015-03-04'
author: Herve
tags: 'JAVASCRIPT, NODE, SCALA, V*'
---

##Tested on
Node v0.12.0  
AMD-FX 8130  
10GB RAM  
 
Each test was run 5 times and the average time was taken

{% highlight javascript %}
 
//Functions declarations
  
  //Closure  
 function foo(a, b) {
  function bar(c) {
    return a + b + c;
  }
  return bar(3);
 }

 //Inner function
 function foo1(a, b) {
  function bar(c) {
    return c + 5;
  }
  return bar(3);
 }

 //2 root functions for the same purpose
 function foo2(a, b) {
  return bar2(a, b, 3);
 }

 function bar2(a, b, c) {
  return a + b + c;
 }
 
//Same with anonymous functions/functions expressions
var foo3 = function(a,b){
  var bar = function(c) {
    return a + b + c;
  }
  return bar(3);  
}

var foo4 = function(a, b) {
  return bar4(a, b, 3);
}

var bar4 = function(a, b, c) {
  return a + b + c;
}

//Adding method to prototype
function foo5(a,b){
  this.bar = function(c) {
    return a + b + c;
  }
  return this.bar(a);	
}

console.log('Starting loop');
var start = new Date().getTime();
for(i=0;i<100000000;i++){
  foo(i,3)
  //foo1(i,3)
  //foo2(i,3)
  //foo3(i,3)
  //foo4(i,3)
  //foo5(i,3)
}
console.log('Ending loop');
var end = new Date().getTime();
console.log(end-start);
{% endhighlight %}

##Why I would prefer the version with the closure

(+) Because it avoids repeating the function parameters in every other function -a,b- gets repeated twice when not in closure

(+) Because it creates a lexical scope, code isolation and code organization, the bar function is encapsulated which makes sense if I do not plan to use this function elsewhere

(-) Unit testing is a little bit more complicated but doable. Not that in this case you do not always need to test the bar function if it is large. Sometimes it is enough to test the foo function as it is the only one which can be used elsewhere.

##Performance

| Function  |  Exec Time  |
|----------|:-------------|
| foo  | 3300 ms |
| foo1 | 2400 ms  |  
| foo2 | 400 ms  |  
| foo3 | 3300ms  |  
| foo4 | 400ms   | 
| foo5 | 19000ms   | 

**Looks like inner functions/closures in this scenario are 5X->10X slower. No difference between functions declarations vs anonymous functions. Modifying the prototype has a dramatic cost**

Functions declarations vs anonymous functions have the same time -> V8 optimizes by only compiling once the function code.

*Note this is an extreme testing, not really a real life scenario. You app will usually spend more time calling http services, databases, etc... It is not frequent to call so many functions in a web app to serve a request*

#Scala equivalent

Scala 2.11.5

{% highlight scala %}
 
object TestInner {

 def foo(a:Int, b:Int) = {
  def bar(c:Int) = a + b + c
  bar(3)
 }

 def foo1(a:Int, b:Int) = {
  def bar(c:Int) = c + 5
  bar(3)
 }

 def foo2(a:Int, b:Int) = bar2(a, b, 3)

 def bar2(a:Int, b:Int, c:Int) = a + b + c

 def main(args: Array[String]) {
  println("Starting loop")
  val start = System.currentTimeMillis
  (1 to 100000000).foreach(foo1(_,3))
  println("Ending loop")
  val end = System.currentTimeMillis
  println(end - start);
 }
}
{% endhighlight %}

##Performance

| Function  |  Exec Time  |
|----------|:-------------|
| foo  | 600 ms  |
| foo1 | 600 ms |
| foo2 | 600 ms |  

**Performance remains the same**


#Python equivalent

CPython 2.7.6

*While Scala runs on the JVM -compiled to bytecode then JIT compiler- and V8 compiles directly to assembler code using 2 compilers -a fast one and an optimzer-, Python is interpreted, so we can expect it to run much slower*

{% highlight python %}
import time

current_milli_time = lambda: int(round(time.time() * 1000))

#Closure  
def foo(a, b):
  def bar(c):
    return a + b + c
  return bar(3)

#Inner function
def foo1(a, b):
  def bar(c):
    return c + 5
  return bar(3)

#2 functions
def foo2(a, b):
  return bar2(a, b, 3)

def bar2(a, b, c):
  return a + b + c

print 'Starting loop'
start = current_milli_time()
for i in range(0, 100000000):
  foo2(i,3)

print 'Ending loop'
end = current_milli_time()
print end - start
{% endhighlight %}

##Performance

| Function  |  Exec Time  |
|----------|:-------------|
| foo  | 47000 ms  |
| foo1 | 31000 ms |
| foo2 | 32000 ms |  