package org.edmcouncil.rdf_serializer

/**
 * The BaseURL is used in combination with the base directory to figure out where
 * imports are located on disk.
 */
class BaseURL private (val url: Option[String]) {
  
  val isSpecified = url.isDefined

  /**
   * @return true if the importUrl is covered by the base URL
   */
  def matchesWith(importUrl: String) = url.exists(importUrl.startsWith)

  def endsWithSlash = url.exists(_.endsWith("/"))

  def endsWithHash = url.exists(_.endsWith("#"))

  def urlEndingWithSlash = if (endsWithSlash || endsWithHash) url else url.map(_ + "/")

  def strip(importUrl: String) = urlEndingWithSlash.map(importUrl.stripPrefix)


}

object BaseURL {

  def apply(url: String): BaseURL = apply(Some(url))
  def apply(url: Option[String]) = new BaseURL(url)
}
