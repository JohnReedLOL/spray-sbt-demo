// We use lazy vals, or lazy initialization, so that we don't have to worry about the order of definition.
lazy val commonSettings = Seq(
  organization := "com.example",
  version := "0.1",
  scalaVersion := "2.11.8",
  // scalacOptions from:
  // - https://tpolecat.github.io/2014/04/11/scalac-flags.html
  // - https://tpolecat.github.io/2017/04/25/scalac-flags.html
  scalacOptions ++= Seq(
    "-Xlint",
    "-encoding", "utf-8",                // Specify character encoding used by source files.
    "-Ywarn-unused-import",              // Warn if an import selector is not referenced.
    "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
    "-encoding", "utf-8",                // Specify character encoding used by source files.
    "-explaintypes",                     // Explain type errors in more detail.
    "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
    "-language:existentials",            // Existential types (besides wildcard types) can be written and inferred
    "-language:experimental.macros",     // Allow macro definition (besides implementation and application)
    "-language:higherKinds",             // Allow higher-kinded types
    "-language:implicitConversions",     // Allow definition of implicit functions called views
    "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
    "-Ywarn-dead-code",                  // Warn when dead code is identified.
    "-Ywarn-inaccessible",               // Warn about inaccessible types in method signatures.
    "-Ywarn-infer-any",                  // Warn when a type argument is inferred to be `Any`.
    "-Ywarn-nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Ywarn-nullary-unit",               // Warn when nullary methods return Unit.
    "-Ywarn-numeric-widen",              // Warn when numerics are widened.
    "-Ywarn-value-discard",              // Warn when non-Unit expression results are unused.
    // "-Ywarn-unused",
    "-Xfatal-warnings"
  ),
  libraryDependencies ++= {
    val akkaV = "2.3.9"
    val sprayV = "1.3.1"
    Seq(
      "io.spray"            %%  "spray-can"     % sprayV,
      "io.spray"            %%  "spray-routing" % sprayV,
      "io.spray"            %%  "spray-testkit" % sprayV  % "test",
      "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
      "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
      "org.specs2"          %%  "specs2-core"   % "2.3.11" % "test",
      "org.jvnet.mimepull"  %   "mimepull" % "1.9.5",
      "ch.qos.logback" % "logback-classic" % "1.2.3", // Java library
      "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
      "com.lihaoyi" %% "sourcecode" % "0.1.4"
    )
  },
  // Note that the REPL can’t cope with -Ywarn-unused-import or -Xfatal-warnings so you should turn them off for the console.
  // To turn on all checks that are currently considered stable, but only for compilation (and not for the tests nor the sbt console), use:
  wartremoverErrors in (Compile, compile) ++= Warts.unsafe
)

// You can check your code with the scalastyle command which is provided by the scalastyle-sbt-plugin:
// > scalastyle

// https://www.scala-sbt.org/0.13/docs/Multi-Project.html

// util sub-project
lazy val util = (project in file("util")).settings(commonSettings)

// A project may depend on code in another project. This is done by adding a dependsOn method call.

// sprayCanDemo sub-project
lazy val sprayCanDemo = (project in file("sprayCanDemo")).settings(commonSettings).dependsOn(util)

// simpleServer sub-project
lazy val simpleServer = (project in file("simpleServer")).settings(commonSettings).dependsOn(util)

// root project is an aggregate of util, sprayCanDemo, and simpleServer. Calling "compile" on root compiles all the sub-projects.
lazy val root = (project in file(".")).aggregate(util, sprayCanDemo, simpleServer)

// Now let's define some custom sbt settings and tasks
// Note: A Setting just contains a value, a Task executes something and then returns a value

val helloSetting = settingKey[String]("A setting that contains the String \"Hello\"")
// I'm going to put it in GlobalScope so that sub-projects can access this
helloSetting in Scope.GlobalScope := "Hello"

val helloTask = taskKey[Unit]("A task that prints the String \"Hello\" obtained from the setting key helloSetting")
helloTask in Scope.GlobalScope := {
  // helloTask depends on helloSetting
  val hello: String = helloSetting.value
  // The special task streams provides per-task logging and I/O via a Streams instance.
  // Documentation: https://www.scala-sbt.org/0.13/docs/Howto-Logging.html
  val log = streams.value.log

  // ---- helloTask begins here ----
  log.info(s"Build logger prints: $hello")
}

val helloWorldTask = taskKey[Unit]("A task that executes a dependent task, helloTask, and then prints World")
helloWorldTask in Scope.GlobalScope := {
  // helloWorldTask depends on helloTask
  val helloResult: Unit = helloTask.value  // helloTask happens-before helloWorldTask
  val log = streams.value.log // streams task happens-before helloWorldTask

  // ---- helloWorldTask begins here ----
  log.info(s"World!")
}
