import sbt._
import sbt.Keys._
import scala.util.Try
import scala.io.Source
import scala.sys.process.Process

/**
 * Add to build.sbt:
 *
 * resourceGenerators in Compile <+= (
 *   resourceManaged in Compile, name, version
 * ) map {
 *   (dir, n, v) => BooterPropertiesGenerator(dir, n, v)
 * }
 */
object BooterPropertiesGenerator {

  def apply(
    dir: File,
    licenses: Seq[(String, URL)],
    organization: String, 
    name: String, 
    version: String,
    scalaVersion: String
  ): Seq[File] = generate(dir, licenses, organization, name, version, scalaVersion)

  /**
   * Generate the file ./target/scala-<version>/resource_managed/main/booter.properties
   * when sbt run or package is executed.
   */
  private def generate(
    dir: File,
    licenses: Seq[(String, URL)],
    organization: String, 
    name: String, 
    version: String,
    scalaVersion: String
  ): Seq[File] = {

    val file          = dir / "booter.properties"
    val gitLongHash   = Process("git rev-parse HEAD").lines.head
    val gitShortHash  = Process("git rev-parse --short HEAD").lines.head
    val gitBranch     = Process("git branch").lines.head.substring(2)
    val now           = new java.util.Date()
    val year          = new java.text.SimpleDateFormat("yyyy").format(now)
    val nowString     = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(now)
    val content = s"""
      |#
      |# Copyright © $year EDM Council
      |#
      |booter.copyright      = Copyright © $year EDM Council
      |booter.license        = ${licenses}
      |booter.organization   = $organization
      |booter.name           = $name
      |booter.version        = $version
      |booter.scala.version  = $scalaVersion
      |booter.git.hash.long  = $gitLongHash
      |booter.git.hash.short = $gitShortHash
      |booter.git.branch     = $gitBranch
      |booter.generated.at   = $nowString
      |#
    """.stripMargin

    def stripContent(x: String) = x.replaceAll("booter.generated.at.*", "").replaceAll("""(?m)\s+$""", "")

    /**
     * Write content to file if different from existing content
     */
    def writeIfChanged(file: File, content: String) {

      val currentFileContent = Try(Source.fromFile(file).mkString).getOrElse("")

      if (stripContent(currentFileContent) != stripContent(content)) {
        IO.write(file, content)
      }
    }

    //
    // We need to check here if the new content is actually new so that we do not generate
    // a new file unnecessarily. The Play Framework's run command will otherwise update
    // everything for each incoming request.
    // See this discussion:
    // https://github.com/playframework/playframework/issues/903#issuecomment-15603997
    //
    writeIfChanged(file, content)

    Seq(file)
  }
}