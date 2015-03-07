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

import java.io.{BufferedInputStream, File, FileInputStream}
import java.nio.file.Path

import grizzled.slf4j.Logging
import org.edmcouncil.util.{BaseURL, PotentialDirectory}
import org.semanticweb.owlapi.io.{OWLOntologyDocumentSource, StreamDocumentSource}
import org.semanticweb.owlapi.model.IRI

import scala.io.Source

/**
 * The ImportResolver tries to find an imported ontology based on a given base directory and base URL.
 *
 * See https://jira.edmcouncil.org/browse/RDFSER-7
 */
class ImportResolver private (baseDir: PotentialDirectory, baseUrl: BaseURL, importedIri: IRI) extends Logging {

  private[this] implicit val codec = scala.io.Codec.UTF8

  type TryPathFunction = () => Path

  val importedUrl = importedIri.toURI.toString
  val matchingBaseUrl = baseUrl.matchesWith(importedUrl)
  val baseDirExists = baseDir.directoryExists
  val baseUrlSpecified = baseUrl.isSpecified
  val remainderOfImportUrl = baseUrl.strip(importedUrl)
  val shouldBeFound = matchingBaseUrl && baseDirExists && baseUrlSpecified && remainderOfImportUrl.isDefined

  val firstPath = baseDir.directoryPath.get.resolve(remainderOfImportUrl.get)

  val pathsToBeTried: Seq[Path] = Seq (
    firstPath,
    firstPath.resolveSibling(firstPath.getFileName.toString + ".rdf"),
    firstPath.resolveSibling(firstPath.getFileName.toString + ".owl"),
    firstPath.resolveSibling(firstPath.getFileName.toString + ".ttl"),
    firstPath.resolveSibling(firstPath.getFileName.toString + ".nt"),
    firstPath.resolveSibling(firstPath.getFileName.toString + ".n3")
  )

  private[this] val tryPath = new PartialFunction[Path, File] {

    var file: Option[File] = None

    def apply(path: Path): File = file.get

    def isDefinedAt(path: Path) = {
      info(s"Trying $path")
      val triedFile = path.normalize().toFile
      file = if (triedFile.isFile) {
        info(s"Found $path")
        Some(triedFile)
      } else None
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

object ImportResolver {

  def apply(basePath: Path, baseUri: BaseURL, importedIri: IRI) =
    new ImportResolver(PotentialDirectory(basePath), baseUri, importedIri)

  def apply(baseDir: PotentialDirectory, baseUrl: BaseURL, importedIri: IRI) =
    new ImportResolver(baseDir, baseUrl, importedIri)
}

