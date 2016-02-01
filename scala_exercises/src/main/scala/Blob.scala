/**
  * Created by herve on 1/27/16.
  * Vector(
      Vector(0,0,0,0,0,0,0,0,0,0),
      Vector(0,0,1,1,1,0,0,0,0,0),
      Vector(0,0,1,1,1,1,1,0,0,0),
      Vector(0,0,1,0,0,0,1,0,0,0),
      Vector(0,0,1,1,1,1,1,0,0,0),
      Vector(0,0,0,0,1,0,1,0,0,0),
      Vector(0,0,0,0,1,0,1,0,0,0),
      Vector(0,0,0,0,1,1,1,0,0,0),
      Vector(0,0,0,0,0,0,0,0,0,0),
      Vector(0,0,0,0,0,0,0,0,0,0)
    )
    blobEdges = (1, 7, 2, 6)
    reads = 44
  *
  *
  */

import scala.annotation.tailrec
import scala.collection.immutable.Queue

/**
  * Blob edges
  * @param top top of the blob
  * @param bottom bottom of the blob
  * @param left left of the blob
  * @param right right of the blob
  */
case class BlobEdges(val top: Int, val bottom: Int, val left: Int, val right: Int)

/**
  * Perimeter in which we look for new blob edges
  * @param lowRow the low row of the perimeter
  * @param highRow the high row of the perimeter
  * @param lowColumn the low column of the perimeter
  * @param highColumn the high column of the perimeter
  */
case class SearchPerimeter(val lowRow: Int, val highRow: Int, val lowColumn: Int, val highColumn: Int)

/**
  * The left and right limit of the row: the last 1 on the left and the last 1 on the right
  * Left may == right
  * @param rowIndex row index
  * @param left the index of the 1 closest to the left
  * @param right the index of the 1 closest to the right
  */
case class RowEdges(val rowIndex: Int, val left: Int, val right: Int)

/**
  * We represent the blob like:
  * Vector(
    Vector(0,0,0),
    Vector(0,1,0),
    Vector(0,1,0),
    Vector(0,0,0)
  )
  * @param blob the blob
  */
