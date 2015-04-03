
val license = scala.io.Source.fromFile("LICENSE.md").getLines mkString ("/**\n * ", "\n * ", "\n */\n")

headers := Map(
  "scala" -> (de.heikoseeberger.sbtheader.HeaderPattern.cStyleBlockComment, license),
  "java" -> (de.heikoseeberger.sbtheader.HeaderPattern.cStyleBlockComment, license)
)
