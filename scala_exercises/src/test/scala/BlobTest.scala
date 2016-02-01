/**
  * Created by herve on 1/28/16.
  */
import org.specs2.Specification

class BlobTest extends Specification {
  def is = s2"""
   This is a specification to check the 'blob edges'
   Empty blob return -1                                                                    $e1
   Simple blob                                                                             $e2
   Exercise blob                                                                           $e3
   More complex blob                                                                       $e4
   Only ones in second row                                                                 $e5
   Only ones in bottom                                                                     $e6
   Cross                                                                                   $e7
   X                                                                                       $e8
   Almost empty                                                                            $e9
   Rhombus                                                                                 $e10
   """

  def e1 = new Blob(Vector(
    Vector(0,0,0),
    Vector(0,0,0),
    Vector(0,0,0),
    Vector(0,0,0)
  )).blobEdges must_== BlobEdges(-1, -1 ,-1 ,-1)
  def e2 = new Blob(Vector(
    Vector(0,0,0),
    Vector(0,1,0),
    Vector(1,1,0),
    Vector(0,0,1)
  )).blobEdges must_== BlobEdges(1, 3 ,0 ,2)
  def e3 = {
    val b = new Blob(Vector(
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
    ))
    b.blobEdges must_== BlobEdges(1, 7, 2, 6)
    b.reads must_== 44
  }
  def e4 = new Blob(Vector(
    Vector(0,0,0,0,1,0,0,0,0,0),
    Vector(0,0,1,1,1,0,0,0,0,0),
    Vector(0,0,1,1,1,1,1,0,0,0),
    Vector(0,0,1,0,0,0,1,0,0,0),
    Vector(0,0,1,1,1,1,1,0,0,0),
    Vector(0,1,0,0,1,0,1,0,0,0),
    Vector(1,0,0,0,1,0,1,0,0,0),
    Vector(0,0,0,0,1,1,1,0,0,0),
    Vector(0,0,1,1,0,0,0,1,0,0),
    Vector(0,0,0,0,1,0,0,0,0,0),
    Vector(0,0,0,0,1,1,0,0,0,0)
  )).blobEdges must_== BlobEdges(0, 10 ,0 ,7)
  def e5 = new Blob(Vector(
    Vector(0,0,0,0,0,0,0,0,0,0),
    Vector(0,0,1,1,1,1,0,0,0,0)
  )).blobEdges must_== BlobEdges(1, 1 ,2 ,5)
  def e6 = new Blob(Vector(
    Vector(0,0,0,0,0,0,0,0,0,0),
    Vector(0,0,0,0,0,0,0,0,0,0),
    Vector(0,0,0,0,0,0,0,0,0,0),
    Vector(0,0,0,0,0,0,0,0,0,0),
    Vector(0,0,0,0,0,0,0,0,0,0),
    Vector(0,0,0,0,0,0,0,0,0,0),
    Vector(0,0,0,0,1,0,0,0,0,0),
    Vector(0,0,0,1,0,1,0,0,0,0),
    Vector(0,0,1,0,0,0,1,1,1,1)
  )).blobEdges must_== BlobEdges(6, 8 ,2 ,9)
  def e7 = new Blob(Vector(
    Vector(0,0,0,0,1,0,0,0,0,0),
    Vector(0,0,0,0,1,0,0,0,0,0),
    Vector(0,0,0,0,1,0,0,0,0,0),
    Vector(0,0,0,0,1,0,0,0,0,0),
    Vector(1,1,1,1,1,1,1,1,1,1),
    Vector(0,0,0,0,1,0,0,0,0,0),
    Vector(0,0,0,0,1,0,0,0,0,0),
    Vector(0,0,0,0,1,0,0,0,0,0),
    Vector(0,0,0,0,1,0,0,0,0,0)
  )).blobEdges must_== BlobEdges(0, 8 ,0 ,9)
  def e8 = new Blob(Vector(
    Vector(1,0,0,0,1,0,0,0,1,0),
    Vector(0,1,0,0,1,0,0,1,0,0),
    Vector(0,0,1,0,1,0,1,0,0,0),
    Vector(0,0,0,1,1,1,0,0,0,0),
    Vector(0,0,0,0,1,0,0,0,0,0),
    Vector(0,0,0,1,1,1,0,0,0,0),
    Vector(0,0,1,0,1,0,1,0,0,0),
    Vector(0,1,0,0,1,0,0,1,0,0),
    Vector(1,0,0,0,1,0,0,0,1,0)
  )).blobEdges must_== BlobEdges(0, 8 ,0 ,8)
  def e9 = new Blob(Vector(
    Vector(0,0,0,0,0,0,0,0,0,0),
    Vector(0,0,0,0,0,0,0,0,0,0),
    Vector(0,0,0,0,0,0,0,0,0,0),
    Vector(0,0,0,0,0,0,0,0,0,0),
    Vector(1,0,0,0,0,0,0,0,0,0),
    Vector(0,1,0,0,0,0,0,0,0,0),
    Vector(0,0,0,0,0,0,0,0,0,0),
    Vector(0,0,0,0,0,0,0,0,0,0),
    Vector(0,0,0,0,0,0,0,0,0,0)
  )).blobEdges must_== BlobEdges(4, 5 ,0 ,1)
  def e10 = new Blob(Vector(
    Vector(0,0,0,0,0,0,0,0,0,0),
    Vector(0,0,0,0,0,0,0,0,0,0),
    Vector(0,0,0,0,1,0,0,0,0,0),
    Vector(0,0,0,1,0,1,0,0,0,0),
    Vector(0,0,1,0,0,0,1,0,0,0),
    Vector(0,0,0,1,0,1,0,0,0,0),
    Vector(0,0,0,0,1,0,0,0,0,0),
    Vector(0,0,0,0,0,0,0,0,0,0),
    Vector(0,0,0,0,0,0,0,0,0,0)
  )).blobEdges must_== BlobEdges(2, 6 ,2 ,6)
}
