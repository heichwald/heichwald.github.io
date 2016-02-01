import scala.collection.immutable.{HashSet, TreeMap}
import scala.collection.mutable.{StringBuilder, ListBuffer}
import scala.annotation.tailrec

/**
  * Created by herve on 1/29/16.
  */
object WordOccurrence {

  val endSentencesMarkers = HashSet[Char]('?', '!')
  val endWordMarkers = HashSet[Char](' ', ',', ';', ':', '\'', '\n') ++ endSentencesMarkers
  val ignoreMarkers = HashSet[Char]('"', '(', ')', '[', ']')
  val specialMarkers = HashSet[Char]('\n', '\t', '\r')


  /**
    * Add the work occurrence to the map and return the updated map
    * Also save the sentence number
    * If the word is already there, the occurences count is incremented otherwise it is set to 1
    * @param word the word to add to the map
    * @param wordsMap the map containing the occurences of all the found words
    * @param sentenceNumber the current sentence number
    * @return the updated map
    */
  private def addOccurrence(word: String, wordsMap: TreeMap[String, (Int, ListBuffer[Int])], sentenceNumber: Int) =
    wordsMap + (word -> {
      wordsMap.get(word) match {
        case Some((occ: Int, sentences: ListBuffer[Int])) =>
          sentences.append(sentenceNumber)
          (occ + 1, sentences)
        case None => (1, ListBuffer(sentenceNumber))
      }
    })


  /**
    * Check if the next non special character is upper case (will continue until the end or finding a non special char)
    * @param text the text to search in
    * @param start the position to look at
    * @return whether the next char is uppercase
    */
  @tailrec
  private def isNextCharUpperCase(text: String, start: Int): Boolean = {
    val l = text.length
    if (start < l) {
      val c = text(start)
      c.isUpper || (c == ' ' || c == '.' || (specialMarkers contains text(start))) && isNextCharUpperCase(text, start + 1)
    } else {
      false
    }
  }

  /**
    * Return the sorted occurrences map resulting from the text parsing
    * !!! Assumption is that every text is somehow correct, so it ends with either ., ! or ?
    * !!! Each new sentence should start with an uppercase char
    * Complete sentence detection is very hard and standalone projects like https://opennlp.apache.org/
    * @param text the text
    *             Allowed special chars are defined at the top of this file
    * @return the occurrences map resulting from the text parsing
    */
  def occurrences(text: String): TreeMap[String, (Int, ListBuffer[Int])] = {
    text.foldLeft((TreeMap[String, (Int, ListBuffer[Int])](), new StringBuilder(), 1, 0))((t, c: Char) => {
      /*
      We use a tuple of 4 elements
      The first is the map occurrences we computed until we met this char
      The second is the word being build char by char
      The third is the sentence number, incremented each time we start a new sentence
      The last one is the position of char c in the text
       */
      val (wordsMap, wordBuilder, sentenceNumber, posInText) = t
      val newPosInText = posInText + 1
      val word = wordBuilder.toString

      /*
      Same as input with potentially the mutable wordBuilder updated
       */
      def defaultTuple = (wordsMap, wordBuilder, sentenceNumber, newPosInText)

      def addCharToWord() = wordBuilder += c.toLower // It is part of the current word that we build

      /*
      If the end of the word is met, add the word to the occurrences map, increment the sentence number if
      needed and restart a new word
      */
      def addWord(n: Int) = (addOccurrence(word, wordsMap, sentenceNumber), new StringBuilder(), sentenceNumber + n, newPosInText)

      def handlePeriodChar = {
        // Check if it is end of sentence or if there is an upper char in the following text chars
        val isEndOfSentence = posInText == text.length - 1 || isNextCharUpperCase(text, posInText + 1)

        if (isEndOfSentence) addWord(1)
        else {
          addCharToWord()
          defaultTuple
        }
      }

      /*
      We ignore some chars and we ignore if there is no word and we meet a ending word marker again - like a sentence ending with ... -
       */
      if ((ignoreMarkers contains c) || wordBuilder.isEmpty && ((endWordMarkers contains c) || c == '.')) defaultTuple
      else if (c == '.') handlePeriodChar
      else if (endWordMarkers contains c) addWord(if (endSentencesMarkers contains c) 1 else 0)
      else {
        addCharToWord()
        defaultTuple
      }
    })._1
  }
}
