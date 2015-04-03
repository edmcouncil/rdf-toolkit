

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.13.0")

//
// https://github.com/orrsella/sbt-sublime
//
addSbtPlugin("com.orrsella" % "sbt-sublime" % "1.0.9")

//
// http://software.clapper.org/grizzled-slf4j/
//
resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
  url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
    Resolver.ivyStylePatterns)

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.1.1")

//
// sbt-header is an sbt plugin for creating or updating file headers, e.g. copyright headers.
//
// https://github.com/sbt/sbt-header
//
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "1.4.0")

