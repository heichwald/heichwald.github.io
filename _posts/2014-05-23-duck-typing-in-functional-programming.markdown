---
layout: post
title: Duck typing in functional programming languages and how to make you code more
  DRY using Scala
date: '2014-05-23T10:11:00.000-07:00'
author: Hervé
tags: 
modified_time: '2014-05-23T10:11:44.263-07:00'
blogger_id: tag:blogger.com,1999:blog-5235607229457101085.post-6615108310934260673
blogger_orig_url: http://hervedev.blogspot.com/2014/05/duck-typing-in-functional-programming.html
---
I know people started to hate compiled languages because of the restrictions a lot of them bring with
them. Some developers felt like it gives me more pain than benefits. I am slower and less efficient.

Strongly typed languages have their benefits but devs -especially devs coming from the front-end world- always favored dynamic languages because of the freedom and weak typing they offer.  
But it does not have to be always like that.

**One of this wanted functionality is called duck typing.**

This means that if two entities look alike, behave or act the same, I can treat them indifferently.
If it looks like a duck, swims like a duck, and quacks like a duck, then it probably is a duck.

*Strong typed languages* like Java do not have a straightforward way to achieve this feature -you may use reflection though but it adds lines of code- because you have to define a relationship between the structures themselves; you use classes and you would define a common interface. This has to be done at compilation time.
The issue with this approach is that sometimes you get code that you do not own or did not write. One class may have a method quark() and one of your own class also has a method quark(). Then it is difficult to deal with such classes.  
*Weak typed languages* -often found in dynamic languages- do not enforce any classes relationships like inheritance, instead they focus on the behavior more than on the structure. It is checked at runtime.

Functional languages bring benefits of both world together. One feature which was missing in a language as Java is the possibility to treat functions a first class citizens meaning being able to pass them to other functions -the reference , not the result of the function/method computation-
This aspect has for long being present in dynamic/scripting languages through functions and closures, yet it is not limited to them; Scala is a compiled strong typed language based on the JVM. It is mainly functional.

##Java example `Test.java`
{% highlight java %}
interface Quacker{
 void quack();
}

class Duck implements Quacker{

 public void quack(){
  System.out.println("quack!!!");
  }
 }

class CopyCat implements Quacker{

 public void quack(){
  System.out.println("I pretend I know how to quack!!!");
 }
}

public class Test{
 public static void main(String[] args){
  Test t = new Test();
  t.emitSound(new Duck());
  t.emitSound(new CopyCat());
 }

 public void emitSound(Quacker q){
  q.quack();
 }
}
{% endhighlight %}

##Javascript example:
{% highlight javascript %}
function Duck () {
 this.quack = function() {
  alert("quack!!!");
 };
}

function CopyCat () {
 this.quack = function() {
  alert("I pretend I know how to quack!!!");
 };
}

function Human () {
 this.speak = function() {
  alert("I just speak!!!");
 };
}

function emitSound (quacker) {
 quacker.quack();
}

emitSound(new Duck());
emitSound(new CopyCat());

//Risk, runtime error because function is not defined!
emitSound(new Human());
{% endhighlight %}

##Scala example:
{% highlight scala %}
First way with structural type:
class Duck {
 def quack = println("quack!!!")
}


class CopyCat {
 def quack = println("I pretend I know how to quack!!!")
}


object DuckTyping extends App {
 def emitSound(duck: { def quack }) = duck.quack

 emitSound(new Duck())
 emitSound(new CopyCat())

}

Second way with functions:
class Duck {
  def quack { println("quack!!!") }
}

class CopyCat {
  def quack { println("I pretend I know how to quack!!!") }
}

object DuckTyping extends App {
  def  emitSound(quack: => Unit) {
    println("I enter emit sound")
    quack
    println("I exit emit sound")
  }
  
  emitSound(new Duck().quack)
  emitSound(new CopyCat().quack)

}
{% endhighlight %}

#DRY code: error handling

I lot of times when seeing people writing code in Java, I saw code duplication like this:

##Java:
{% highlight java %}
public class MyErrors{

  public static void failure1(){
  try{
   System.out.println(10/0);
  }
  catch(java.lang.Exception e){
   e.printStackTrace();
  }
 }

  public static void failure2(){
  try{
   int[] ints = new int[]{0,1};
   System.out.println(ints[2]);
  }
  catch(java.lang.Exception e){
   e.printStackTrace();
  }
 }

  public static void main(String[] args){
  MyErrors.failure1();
  MyErrors.failure2();
 }
}
{% endhighlight %}

By using a functional language it is very to easy to improve this code:
Here the error handling is written only once.

##Scala:
{% highlight scala %}
object MyErrors extends App {

 def result(f: => Unit){
  try{
   f
  }
  catch{
   case e: Exception => e.printStackTrace()
  }
 }

 result(System.out.println(10/0))
 result(
  System.out.println(
   List(1,2)(2)
  )
 )
}
{% endhighlight %}