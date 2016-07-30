package org.edmcouncil.rdf_toolkit

import java.io.{ FileInputStream, File }
import java.nio.charset.Charset

import org.openrdf.model.{ Model, Literal, BNode, Statement }
import org.slf4j.Logger

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.{ Codec, BufferedSource }
import scala.language.postfixOps

/**
 * Common functionality for tests of Sesame sorted RDF writers.
 */
trait SesameSortedWriterSpecSupport {

  val logger: Logger

  /** Deletes the given file or directory.  For a directory, deletes recursively. */
  def deleteFile(file: File): Unit = {
    if (file isDirectory) {
      for (childFile ← file.listFiles()) {
        deleteFile(childFile)
      }
    }
    file delete ()
  }

  /** Creates a clean directory at the given path.  Deletes any existing contents. */
  def mkCleanDir(dirPath: String): File = {
    val newDir = new File(dirPath)
    if (newDir exists) { deleteFile(newDir) }
    newDir mkdirs ()
    newDir
  }

  /** Collects all of the files in a directory tree. */
  def listDirTreeFiles(dir: File): Seq[File] = {
    val result = new ListBuffer[File]();
    if (dir.exists()) {
      if (dir isDirectory) {
        for (file ← dir.listFiles()) {
          result ++= listDirTreeFiles(file)
        }
      } else {
        result += dir // 'dir' is actually a file
      }
    }
    result
  }

  /** Sets the extension part of a filename path, e.g. ".ttl" or "_modified.ttl". */
  def setFilePathExtension(filePath: String, fileExtension: String): String = {
    if (filePath.contains(".")) {
      s"${filePath.substring(0, filePath.lastIndexOf("."))}$fileExtension"
    } else {
      s"$filePath$fileExtension"
    }
  }

  /** Reads the contents of a file into a String. */
  def getFileContents(file: File): String = new BufferedSource(new FileInputStream(file)).mkString

  def getFileContents(file: File, encoding: String): String = new BufferedSource(new FileInputStream(file))(new Codec(Charset.forName(encoding))).mkString

  def compareFiles(file1: File, file2: File, encoding: String): Boolean = {
    logger.debug("CompareFiles:")
    logger.debug(s"   left = ${file1.getAbsolutePath}")
    logger.debug(s"  right = ${file2.getAbsolutePath}")
    val source1Lines = new BufferedSource(new FileInputStream(file1))(new Codec(Charset.forName(encoding))).getLines()
    val source2Lines = new BufferedSource(new FileInputStream(file2))(new Codec(Charset.forName(encoding))).getLines()
    compareStringIterators(source1Lines, source2Lines, file1, file2)
    logger.debug("CompareFiles: done")
    true
  }

  def compareStringIterators(iter1: Iterator[String], iter2: Iterator[String], file1: File, file2: File): Boolean = {
    var lineCount = 0
    while (iter1.hasNext || iter2.hasNext) {
      lineCount += 1
      if (!iter2.hasNext) {
        logger.error(s"left file [${file1 getName}] has more lines than right [${file2 getName}] ($lineCount+): ${iter1.next()}")
        return false
      }
      if (!iter1.hasNext) {
        logger.error(s"right file [${file2 getName}] has more lines than left [${file1 getName}] ($lineCount+): ${iter2.next()}")
        return false
      }
      val line1 = iter1.next()
      val line2 = iter2.next()
      if (!compareStrings(line1, line2, lineCount, file1, file2)) {
        return false
      }
    }
    true
  }

  def compareStrings(str1: String, str2: String, lineCount: Int, file1: File, file2: File): Boolean = {
    if ((str1.length >= 1) || (str2.length >= 1)) {
      var index = 0
      while ((str1.length > index) || (str2.length > index)) {
        if (str2.length <= index) {
          logger.error(s"left line [${file1 getName}] ($lineCount) is longer than (${index + 1}+) than right [${file2 getName}]: tail = ${str1.substring(index)}")
          return false
        }
        if (str1.length <= index) {
          logger.error(s"right line [${file2 getName}] ($lineCount) is longer than (${index + 1}+) than left [${file1 getName}]: tail = ${str2.substring(index)}")
          return false
        }
        var leftCh = str1.charAt(index)
        var rightCh = str2.charAt(index)
        if (leftCh != rightCh) {
          logger.error(s"char mismatch at $lineCount:${index + 1} => [${file1 getName}] $leftCh [#${leftCh.toShort}] != [${file2 getName}] $rightCh [#${rightCh.toShort}]")
        }
        index += 1
      }
      true
    } else {
      true
    }
  }

  /** Compares whether two triples match, allowing for blank nodes. */
  def triplesMatch(st1: Statement, st2: Statement): Boolean = {
    if ((st1.getSubject == st2.getSubject) || (st1.getSubject.isInstanceOf[BNode] && st2.getSubject.isInstanceOf[BNode])) {
      if (st1.getPredicate == st2.getPredicate) {
        if (st1.getObject.isInstanceOf[Literal] && st2.getObject.isInstanceOf[Literal]) {
          st1.getObject.stringValue.replaceAll("\\s+", " ").trim == st2.getObject.stringValue.replaceAll("\\s+", " ").trim
        } else if ((st1.getObject == st2.getObject) || (st1.getObject.isInstanceOf[BNode] && st2.getObject.isInstanceOf[BNode])) {
          true
        } else {
          false
        }
      } else {
        false
      }
    } else {
      false
    }
  }

  def assertTriplesMatch(model1: Model, model2: Model): Unit = {
    val unmatchedTriples1to2 = new mutable.HashSet[Statement]()
    for (st1 ← model1) {
      var triplesMatch1to2 = false
      for (st2 ← model2 if !triplesMatch1to2) {
        if (triplesMatch(st1, st2)) { triplesMatch1to2 = true }
      }
      if (!triplesMatch1to2) { unmatchedTriples1to2 += st1 }
    }
    val unmatchedTriples2to1 = new mutable.HashSet[Statement]()
    for (st2 ← model2) {
      var triplesMatch2to1 = false
      for (st1 ← model1 if !triplesMatch2to1) {
        if (triplesMatch(st2, st1)) { triplesMatch2to1 = true }
      }
      if (!triplesMatch2to1) { unmatchedTriples2to1 += st2 }
    }
    assert(((unmatchedTriples1to2.size == 0) && (unmatchedTriples2to1.size == 0)).asInstanceOf[Boolean], s"found unmatched triples: [${unmatchedTriples1to2.size}/${model1.size}]{{{ $unmatchedTriples1to2 }}}, [${unmatchedTriples2to1.size}/${model2.size}]{{{ $unmatchedTriples2to1 }}}")
  }

}
