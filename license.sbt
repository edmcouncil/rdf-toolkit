
headerLicense := Some(HeaderLicense.Custom(
  scala.io.Source.fromFile("LICENSE.md").getLines mkString ("\n")
))

headerMappings := Map(
  HeaderFileType.scala -> HeaderCommentStyle.cStyleBlockComment,
  HeaderFileType.java -> HeaderCommentStyle.cStyleBlockComment
)
