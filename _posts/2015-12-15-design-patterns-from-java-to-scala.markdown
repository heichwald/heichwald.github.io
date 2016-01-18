---
layout: post
title: 'Some design patterns conversions from Java to Scala'
date: '2015-12-15'
author: Herve
tags: 'JAVA, SCALA, DESIGN PATTERNS'
---

This page will be updated. So keep posted.

# Decorator

A coffee is a good example where a decorator might be needed. At a coffee machine, you can select a high number of combinations among coffee types, additions like sugar or milk. You do not want to create a static class for each of this combination. You would have so many, it would be awful. Instead use a decorator.

## Coffee Decorator in Java
{% highlight java %}
package patterns;

interface Coffee {
    String ingredients();

    Float cost();
}

class SimpleCoffee implements Coffee {
    @Override
    public String ingredients() {
        return "Coffee";
    }

    @Override
    public Float cost() {
        return 3f;
    }
}

abstract class CoffeeDecorator implements Coffee {
    protected Coffee decoratedCoffee;

    public CoffeeDecorator(Coffee decoratedCoffee) {
        this.decoratedCoffee = decoratedCoffee;
    }

    @Override
    public String ingredients() {
        return decoratedCoffee.ingredients();
    }

    @Override
    public Float cost() {
        return decoratedCoffee.cost();
    }
}


class SugarDecorator extends CoffeeDecorator {

    public SugarDecorator(Coffee decoratedCoffee) {
        super(decoratedCoffee);
    }

    @Override
    public String ingredients() {
        return decoratedCoffee.ingredients() + ", Sugar";
    }

    @Override
    public Float cost() {
        return decoratedCoffee.cost() + 0.5f;
    }
}

class MilkDecorator extends CoffeeDecorator {

    public MilkDecorator(Coffee decoratedCoffee) {
        super(decoratedCoffee);
    }

    @Override
    public String ingredients() {
        return decoratedCoffee.ingredients() + ", Milk";
    }

    @Override
    public Float cost() {
        return decoratedCoffee.cost() + 1f;
    }
}

public class Decorator {
    public static void main(String[] args) {
        Coffee coffee = new SugarDecorator(new MilkDecorator(new SimpleCoffee()));
        System.out.println("Ingredients: " + coffee.ingredients());
        System.out.println("Cost: " + coffee.cost());
    }

{% endhighlight %}

## Coffee Decorator in Scala
{% highlight scala %}
trait Coffee {
  def ingredients: String
  def cost: Float
}

class SimpleCoffee extends Coffee {
  def ingredients = "Coffee"
  def cost = 3f
}

trait Sugar extends Coffee {
  abstract override def ingredients = "Sugar" + "," + super.ingredients
  abstract override def cost = 0.5f + super.cost
}

trait Milk extends Coffee {
  abstract override def ingredients = "Milk" + "," + super.ingredients
  abstract override def cost = 1f + super.cost
}

object Main extends App {
  val coffee = new SimpleCoffee with Sugar with Milk
  println("Ingredients: " + coffee.ingredients)
  println("Cost: " + coffee.cost)
}
{% endhighlight %}