case class Blob(blob: Vector[Vector[Int]]) {

  /**
    * If no 1 found, the index is -1
    */
  val emptyEdges = BlobEdges(-1, -1, -1, -1)

  /**
   * Number of cell reads in the matrix
   */
  private var _reads: Int = 0

  /**
    * Max edges of the blob found so far
    */
  private var _maxEdges: BlobEdges = emptyEdges

  /**
    * Public reads accessor
    * @return reads
    */
  def reads = _reads

  /**
    * Return j if i < 0, i if j < 0 else return the min
    * @param i an int
    * @param j an int
    * @return j if i < 0, i if j < 0 else return the min
    */
  private def minHelper(i: Int, j: Int) =
    if (i < 0) j
    else if (j < 0) i
    else math.min(i, j)

  /**
    * Set max edges between current max edges _maxEdges and b1
    * It may be a mix of current max and b1 as it will take closest to edge in every 4 directions
    * @param b1 blob edges
    */
  private def updateMaxEdges(b1: BlobEdges): Unit = {
    _maxEdges = BlobEdges(
      minHelper(b1.top, _maxEdges.top),
      math.max(b1.bottom, _maxEdges.bottom),
      minHelper(b1.left, _maxEdges.left),
      math.max(b1.right, _maxEdges.right)
    )
  }

  /**
    * Mid of low and high
    * @param low low index
    * @param high high index
    * @return the mid of low and high
    */
  private def mid(low: Int, high: Int) = low + (high - low) / 2

  /**
    * Check if a perimeter is valid based on its boundaries
    * @param searchPerimeter perimeter
    * @return if a perimeter is valid based on its boundaries
    */
  private def isValid(searchPerimeter: SearchPerimeter) =
    searchPerimeter.lowRow <= searchPerimeter.highRow && searchPerimeter.lowColumn <= searchPerimeter.highColumn

  /**
    * Is the point defined by row index and column index in the virtual square defined by the 4 boundaries of the blob -top, bottom, left and right- ?
    * @param blobEdges blob edges
    * @param rowIndex row index
    * @param columnIndex column index
    * @return whether the point defined by row index and column index in the virtual square defined by the 4 boundaries of the blob
    */
  private def isInBlobSquare(blobEdges: BlobEdges, rowIndex: Int, columnIndex: Int) =
    blobEdges.left <= columnIndex && columnIndex <= blobEdges.right && blobEdges.top <= rowIndex && rowIndex <= blobEdges.bottom


  /**
    * Find first index where the cell is a 1 in a row or in a column
    * Side-effect: we incr the reads when we read a cell
    * @param isRow do we iterate on a row or column
    * @param blobEdges the blob edges so we do not read unecessary cells
    * @param index index of a column or row
    * @param indices indices to iterate over
    * @return first index where the cell is a 1 or - 1
    */
  @tailrec
  private def firstOneIndex(isRow: Boolean)(blobEdges: BlobEdges, index: Int, indices: Seq[Int]): Int = indices match {
    case Seq() => -1
    case Seq(i, xs@_*) =>
      val rowIndex = if (isRow) index else i
      val columnIndex = if (isRow) i else index
      val row = blob(rowIndex)
      if (columnIndex >= 0 && columnIndex < row.size && !isInBlobSquare(blobEdges, rowIndex, columnIndex)) {
        _reads += 1
        if (row(columnIndex) == 1) i else firstOneIndex(isRow)(blobEdges, index, xs)
      } else {
        firstOneIndex(isRow)(blobEdges, index, xs)
      }

  }

  private def firstOneIndexRow = firstOneIndex(true)_
  private def firstOneIndexColumn = firstOneIndex(false)_

  /**
    * Search max blob edges by dichotomy
    * _maxEdges is modified along the way
    * @param blobEdges max new blob edges so far
    * @param q a queue of perimeters in the matrix where we have to look for potential new blob edges
    */
  @tailrec
  private def edgesByDichotomy(blobEdges: BlobEdges, q: Queue[SearchPerimeter]): Unit = {

    /**
      * Look for new edges in  an search perimeter from the queue
      */
    lazy val queueNonEmpty = {
      val (searchPerimeter, newQ) = q.dequeue

      /**
        * We can optimize our algorithm by moving a row or column outside of the virtual square defined by the max edges found so far
        * We want to stay in the perimeter
        * @param perimeterLow the low index of the perimeter
        * @param perimeterHigh the high index of the perimeter
        * @param maxEdgesLow the low index of the max edges
        * @param maxEdgesHigh the high index of the max edges
        * @param index the index we will check to be in the virtual square and move otherwise
        * @param default the default value to return if no one index in the perimeter would outside the virtual square
        * @param firstSide we will go one direction first and if nothing founf we will go the other side (ex, go left first and
        *                  then right,  or go up first and then down)
        * @return the index outside the virtual square or default
        */
      @tailrec
      def tryMoveToOutsideBlobSquare(perimeterLow: Int, perimeterHigh: Int, maxEdgesLow: Int, maxEdgesHigh: Int)
                                    (index: Int, default: Int, firstSide: Boolean = true): Int = {
        if (index >= perimeterLow && index <= perimeterHigh) {
          if (index >= maxEdgesLow && index <= maxEdgesHigh)
            tryMoveToOutsideBlobSquare(perimeterLow, perimeterHigh, maxEdgesLow, maxEdgesHigh)(index + (if (firstSide) -1 else 1), default, firstSide)
          else index
        } else if (firstSide) tryMoveToOutsideBlobSquare(perimeterLow, perimeterHigh, maxEdgesLow, maxEdgesHigh)(default, default, false)
        else default
      }

      def tryMoveColumnToOutsideBlobSquare =
        tryMoveToOutsideBlobSquare(searchPerimeter.lowColumn, searchPerimeter.highColumn, _maxEdges.left, _maxEdges.right)_

      def tryMoveRowToOutsideBlobSquare =
        tryMoveToOutsideBlobSquare(searchPerimeter.lowRow, searchPerimeter.highRow, _maxEdges.top, _maxEdges.bottom)_

      /**
        * Search for new edges by dichotomy in 2 sub zones
        * @param b new know blog edges (will be compared with max edges)
        * @param l a list of new perimeters to search in (2 elements)
        * @return new blob edges, new queue
        */
      def dichotomyIn2SubZones(b: BlobEdges)(l: List[SearchPerimeter]) = (b, newQ.enqueue(l))


      def dichotomyIn2SubZonesByColumn(topEdge: Int, bottomEdge: Int, column: Int) =
        dichotomyIn2SubZones(BlobEdges(
          topEdge,
          bottomEdge,
          column,
          column
        ))_

      def dichotomyIn2SubZonesByRow(leftEdge: Int, rightEdge: Int, row: Int) =
        dichotomyIn2SubZones(
          BlobEdges(
            row,
            row,
            leftEdge,
            rightEdge
          ))_


      if (!isValid(searchPerimeter)) {
        /**
          * If the perimeter is not valid (like low > high), we just discard it
          */
        (blobEdges, newQ)
      } else {
        val tempMidRowIndex = mid(searchPerimeter.lowRow, searchPerimeter.highRow)
        val tempMidColumnIndex = mid(searchPerimeter.lowColumn, searchPerimeter.highColumn)
        /**
          * We can optimize our research by trying to move the mid row and mid column just out of the virtual square defined by the blob
          */
        val midRowIndex = tryMoveRowToOutsideBlobSquare(tempMidRowIndex, tempMidRowIndex, true)
        val midColumnIndex = tryMoveColumnToOutsideBlobSquare(tempMidColumnIndex, tempMidColumnIndex, true)
        /**
          * We try to find the first 1 in the new search perimeter on the mid row
          */
        val leftEdge = firstOneIndexRow(blobEdges, midRowIndex, searchPerimeter.lowColumn to searchPerimeter.highColumn)

        /**
          * Find the index of a top 1 in the mid column
          * @param startIndex index included to start the research, we move down
          * @param endIndex index excluded to stop the research
          * @return the index or -1
          */
        def findTop(startIndex: Int, endIndex: Int) =  firstOneIndexColumn(blobEdges, midColumnIndex, startIndex until endIndex)

        /**
          * Find the index of a bottom 1 in the mid column, if none found default to the top found or -1
          * @param bottomRowMaxIndex the bottom index where to start to look for a 1, we move up
          * @param topEdge the top edge
          * @return the index of a bottom 1 in the mid column, if none found default to the top found or -1
          */
        def findBottom(bottomRowMaxIndex: Int, topEdge: Int = -1) = {
          val i = firstOneIndexColumn(blobEdges, midColumnIndex, bottomRowMaxIndex until topEdge by -1)
          if (i == -1) topEdge else i
        }

        /**
          * We divide our perimeter in 4 sub perimeters and look for new edges there
          * @param newBlobEdges the new know blog edges (will be compared with max edges)
          * @return new blob edges, new queue
          */
        def dichotomyIn4SubZones(newBlobEdges: BlobEdges) =
          (
            newBlobEdges,
            newQ.enqueue(List(
              SearchPerimeter(searchPerimeter.lowRow, midRowIndex - 1, searchPerimeter.lowColumn, midColumnIndex - 1),
              SearchPerimeter(searchPerimeter.lowRow, midRowIndex - 1, midColumnIndex + 1, searchPerimeter.highColumn),
              SearchPerimeter(midRowIndex + 1, searchPerimeter.highRow, searchPerimeter.lowColumn, midColumnIndex - 1),
              SearchPerimeter(midRowIndex + 1, searchPerimeter.highRow, midColumnIndex + 1, searchPerimeter.highColumn)))
          )

        /**
          * What do we do if we found a 1 in the mid row (in fact mid row adjusted)
          */
        lazy val oneInRowFound = {
          /**
            * We look for a right index until left, if none found, it will be left index
            */
          val rightEdgeUntilLeft = firstOneIndexRow(blobEdges, midRowIndex, searchPerimeter.highColumn until leftEdge by -1)
          val rightEdge = if (rightEdgeUntilLeft != -1) rightEdgeUntilLeft else leftEdge
          /**
            * We look for a 1 in the top half of the square on the mid column
            */
          val topEdgeOnTopHalf = findTop(searchPerimeter.lowRow, midRowIndex)
          /**
            * If none found, we know we have a 1 one the row, so we can set top to this value as well (top in the square found so far)
            */
          val topEdge = if (topEdgeOnTopHalf != -1) topEdgeOnTopHalf else midRowIndex
          /**
            * We look for the bottom 1 on the mid column, if none it is top
            */
          val bottomEdge = findBottom(searchPerimeter.highRow, math.max(topEdge, midRowIndex))
          /**
            * Top or bottom edge has been found on the column, so it may be more on the left or the right that the left
            * and right we found on the mid row, so we need to potentially adjust the left and right indices
            */
          val otherEdgePossible = topEdge != midRowIndex || bottomEdge != midRowIndex
          val maxLeftEdge = if (otherEdgePossible && midColumnIndex < leftEdge) midColumnIndex else leftEdge
          val maxRightEdge = if (otherEdgePossible && midColumnIndex > rightEdge) midColumnIndex else rightEdge

          /**
            * If all the ones are on one half of the perimeter, we can eliminate half of the zone for the next dichotomy
            */
          lazy val allOnesInOneSideOfPerimeter = {
            val l = if (maxLeftEdge < midColumnIndex) {
              List(
                SearchPerimeter(searchPerimeter.lowRow, midRowIndex - 1, searchPerimeter.lowColumn, midColumnIndex - 1),
                SearchPerimeter(midRowIndex + 1, searchPerimeter.highRow, searchPerimeter.lowColumn, midColumnIndex - 1)
              )
            } else {
              List(
                SearchPerimeter(searchPerimeter.lowRow, midRowIndex - 1, midColumnIndex + 1, searchPerimeter.highColumn),
                SearchPerimeter(midRowIndex + 1, searchPerimeter.highRow, midColumnIndex + 1, searchPerimeter.highColumn)
              )
            }
            dichotomyIn2SubZonesByRow(
              maxLeftEdge,
              maxRightEdge,
              midRowIndex
            )(l)
          }

          /**
            * Only ones in 1 half of the perimeter
            */
          if (topEdge == midRowIndex &&
            (maxLeftEdge < midColumnIndex && maxRightEdge < midColumnIndex || maxLeftEdge > midColumnIndex && maxRightEdge > midColumnIndex)
          ) allOnesInOneSideOfPerimeter
          else dichotomyIn4SubZones(BlobEdges(topEdge, bottomEdge, maxLeftEdge, maxRightEdge))
        }

        /**
          * What do we do when no one in row
          */
        lazy val noOneInRow = {

          /**
            * What do we do when no one in top half on the mid column
            */
          lazy val noOneInTopHalf = {
            /**
              * Look for a one in bottom half on the mid column
              * First a top and if we have a top, then a bottom
              */
            val topEdgeOnBottomHalf =
              if (midRowIndex < searchPerimeter.highRow) findTop(midRowIndex + 1, searchPerimeter.highRow + 1) else -1
            if (topEdgeOnBottomHalf != -1) {
              oneInBottomHalf(topEdgeOnBottomHalf)
            } else {
              if (_maxEdges == emptyEdges) dichotomyIn4SubZones(blobEdges) // if none blob edges found so far, we know nothing
              else noOneInColumn
            }
          }

          /**
            * What do we do when we have a one in top half?
            * First find a potential bottom 1 and then do dichotomy in only 2 sub zones
            * @param topEdgeOnTopHalf the top edge on top half
            * @return new blob edges, new queue
            */
          def oneInTopHalf(topEdgeOnTopHalf: Int) = {
            val bottomEdge = findBottom(midRowIndex - 1, topEdgeOnTopHalf)
            dichotomyIn2SubZonesByColumn(
              topEdgeOnTopHalf,
              bottomEdge,
              midColumnIndex
            )(List(
              SearchPerimeter(searchPerimeter.lowRow, midRowIndex - 1, searchPerimeter.lowColumn, midColumnIndex - 1),
              SearchPerimeter(searchPerimeter.lowRow, midRowIndex - 1, midColumnIndex + 1, searchPerimeter.highColumn)
            ))
          }

          /**
            * What do we do when we have a one in bottom half
            * First find a potential bottom 1 and then do dichotomy in only 2 sub zones
            * @param topEdgeOnBottomHalf the top edge on bottom half
            * @return new blob edges, new queue
            */
          def oneInBottomHalf(topEdgeOnBottomHalf: Int) = {
            val bottomEdge = findBottom(searchPerimeter.highRow, topEdgeOnBottomHalf)
            dichotomyIn2SubZonesByColumn(
              topEdgeOnBottomHalf,
              bottomEdge,
              midColumnIndex
            )(List(
                SearchPerimeter(midRowIndex + 1, searchPerimeter.highRow, searchPerimeter.lowColumn, midColumnIndex - 1),
                SearchPerimeter(midRowIndex + 1, searchPerimeter.highRow, midColumnIndex + 1, searchPerimeter.highColumn)
            ))

          }

          /**
            * What do we do when we don't have a one in the mid column (and no one in the row)
            * We can limit our next dichotomy to only one sub zone
            */
          lazy val noOneInColumn = {
            if (_maxEdges == emptyEdges) {
              dichotomyIn4SubZones(blobEdges) // We know nothing so far
            } else {
              val lowColumnIndex = if (_maxEdges.left > midColumnIndex) midColumnIndex + 1 else searchPerimeter.lowColumn
              val highColumnIndex = if (_maxEdges.left > midColumnIndex) searchPerimeter.highColumn else midColumnIndex - 1
              val lowRowIndex = if (_maxEdges.top > midRowIndex) midRowIndex + 1 else searchPerimeter.lowRow
              val highRowIndex = if (_maxEdges.top > midRowIndex) searchPerimeter.highRow else midRowIndex - 1
              (blobEdges,
                newQ.enqueue(
                  List(SearchPerimeter(lowRowIndex, highRowIndex, lowColumnIndex, highColumnIndex))
                )
              )
            }
          }

          val topEdgeOnTopHalf = findTop(searchPerimeter.lowRow, midRowIndex)
          if (topEdgeOnTopHalf != -1) oneInTopHalf(topEdgeOnTopHalf) else noOneInTopHalf
        }

        if (leftEdge != -1) oneInRowFound else noOneInRow
      }
    }

    if (!q.isEmpty) {
      val (edges, newQ) = queueNonEmpty
      /**
        * Potentially, we have 1 closer to the matrix edges
        */
      updateMaxEdges(edges)
      edgesByDichotomy(_maxEdges, newQ)
    }
  }


  /**
    * Find the top, bottom, left, right edges for a blob
    */
  lazy val blobEdges = {
    edgesByDichotomy(emptyEdges, Queue[SearchPerimeter](SearchPerimeter(0, blob.size - 1, 0, blob(0).size - 1)))
    _maxEdges
  }
}