---
layout: post
title: 'Parameterized binary tree in scala with testing'
date: '2015-12-23'
author: Herve
tags: 'BINARY TREE, SCALA, SCALACHECK, SPECS2'
---

# What is a binary tree and what is it used for

A complete defintion with diagrams can be found on <https://en.wikipedia.org/wiki/Binary_tree>  
It is a good -average O(log n)- structure to search elements.  
Note that the implementation below is a regular binary tree, therefore may be unbalanced.  

# The tree defintion
{% highlight scala %}
/**
  * Defines a tree element has having a value and a
  * count to avoid duplicates in the tree 
 */
case class Element[T](val value: T, count: Int = 1) {
  override def toString(): String = s"(${value},$count)"
}
{% endhighlight %}

## Tree API
{% highlight scala %}
/**
  * A tree definition, support for add, delete,
  * search, size, height and pretty print 
 */
sealed abstract class Tree[T](implicit ordering: Ordering[T]) {
  val size: Int
  val height: Int

  def add(a: T): Tree[T]

  def search(a: T): Boolean

  def delete(a: T): Tree[T]

  protected def printLevel0(): Unit
  
  protected def printOtherLevel(subLevel: Int): Unit
  
  private[tree] def printGivenLevel(level: Int): Unit = level match {
    case 0 => printLevel0()
    case _ => printOtherLevel(level - 1)
  }

  def prettyPrint(): Unit = (0 to height + 1) foreach { h =>
    printGivenLevel(h)
    println()
  }
}
{% endhighlight %}

Note that the class is sealed as we will provide all subclasses next. We also provide an implicit ordering as we will need to compare elements values to add, delete or search in the tree

## Creating a tree from a list
{% highlight scala %}
object Tree {
  def binaryTree[T](l: List[T])(implicit ordering: Ordering[T]) = 
    l.foldLeft(Empty(): Tree[T])((b, a) => b.add(a))
}
{% endhighlight %}

## Empty class as a stopper

Navigating down the tree, you need at some point to say that you are done, so let's define an EMpty class for that.

{% highlight scala %}
/**
  * Act a stopper for the tree branches
 */
sealed case class Empty[T](implicit ordering: Ordering[T]) extends Tree[T] {
  val size = 0
  val height = -1

  def add(a: T) = Node(Element(a), Empty(), Empty())

  def search(a: T) = false

  def delete(a: T) = Empty()

  def merge(tree: Tree[T]) = tree

  protected def printLevel0() = print("E")
    
  protected def printOtherLevel(level: Int) = {
    printGivenLevel(level)
    print(" ")
    printGivenLevel(level)
  }
  
  override def equals(o: Any) = o.isInstanceOf[Empty[T]]

  override def hashCode = 31
}
{% endhighlight %}

## Node definition
{% highlight scala %}
sealed case class Node[T]
    (el: Element[T], left: Tree[T], right: Tree[T])
    (implicit ordering: Ordering[T])
  extends Tree[T] {
  import ordering._

  val size = 1 + left.size + right.size
  val height = 1 + math.max(left.height, right.height)

  /**
    * If value already exists then increment count value
    * If bigger then continue in the right subtree 
    * else in the left subtree
   */
  def add(a: T) = {
    if (a == el.value) Node(Element(el.value, el.count + 1), left, right)
    else if (a > el.value) Node(el, left, right.add(a))
    else Node(el, left.add(a), right)
  }

  /**
    * Found if same value otherwise
    * if bigger, search in right subtree
    * else search in left subtree
   */
  def search(a: T) = 
    if (a == el.value) true
    else if (a < el.value) left.search(a)
    else right.search(a)

  /**
    * Pop the maximum element from the right subtree
    * and return the max value and the new subtree   
   */
  def popMaximum: (Element[T], Tree[T]) = right match {
    case Empty() => (el, left)
    case nRight: Node[T] =>
      val (max, t) = nRight.popMaximum
      (max, Node(el, left, t))
  }    
    
  /**
    * If found and no sub trees, then just replace it by Empty
    * If only left or right subtree, then just replace it by the subtree
    * Else find maximum in the left subtree and replace the element `a` by
    * this maximum. The maximum is removed from the left subtree   
    * Note that another implementation could be to decrement first the count
    * if > 1
   */
  def delete(a: T) = {     
    if (el.value == a) {
      left match {
        case Empty()               => right
        case _ if right == Empty() => left
        case nLeft: Node[T]        =>
          // https://en.wikipedia.org/wiki/Binary_search_tree#Deletion	
          val (max, newLeft) = nLeft.popMaximum
          Node(max, newLeft, right)
      }
    } else if (a < el.value) { 
      Node(el, left.delete(a), right)
    } else {
      Node(el, left, right.delete(a))
    }
  }

  protected def printLevel0() = print(el)
    
  protected def printOtherLevel(level: Int) = {
    left.printGivenLevel(level)
    print(" ")
    right.printGivenLevel(level)
  }
  
  /**
    * Same element (value and count) and same left and right subtrees
   */
  override def equals(o: Any) = o match {
    case Node(el2, left2, right2) =>
      el == el2 && left == left2 && right == right2
    case _                        => false
  }

  override def hashCode = 31 * (el.hashCode + left.hashCode + right.hashCode)
}
{% endhighlight %}

