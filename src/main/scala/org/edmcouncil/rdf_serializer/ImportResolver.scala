package org.edmcouncil.rdf_serializer

import java.io.{BufferedInputStream, FileInputStream, File}
import java.nio.file.Path

import grizzled.slf4j.Logging
import org.edmcouncil.rdf_serializer.PotentialFile
import org.semanticweb.owlapi.io.StreamDocumentSource

import scala.io.Source

/**
 * The ImportResolver tries to find an imported ontology based on a given base directory and base URL.
 *
 * See https://jira.edmcouncil.org/browse/RDFSER-7
 */
class ImportResolver private (baseDir: PotentialDirectory, baseUrl: BaseURL, importedUrl: String) extends Logging {

  private[this] implicit val codec = scala.io.Codec.UTF8

  type TryPathFunction = () => Path

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
  def inputDocumentSource = inputStream.map(new StreamDocumentSource(_))
}

object ImportResolver {

  def apply(baseDir: PotentialDirectory, baseUrl: BaseURL, importedUrl: String) =
    new ImportResolver(baseDir, baseUrl, importedUrl)
}

