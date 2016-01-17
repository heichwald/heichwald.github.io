---
layout: post
title: 'Reverse in place words order in a string in Scala'
date: '2016-01-17'
author: Herve
tags: 'SCALA, IN PLACE'
---

# Reverse a string in an immutable fashion

If you do not care about the extra memory and CPU used to garbage collect the intermediate created strings, opt for this one liner version.
{% highlight scala %}
def reverseWordsOrder(phrase: String) =
   // -1 is to keep whitespaces
  (phrase.reverse.split(" ", -1) map(_.reverse)).mkString(" ")
{% endhighlight %}

# Reverse a string in an mutable fashion -constant memory space-

This involves to do more work, so do it only when it is meaningful.

## A method to reverse an array of chars in place 
A string is an immutable object but it is easy to convert once from a string to an array of chars

{% highlight scala %}
/**
* Reverse array in place from start to end inclusive
*/
def reverse(arr: Array[Char], start: Int, end: Int) = {
  if (end > start) {
    (0 to (end - start)/2) foreach { i =>
    val iStart = i + start
    val iEnd = end -i
    val b = arr(iStart)
    arr(iStart) = arr(iEnd)
    arr(iEnd) = b
    }
  }
}
{% endhighlight %}

## A method to reverse the order of words in a phrase in place

{% highlight scala %}
 /*
 "I love you" becomes "you love I"
 Preserves whitespaces
 Any non whitespace is considered part of the word -naive-
 */
def reverseWordsOrderInPlace(phrase: String) = {
  val arr = phrase.toCharArray
  val l = phrase.length - 1
  // Start by reversing all the chars in the string
  reverse(arr, 0, l)
  // A word start whenever there is no word already started and when the char is not a whitespace
  var startWordIndex: Option[Int] = None
  (0 to l) foreach { i =>
    if (startWordIndex.isDefined) {
      // Ending phrase or new space after word
      if (i == l || arr(i) == ' ') {
        val endIndex = if (i == l && arr(i) != ' ') i else i - 1
        // Rereverse the word only
        reverse(arr, startWordIndex.get, endIndex)
        // We finished with this word, now let's look for the next one
        startWordIndex = None
      }
    } else if (arr(i) != ' ') {
      /*First word char*/
      startWordIndex = Some(i)
    }
  }
  arr.mkString
}
{% endhighlight %}

# Testing our code

We first test our `reverse` helper function. We let scalacheck generates a big number of input strings and verify it against scala already existing `string.reverse` method.  
Then we can proceed to test our `reverseWordsOrderInPlace` and `reverseWordsOrder` methods.

{% highlight scala %}
package reverse

import org.specs2.Specification
import org.specs2.ScalaCheck

import ReversePhrase._

class ReversePhraseTest extends Specification with ScalaCheck {
  def is = s2"""
   This is a specification to check the 'reverse phrase but words'
   The reverse function is correct                                                         $e1
   The phrase should be reversed in place but the words chars remain in order              $e10
   The phrase should be reversed but the words chars remain in order                       $e11
   The phrase should be reversed in place but the words chars remain in order with spaces  $e20
   The phrase should be reversed but the words chars remain in order with spaces           $e21
   """
   
  def e1 = prop {
    (s: String) => s.reverse == { 
      val chars = s.toCharArray
      reverse(chars, 0 , s.length - 1)
      chars.mkString
    }
  }                                                                    
  def e10 = reverseWordsOrderInPlace("I am a legend") must_== "legend a am I"
  def e11 = reverseWordsOrder("I am a legend") must_== "legend a am I"
  def e20 = reverseWordsOrderInPlace(" I am a  legend  ") must_== "  legend  a am I "
  def e21 = reverseWordsOrder(" I am a  legend  ") must_== "  legend  a am I "
}
{% endhighlight %}

Git clone the sbt project on my [Github](https://github.com/heichwald/fun-with-scala/tree/master/reverse-string)