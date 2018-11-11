logLevel := Level.Debug

// Documentation: https://johnreedlol.github.io/sbt-revolver/
addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")

// Documentation: http://www.wartremover.org/
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.3.7")

// Documentation: http://www.scalastyle.org/sbt.html
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")

// Documentation: https://johnreedlol.github.io/sbt-git/
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")

// Documentation: https://coursier.github.io/coursier/1.1.0-SNAPSHOT/docs/intro
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.2")

// Documentation: https://johnreedlol.github.io/sbt-updates/
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.2")

// Documentation: https://johnreedlol.github.io/neo-sbt-scalafmt/
addSbtPlugin("com.lucidchart" % "sbt-scalafmt-coursier" % "1.12")

// Documentation: https://johnreedlol.github.io/sbt-assembly/
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.8")

// Note: This more recent scalafmt plugin does not support sbt 0.13, only sbt 1.X
// addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "1.5.1")