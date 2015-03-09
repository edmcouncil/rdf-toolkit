/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Enterprise Data Management Council
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * 
 *
 * The above copyright notice and this permission notice shall be
*  included in all copies or substantial portions of the Software. 
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 */

package org.edmcouncil.rdf_serializer

import java.io.{IOException, BufferedInputStream, File, FileInputStream}
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file._

import grizzled.slf4j.Logging
import org.edmcouncil.util.{PotentialFile, BaseURL, PotentialDirectory}
import org.semanticweb.owlapi.io.{OWLOntologyDocumentSource, StreamDocumentSource}
import org.semanticweb.owlapi.model.IRI

import scala.io.Source

/**
 * The ImportResolver tries to find an imported ontology based on a given base directory and base URL.
 *
 * See https://jira.edmcouncil.org/browse/RDFSER-7
 */
class ImportResolver private (baseDir: PotentialDirectory, baseUrl: BaseURL, importedIri: IRI) extends Logging {

  import ImportResolver._

  private[this] implicit val codec = scala.io.Codec.UTF8

  type TryPathFunction = () => Path

  val importedUrl = importedIri.toURI.toString
  val matchingBaseUrl = baseUrl.matchesWith(importedUrl)
  val baseDirExists = baseDir.exists
  val baseUrlSpecified = baseUrl.isSpecified
  val remainderOfImportUrl = baseUrl.strip(importedUrl)
  val shouldBeFound = matchingBaseUrl && baseDirExists && baseUrlSpecified && remainderOfImportUrl.isDefined
  //val firstPath = baseDir.path.get.resolve(remainderOfImportUrl.get)

  private val pathsToBeTried: Seq[Path] = Seq(baseDir.path.get)

  private val rdfFileMatcherPattern = (
    "**/" + remainderOfImportUrl.get + checkFileExtensions.mkString(".{", ",", "}")
  ).replace("/.", ".").toLowerCase

  private val rdfFileMatcher = pathMatcher(s"glob:$rdfFileMatcherPattern")

  /**
   * tryPath is called for each Path entry in a Seq[Path] collection. The first one that matches is going
   * to be imported.
   */
  private val tryPath = new PartialFunction[Path, File] {

    var file: Option[File] = None

    def apply(path: Path): File = file.get

    def isDefinedAt(path: Path): Boolean = {
      //info(s"isDefinedAt: $path")
      val walker = new DirectoryWalker(rdfFileMatcher)
      Files.walkFileTree(path, walker)
      file = walker.result
      file.isDefined
    }
  }

  private[this] val tryAll = pathsToBeTried collectFirst tryPath

  /**
   * The first Path in the list of pathsToBeTried that points to an existing ontology file
   */
  val resource = tryAll

  val found = resource.isDefined

  def inputStream = resource.map((file: File) => new BufferedInputStream(new FileInputStream(file)))
  def inputSource = inputStream.map(Source.fromInputStream(_)(codec))
  def inputDocumentSource: Option[OWLOntologyDocumentSource] = inputStream.map(new StreamDocumentSource(_, importedIri))
}

object ImportResolver extends Logging {

  private val fileSystem = FileSystems.getDefault
  private def pathMatcher(syntaxAndPattern: String) = fileSystem.getPathMatcher(syntaxAndPattern)
  private val checkFileExtensions = Seq("rdf", "owl", "ttl", "nt", "n3") // TODO: Get this list from either OWLAPI or Sesame


  def apply(basePath: Path, baseUri: BaseURL, importedIri: IRI) =
    new ImportResolver(PotentialDirectory(basePath), baseUri, importedIri)

  def apply(baseDir: PotentialDirectory, baseUrl: BaseURL, importedIri: IRI) =
    new ImportResolver(baseDir, baseUrl, importedIri)
}

class DirectoryWalker(matcher: PathMatcher) extends SimpleFileVisitor[Path] with Logging {

  var result: Option[File] = None

  /**
   * Compares the pattern against the file or directory name and returns true if we found a valid RDF file
   */
  private def checkFile(path: Path): Boolean = {
    val normalizedPath = path.normalize()
    val potentialFile = PotentialFile(Some(normalizedPath.toString.toLowerCase))
    if (! matcher.matches(potentialFile.path.get)) {
      debug(s"Tried $normalizedPath, no match ${potentialFile}")
      return false
    }
    info(s"Found $normalizedPath -> ${normalizedPath.toRealPath().toString}")
    result = Some(path.toFile)
    true
  }

  /**
   * Invoke the pattern matching method on each file.
   */
  override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult =
    if (attrs.isRegularFile && checkFile(file)) FileVisitResult.TERMINATE else FileVisitResult.CONTINUE

  /**
   * Invoke the pattern matching method on each directory.
   */
  override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = FileVisitResult.CONTINUE

  override def visitFileFailed(file: Path, exc: IOException): FileVisitResult = {
    println(exc)
    FileVisitResult.CONTINUE
  }
}
