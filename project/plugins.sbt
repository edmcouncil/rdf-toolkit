

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.11.2")

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