# Testing the code with Specs2 and scalacheck

{% highlight scala %}
class BinaryTreeSpec extends Specification with ScalaCheck {
  def is = s2"""

 This is a specification to check the 'binary tree'

 The tree should work with empty list and be empty tree          $e1
 The tree should work with simple int list                       $e2
 Right subtree is larger and left is smaller                     $e3
 Size of the tree is correct                                     $e4
 Height of the tree is correct when unbalanced                   $e5
 Height of the tree is correct when balanced                     $e6
 Height of the empty tree is correct                             $e7
 Search empty tree                                               $e8
 Search complex tree, present                                    $e9
 Search complex tree, absent                                     $e10
 Delete in the empty tree is should be empty tree                $e11
 Deletion right in a simple tree                                 $e12
 Deletion in a various trees                                     $e13
 Deletion non existing item in a various trees                   $e14
                                                                 """

  def e1 = binaryTree(List[Int]()) must_== Empty[Int]
  def e2 = binaryTree(List[Int](3, 1, 2)) must_==
    Node(Element(3, 1), Node(Element(1, 1), Empty[Int](), Node(Element(2, 1),
        Empty[Int](), Empty[Int]())), Empty[Int]())
  
  def isWellformed(t: Tree[Int]): Boolean = t match {
    case Empty() => true
    case Node(el, left, right) => {
      val isLeftValid = left match {
        case Empty()         => true
        case Node(ell, _, _) => el.value > ell.value && isWellformed(left)
      }
      val isRightValid = right match {
        case Empty()         => true
        case Node(elr, _, _) => el.value < elr.value && isWellformed(right)
      }
      isLeftValid && isRightValid
    }
  }

  /*
   * Generate automatically random lists
   */
  def e3 = prop {
    (l: List[Int]) =>
      {
        isWellformed(binaryTree(l)) must beTrue
      }
  }

  def e4 = prop {
    (l: List[Int]) =>
      {
        binaryTree(l).size must_== l.distinct.size
      }
  }

  def e5 = binaryTree(List[Int](1, 2, 3)).height must_== 2
  def e6 = binaryTree(List[Int](2, 1, 3)).height must_== 1
  def e7 = binaryTree(List[Int]()).height must_== -1
  def e8 = binaryTree(List[Int]()) search 5 must beFalse
  def e9 = binaryTree(List[Int](1, 2, 3, 56, 6, 8, 15)) search 9 must beFalse
  def e10 = binaryTree(List[Int](1, 2, 3, 56, 6, 8, 15)) search 8 must beTrue
  def e11 = binaryTree(List[Int]()) delete 3 must_== Empty[Int]()
  def e12 = binaryTree(List[Int](1, 2, 3)) delete 3 must_==
    Node(Element(1, 1), Empty[Int](), Node(Element(2, 1),
      Empty[Int](), Empty[Int]()))
  def e13 = prop {
    (l: List[Int]) =>
      l match {
        case Nil => binaryTree(l) delete 3 must_== Empty[Int]()
        case _ =>
          val lunique = l.distinct
          val tree = binaryTree(l)
          val sizeBefore = tree.size
          val toDelete = (Random shuffle lunique).head
          val afterDeletionTree = tree delete toDelete
          afterDeletionTree.size == sizeBefore - 1 &&
          isWellformed(afterDeletionTree) must beTrue
      }
  }
  def e14 = prop {
    (l: List[Int]) =>
      {
        val tree = binaryTree(l)
        val sizeBefore = tree.size
        val s = Stream.continually(Random.nextInt)
        val n = (s dropWhile { l.contains(_) }).head
        tree delete n
        tree.size == sizeBefore && isWellformed(tree) must beTrue
      }
  }
}
{% endhighlight %}

## Running the tests
Git clone the sbt project on my [Github](https://github.com/heichwald/fun-with-scala/tree/master/trees)