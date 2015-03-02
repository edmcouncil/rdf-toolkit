package org.edmcouncil.rdf_serializer

import java.io.OutputStream

import org.scalatest.{Matchers, WordSpecLike}

abstract class UnitSpec extends WordSpecLike with Matchers with OutputSuppressor

trait OutputSuppressor {
  def suppressOutput[T](thunk: => T): T = {

    val bitBucket = new OutputStream() {
      def write(b: Int) {}
    }

    Console.withOut(bitBucket) {
      return thunk
    }
  }
}
