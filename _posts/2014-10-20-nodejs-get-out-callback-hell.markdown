---
layout: post
title: 'How to avoid the infamous callback hell in Node.js'
date: '2014-10-20'
author: Hervé
tags:
---

#Where is the trouble coming from?

Node has the nice feature of being an asynchronous platform. As a consequence, you execute functions and deal with the 
response in a callback. Indeed Node is running a single event loop and is of course not waiting for your asynchronous 
call to return before jumping to some other work to do. That's very handy and cool but comes with a major drawback; if 
you do not pay attention to your code, you will be doomed with blocks like:
{% highlight javascript %}
foo1(function () {
    foo2(function () {
        foo3(function () {
            foo4(function () {
                ...
            })
        })
    })
})
{% endhighlight %}
The deeply nested code makes it not only difficult to read but a challenge to maintain as well.

##Introcing the classic node.js callback

Because your calls are async, dealing with the response will be done later, when the response is actually received.
Therefore you will pass to node a function -callback- to be executed in the future.

###Error handling

Errors cannot be managed by a traditional try/catch block because your try/catch is executed synchronously and won't wait
for the response of your async call. It will just consider everything went fine.
Therefore, there is a convention: your callback has to be written like a function with two arguments: `function(error, response){}` 
The first argument will contain an error if any or null and the second one will contain the response in case of success.

Yet synchronous code will still be handled by try/catch, so you will have to alternate between the two, not ideal.  
Let's look at the following example where we read asynchronously the content of a file.

{% highlight javascript %}
fs.readFile('/myfile', function (err, data) {
    if (err) throw err;
    else console.log(data);
});
{% endhighlight %}

We have to check for err and throw the error. Things get messier when more nested callbacks come into play.
Error management tends to be complicated and repeated.
{% highlight javascript %}
/*
Example 1: chaining async calls sequentially
*/
fs.readFile('/myfile', function (err, data) {
    if (err) throw err;
    else 
        fs.readFile('/myfile2', function (err, data) {
            if (err) throw err;
            else console.log(data);
        });
});
{% endhighlight %}

###Calling the callback gets repeated and forgotten...

Let's imagine a second that we want to do something with the data read in the example above but we do not what when
writing this function. It will be determined by the caller.  
We rewrite the snippet:
{% highlight javascript %}
//callback must be in the form of a function with err,response as args
//Small example where we already call the callback in three places!
var foo = function(callback){
    fs.readFile('/myfile', function (err, data1) {
        if (err) callback(err);
        else 
            fs.readFile('/myfile2', function (err, data2) {
                if (err) callback(err);
                else callback(data1 + data2);
            });
    });
};
//Calling foo
foo(function(err, data){
    //Actually throwing the error is done here. 
    if(err) throw err;
    else {
        console.log(data);
        //You may still have try/catch
        try{
            someFakeSynchronousParsing.parse(data);
        }catch(err){
            throw err;
        }
    }
});
{% endhighlight %}
The issue here is that you have to call the callback in multiple places and it gets worse when adding more control flows 
like if/else. *It is very easy to forget to call the callback* somewhere and the consequence is code which will never run.
It will lead in most scenario in a request which will hang forever and time out eventually on the client.

###Parallization isn't beautiful

Our approach here is to keep a counter which holds the total number of initial operations. It will be decreased each time 
an operation is completed and tested against 0. 0 would mean everything is completed and we can return.

{% highlight javascript %}
var myParallel = function(callback){
    //We assume foo1 and foo2 are async functions 
    var counter = 2
    //Call foo1
    foo1(function(err, data){
        //When here, foo1 has completed
        if(--counter == 0) callback(null, 'foo1 finished last');
    });
    //Call foo2
    foo2(function(err, data){
        //When here, foo1 has completed
        if(--counter == 0) callback(null, 'foo2 finished last');
    });
};

myParallel(function(err, msg){
    console.log(msg);
});
{% endhighlight %}
Having to explicitly deal with the counter ourselves is unsatisfactory. Imagine now that you mix that with chains of 
deeply nested async calls and get the hellish part.

> Note that because Node runs on a single loop, we do not have to care about concurrent threads accessing counter
at the same time and having to lock it.

#Do not go into the mess

##Use named functions

By using named function instead of anonymous functions in nested callbacks, you will not only improve readibility but also
help when unit testing your functions.
Let's look back at *Example 1* and improve on it.
{% highlight javascript %}
/*
Example 1 bis: chaining async calls sequentially with named functions
*/
var read2 = function(){
    fs.readFile('/myfile2', function (err, data) {
        if (err) throw err;
        else console.log(data);
    });
}

var read1 = function(){
    fs.readFile('/myfile', function (err, data) {
        if (err) throw err;
        else read2();
    });
}
read1();
{% endhighlight %}

##Use async library

For next-level code, i strongly advise you to use the following [async](https://github.com/caolan/async) library. Your code
will read a lot better and your blocks will be shallower.
{% highlight javascript %}
/*
Rewrite example 1 with this library
*/
var read2 = function(){
    fs.readFile('/myfile2', function (err, data) {
        if (err) throw err;
        else console.log(data);
    });
}

var read1 = function(){
    fs.readFile('/myfile', function (err, data) {
        if (err) throw err;
    });
}

async.series(
    [ read1, read2 ],
    function(err, results) {
        //You would still have to handle potential err here
    }
);
{% endhighlight %}

This library is very powerful and you could as easily just run the two functions in // with `async.parallel`
The documentation is complete and well done so I simply invite you to read it on Github.

> It won't solve the error management hassle as you still deal with callbacks
 
 
##Use promises

While callbacks were once described as a Node feature, I always felt this was more a statement to hide the simple truth that Node
was missing Futures or Promises, whatever you call them. Simply said, a promise is a container holding the result of an 
asynchronous computation.  
*They are better because they allow you to write code almost the same way you would write synchronous code,* ***including errors
with catch!***

###Bluebird as a fast promise library

The most used and fastest library for Node.js is probably [bluebird](https://github.com/petkaantonov/bluebird)

####Transform callbacks into promises with promisification

Bluebird comes with an api to automatically transform a callback function which follow the node convention (err, response)
into a promise.
{% highlight javascript %}
var Promise = require("bluebird");
//Promisfy all functions from fs
Promise.promisifyAll(fs);
//Promisified functions are regular functions 
//appended with Async readFile -> readFileAsync

fs.readFileAsync("file.js", "utf8")
.then(data){
    console.log(data);
    return data.substring(1, 4);
}
.then(data){
    //You can chain easily functions
    //data is previous substring be substring and will go in catch in any error
    console.log(data);
}
//If you don't catch, it will simply be thrown, no need to manually throw it like before
.catch(err){
    console.error(err);
};
{% endhighlight %}

Be careful not to mix callbacks within promises as catch would not work as expected.
Don't do this:
{% highlight javascript %}
.then(data){
    //Async function
    fs.readFile("file.js", "utf8", function(error, resp){
        //Throw_error
        if(error) throw error;
    });
}
.catch(err){
    //You won't enter this catch from Throw_error
    console.error(err);
};
{% endhighlight %}

> There are a few node.js promises library, bluebird is currently the fastest. Check out this [benchmark](http://jsperf.com/pimp-vs-bluebird-vs-q-vs-rsvp) as a proof.]