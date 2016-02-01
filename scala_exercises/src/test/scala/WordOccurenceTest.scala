import org.specs2.Specification

import WordOccurrence.occurrences

import scala.collection.immutable.TreeMap
import scala.collection.mutable.ListBuffer

/**
  * Created by herve on 1/29/16.
  */
class WordOccurenceTest  extends Specification {
  def is = s2"""
   This is a specification to check the 'blob edges'
   Empty string should return empty Map                                                    $e1
   Invalid sentences won't work                                                            $e2
   Simple sentences                                                                        $e3
   Subject example                                                                         $e4
   """

  def e1 = occurrences("") must_== Map()
  def e2 = occurrences("a") must_== Map()
  def e3 = occurrences("first. Second.") must_== TreeMap("first" -> (1,ListBuffer(1)), "second" -> (1,ListBuffer(2)))
  def e4 = occurrences(
    "Given an arbitrary text document written in English, write a program that will generate a \n\nconcordance," +
    " i.e. an alphabetical list of all word occurrences, labeled with word frequencies. \n\nBonus: label each word " +
    "with the sentence numbers in which each occurrence appeared."
  ) must_== TreeMap(
    "a" -> (2,ListBuffer(1, 1)),
    "all" -> (1,ListBuffer(1)),
    "alphabetical" -> (1,ListBuffer(1)),
    "an" -> (2,ListBuffer(1, 1)),
    "appeared" -> (1,ListBuffer(2)),
    "arbitrary" -> (1,ListBuffer(1)),
    "bonus" -> (1,ListBuffer(2)),
    "concordance" -> (1,ListBuffer(1)),
    "document" -> (1,ListBuffer(1)),
    "each" -> (2,ListBuffer(2, 2)),
    "english" -> (1,ListBuffer(1)),
    "frequencies" -> (1,ListBuffer(1)),
    "generate" -> (1,ListBuffer(1)),
    "given" -> (1,ListBuffer(1)),
    "i.e." -> (1,ListBuffer(1)),
    "in" -> (2,ListBuffer(1, 2)),
    "label" -> (1,ListBuffer(2)),
    "labeled" -> (1,ListBuffer(1)),
    "list" -> (1,ListBuffer(1)),
    "numbers" -> (1,ListBuffer(2)),
    "occurrence" -> (1,ListBuffer(2)),
    "occurrences" -> (1,ListBuffer(1)),
    "of" -> (1,ListBuffer(1)),
    "program" -> (1,ListBuffer(1)),
    "sentence" -> (1,ListBuffer(2)),
    "text" -> (1,ListBuffer(1)),
    "that" -> (1,ListBuffer(1)),
    "the" -> (1,ListBuffer(2)),
    "which"-> (1,ListBuffer(2)),
    "will" -> (1,ListBuffer(1)),
    "with" -> (2,ListBuffer(1, 2)),
    "word" -> (3,ListBuffer(1, 1, 2)),
    "write" -> (1,ListBuffer(1)),
    "written" -> (1,ListBuffer(1))
  )
}
