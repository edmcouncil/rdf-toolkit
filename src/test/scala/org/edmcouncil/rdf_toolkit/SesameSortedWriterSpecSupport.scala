package org.edmcouncil.rdf_toolkit

import java.io.{ FileInputStream, File }
import java.nio.charset.Charset

import org.slf4j.Logger

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
    compareStringIterators(source1Lines, source2Lines)
    logger.debug("CompareFiles: done")
    true
  }

  def compareStringIterators(iter1: Iterator[String], iter2: Iterator[String]): Boolean = {
    var lineCount = 0
    while (iter1.hasNext || iter2.hasNext) {
      lineCount += 1
      if (!iter2.hasNext) {
        logger.error(s"left file has more lines than right ($lineCount+): ${iter1.next()}")
        return false
      }
      if (!iter1.hasNext) {
        logger.error(s"right file has more lines than left ($lineCount+): ${iter2.next()}")
        return false
      }
      val line1 = iter1.next()
      val line2 = iter2.next()
      if (!compareStrings(line1, line2, lineCount)) {
        return false
      }
    }
    true
  }

  def compareStrings(str1: String, str2: String, lineCount: Int): Boolean = {
    if ((str1.length >= 1) || (str2.length >= 1)) {
      var index = 0
      while ((str1.length > index) || (str2.length > index)) {
        if (str2.length <= index) {
          logger.error(s"left line ($lineCount) is longer than (${index + 1}+) than right: tail = ${str1.substring(index)}")
          return false
        }
        if (str1.length <= index) {
          logger.error(s"right line ($lineCount) is longer than (${index + 1}+) than left: tail = ${str2.substring(index)}")
          return false
        }
        var leftCh = str1.charAt(index)
        var rightCh = str2.charAt(index)
        if (leftCh != rightCh) {
          logger.error(s"char mismatch at $lineCount:${index + 1} => $leftCh [#${leftCh.toShort}] != $rightCh [#${rightCh.toShort}]")
        }
        index += 1
      }
      true
    } else {
      true
    }
  }

}
