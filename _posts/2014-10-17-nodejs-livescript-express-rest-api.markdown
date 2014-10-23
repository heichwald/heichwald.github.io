---
layout: post
title: 'My feeling on livescript and how I used it to build a restful api'
date: '2014-10-17'
author: Hervé
tags: 'livescript, javascript'
---
***My first experience with Node.js led to some joy but some frustration too***

When I started using Node to build our new registration and profiles application at NY Mag, I was somehow following the
global trend to move to lighter servers with high development velocity.

I really liked to low memory or cpu footprint coming with Node. It was pretty awesome coming from a traditional Java world
with more heavyweight applications. being my first application in Node, I bootstrapped my application using an existing 
login/registration application found on the internet based on express. I was really happy with the code/restart/test done
in a few seconds.

Yet the freedom given by express also comes with a price. You **have to** put in place **conventions** and have **responsible developers**.
And that was one of my first issues: not all developers were comfortable and with a good understanding of how to work with
asynchronous code leading to confusion and how to write callbacks as well as a lot of copy/paste. You may argue that this is
more a developers/people issue than a platform/language problem and I would agree with you.  
You have to be careful with were your code is going and with more and more people contributing, it might be hard to enforce 
and you can get quickly in the not so wonderful world of multiple nested callbacks, often referred as **callbacks hell**.
This is a hard world because it so easy to mess up everything or just forget to call the callback somewhere.  
Errors management is not easy as well, regular try-catch does not work in a async callback world, so you have to mix between
try-catch style in synchronous code and (error, response) as args of the callback.

>I would higly recommend to at least use async lib that you can find here: [caolan-async](https://github.com/caolan/async). Take a look at the flow controls.

##Javascript is a simple but poor language

Javascript is a functional language, so all you deal with are functions, Javascript objects and arrays. It is so popular
because thanks to google V8 and drastic performance improvements, it can now be used with great success on the client and 
the server and above all it is easy to use. It is hard to argue that it lacks a lot of features of modern languages. It 
is verbose and miss simple features like string interpolation. Despite being functional, it does not have immutable types
or function currying. No named or default parameters in function as well. You can forget about your classic vision of OOP 
too. Because it is so poor, you will probaly end up using external libraries to fill the gaps and I would recommend you use
[lodash](https://lodash.com/docs). You may still end up with code difficult to read; something like `_.f1(_.f2(_.f3, a), b), c)`
Well, this is not ideal.

##Livescript as a savior?

Javascript flaws and imperfections are well known and have led to the creation of alternative languages compiling to javascript.
You may have heard about *coffeescript*, probably the most popular, or *typescript* developed by Microsoft which adds type checking at compilation time.
I want to talk here about *livescript* because **i think it is really nice**.  
Livescript is very functional oriented and inherits its syntax from other well recognized functional languages such as Haskell.
You can read the full [livescript](http://livescript.net) documentation in a couple of hours which means the learning curve is low.
The page comes with an online compiler/runner, so you can easily write code to both see how it compiles and runs.

The syntax is neat, short and readable. The language adds so much to javascript limited set of features.
The online doc shows a lot of examples about the additions and the number of lines of code it will save you. I really like and highly recommend to check the following sections.

* currying
* a more tradional way of doing OOP
* loops and comprehensions
* destructuring of Javascript objects
* helpful sugar: string interpolation, safe object traversal with ?
* less parenthesis and readable chaining of functions
* [prelude-ls](http://www.preludels.com/), an awesome library filled with useful higher order functions

***Everything cannot be perfect and Livescript does not solve some issues within Javascript itself***

While Livescript has a functional goal and the syntax clearly push you to this direction, all the variables and structures
you manipulate are still mutable. Functional programming is usually flavoured with immutable data which I think provide 
more robust and maintainable code. A function takes an input and returns an output and by not modifying mutable data, tends to
to do a better job at preventing against unwanted side effects or race conditions. Javascript does have immutable data and as
a consequence Livescript does not have it either. The best it can offer you is `const` which ensures the variable is not reassigned
but cannot guarantee data itself `object, array` is not modified.

The syntax tends to me so minimalist that it can be hard to read. You can omit () and , almost everywhere. I suggest you rather use:
{% highlight haskell %}
#when you have several functions calls, use do
f1 do
    f2 1 2
    arg2
{% endhighlight %}
This will also save you from unexpected compilation results as putting everything on one line may not compile as you think it will.

>**Compilation debugging:** I would recommend IntelliJ or Webstorm with [Livescript plugin](https://github.com/racklin/livescript-idea) as you are only one click away to see
the preview compiled version of a file. ![](https://camo.githubusercontent.com/e5dad8d2fb67ff9948be5d96d2c05a64ce73eb7e/687474703a2f2f706c7567696e732e6a6574627261696e732e636f6d2f66696c65732f373236362f73637265656e73686f745f31343237332e706e67)

Some conventions like use dash instead of camelCase can also lead to unexpected behaviour -as it compiles to camelCase- 
and force you to be very careful about what your are manipulating, variables, strings. I am not a big fan.


##Building simple Hello Work app in express

As a simple exercise, let's see how we can build Hello World in livescript and Express.
{% highlight bash %}
mkdir myapp
cd myapp
npm init
npm install express --save
touch app.ls
{% endhighlight %}

Open `app.ls` and add:
{% highlight haskell %}
require! express
app = express!
app.get '/' (req, res) -> res.send 'Hello World!'

server = app.listen 3000 ->
  host = server.address!.address
  port = server.address!.port
  console.log "Example app listening at http://#host:#port"
{% endhighlight %}

Open a terminal:
{% highlight bash %}
#compile it
lsc -c app.ls
#run it
node app
#in another terminal
curl http://0.0.0.0:3000/
#outputs "Hello World"
{% endhighlight %}

##Catching errors at runtime

Let's not fool ourselves here. The compilation step is just a livescript -> javascript conversion, it does not prevent
us from writing wrong code.
{% highlight haskell %}
#foo is undefined
foo.bar
{% endhighlight %}

###Consequences

This will only fail at runtime. You *have to* ensure your code is good before running it in production. 2 points I want to bring forward.

* Write a lot of unit tests, as you do not have the compilation step to tell you that you wrote bad code
* I still recommend a scripting language for rather small or straightforward apps because of the dev velocity you get but
I would use a compiled language for a heavy server application containing more business logic.
* Refactoring is harder in a scripting language because you have no other safeguard than testing. It can be dangerous if
you have a lot of developers on the codebase and cannot risk breaking the code.

> ***Protect your node process***  
Running into an uncaught exception like in the scenario above will simply kill your application server.
Not very sturdy, yah. Therefore I highly advise you to use the [cluster module](http://nodejs.org/api/cluster.html).
It will restart the process in case it dies. You should identify why your process dies and fix the code or catch exception 
occurrence is valid.