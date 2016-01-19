---
layout: post
title: 'Is javascript a good fit as a functional language?'
date: '2016-01-18'
author: Herve
tags: 'JAVASCRIPT, FUNCTIONAL PROGRAMMING, IMMUTABLE'
---

# Why javascript fails short in the functional land

The definition on wikipedia starts as the following:

> In computer science, functional programming is a programming paradigm—a style of building the structure and elements of computer programs—that treats computation as the evaluation of mathematical functions and avoids changing-state and mutable data

- Javascript comes built-in with very few types -just compare JS Array and Object with [scala collections](http://docs.scala-lang.org/overviews/collections/performance-characteristics.html)- and only mutable data types. Probably, you will deal with code where everything is an object or an array (ES6 added a few types and we now also have Sets, Maps and their weak equivalents)  
Being limited to only mutable types is just incompatible for me with doing clean functional code as there is no efficient way to guarantee avoiding changing state and mutable data.
- While functions are first class citizens, the language is pretty poor in higher-order functions that comes natively. As a result, developers have to rely heavily on third party libraries like underscore or lodash.

How could we ensure data immutability?

## Data immutability with Object.freeze and Object.create

### Immutable with Object.freeze

To avoid changing state and mutable data, it is almost required to have a mechanism available to forbid the developer to make changes in the object.  
Let's look at `Object.freeze`. There are some caveats when using `freeze`.  
First of all, you have to use strict mode, otherwise javascript won't complain, secondly it does not freeze nested objects, so you will have to do that yourself.
{% highlight javascript %}
'use strict';

var obj1 = {
  level1: {
    level11: "foo"
  }
};

Object.freeze(obj1);
{% endhighlight %}

`obj1.level1.level11 = "other";` would work. It is still workable as you can create a helper function to freeze the objects recursively; yet there might be a cost associated with the traversal and all the freezing calls if the object is very deep but it should be acceptable. 

### Creating a copy with Object.create

Once you have something frozen, you need to be able to create a cheap copy of it. Indeed functional programming is all about passing an argument to a function and getting a result which might be just a slightly modified object from the argument. You cannot afford to have an expensive `clone` operation happen as this would be memory hungry and would take considerable CPU time for large objects; and you will probably call functions over on over.

So Javascript comes with `Object.create` which does not clone the object, instead it uses the copied object prototype.
{% highlight javascript %}
'use strict';

var obj1 = {
    foo: "bar"
};

Object.freeze(obj1);
var obj2 = Object.create(obj1);
obj2.newprop = "hi"; 
{% endhighlight %}

This works but there is something fundamental you cannot do which is property reassignement. Meaning you cannot do `obj2.foo = "bar2";` If you do not see yet why this is so limiting, just consider that Arrays are objects as well, so you cannot do:
{% highlight javascript %}
'use strict';

var arr1 = new Array(10000000);
arr1.fill(0);
Object.freeze(arr1);
var arr2 = Object.create(arr1);
arr2[0] = 1;
{% endhighlight %}

Also there is no way to just get an immutable copy of this array with one element added or removed. So you cannot work with the basic expected operations.  

Cloning the array or similarly using `Object.assign` or `Array.from` is not an option as it does not reuse the current structure and will takes time and memory. Just run `node --expose-gc`:

{% highlight javascript %}
'use strict';

const util = require('util');

function printMemory() {
  global.gc();
  console.log(util.inspect(process.memoryUsage()));
}

var arr1 = new Array(10000000);
arr1.fill(0);
printMemory();
Object.freeze(arr1);
var arr2 = Object.assign({}, arr1);
// Or do var arr2 = Array.from(arr1); similar
printMemory();
{% endhighlight %}

which takes about 10 seconds on my computer to perform the assign and uses far more memory for the second print as shown below:
{% highlight bash %}
rss: 102273024, heapTotal: 90638880, heapUsed: 83635512 
rss: 1084395520, heapTotal: 934812192, heapUsed: 928158456 
{% endhighlight %}

## Prototypal inheritance

There is another issue with using the prototype as done with `Object.create`. Imagine for a second that we don't have all the issues mentioned above with `Object.freeze` and let's consider a function `copy` which simply returns a copy of the argument.

Each time you create a copy with `Object.create`, you merely create a new link in the new object to a parent object, so that if you ask for a property, it will check in the parent -and then the parent parent etc- if the object does not have the property directly.
This can be terribly inefficient for long chains of objects. Simply consider the following example where we try to create a million copy of the same very small object.

{% highlight javascript %}
function copy(arg) {
  return Object.create(arg);
}

var srcObj = {
  foo: "bar"
}, currentObj = srcObj;

Object.freeze(srcObj);

for (var i = 0; i < 1000000; i++) {
  currentObj = copy(currentObj);
  Object.freeze(currentObj);
}
{% endhighlight %}

I stopped it after 15 minutes as it had not completed yet.

# Using immutable.js

[Immutable.js](http://facebook.github.io/immutable-js/) provides an efficient implementation of immutable data types based on [persistent data structures](https://en.wikipedia.org/wiki/Persistent_data_structure) like Hash array mapped tries and vector tries as seen in Scala or Clojure.

## Replacing objects by immutable records

{% highlight javascript %}
'use strict';

var Immutable = require('immutable');

var ABRecord = Immutable.Record({a:1, b:2});
var r1 = new ABRecord();
var r2 = new ABRecord({b:3});
// r1.b = 5; will throw error
console.log(r1);
console.log(r2);
{% endhighlight %}

We were not able to update an element in an array using `freeze`, so let's see how we can make it work with immutable types:

{% highlight javascript %}
'use strict';
const util = require('util'),
  Immutable = require('immutable');

function printMemory() {
  global.gc();
  console.log(util.inspect(process.memoryUsage()));
}

const arr1 = new Array(10000000);
arr1.fill(1);

const list1 = Immutable.List(arr1);
printMemory();
const list2 = list1.set(0, 2);
printMemory();
console.log(list2.get(0));
console.log(list1.get(0));
{% endhighlight %}

which is very fast between the two prints and outputs:

{% highlight bash %}
{ rss: 268570624, heapTotal: 249557024, heapUsed: 215922184 }
{ rss: 270667776, heapTotal: 251620896, heapUsed: 216368816 }
2
1
{% endhighlight %}

## Convert back to native objects when needed

One issue with these types is that they are not native, so if you are using a lot of third party libraries, you might have to convert back and forth between types.  
Example with lodash:

{% highlight javascript %}
'use strict';
const util = require('util'),
  _ = require('lodash'),
  Immutable = require('immutable');

const arr1 = new Array(10000000);
arr1.fill(1);

const list1 = Immutable.List(arr1);
console.log(_.head(list1));
{% endhighlight %}

prints `undefined`

You have to say:

{% highlight javascript %}
'use strict';
const util = require('util'),
  _ = require('lodash'),
  Immutable = require('immutable');

const arr1 = new Array(10000000);
arr1.fill(1);

const list1 = Immutable.List(arr1);
console.log(_.head(list1.toArray()));
{% endhighlight %}

which prints 1

**My conclusion is that we can achieve functional programming, yet because it is not native to the language it always comes with pain, hassle and tradeoff**