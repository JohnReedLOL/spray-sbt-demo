// https://www.scala-sbt.org/0.13/docs/Multi-Project.html
name := "sbt-multi-project-example"

// Note: This will not compile on Scala 2.12 because Spray is not available on Scala 2.12. Use Akka HTTP instead.
// crossScalaVersions := Seq("2.11.8", "2.12.7")

// PROJECTS

// We use lazy vals, or lazy initialization, so that we don't have to worry about the order of definition.
lazy val root = project
  .in(file(".")) // root project (also known as the global project)
  .settings(settings)
  .aggregate(
    // sub-projects
    util,
    sprayCanDemo,
    simpleServer
  )

lazy val util = project
  .in(file("util"))
  .settings(
    name := "util",
    organization := "com.typesafe.scalalogging",
    settings,
    libraryDependencies ++= commonDependencies
  )

lazy val sprayCanDemo = project
  .in(file("sprayCanDemo"))
  .settings(
    name := "sprayCanDemo",
    organization := "com.example",
    settings,
    assemblySettings, // This sub-project is a deliverable, so we produce a fat-jar for it.
    mainClass in assembly := Some("com.example.Boot"), // This is the main class for sprayCanDemo.
    libraryDependencies ++= commonDependencies ++ akkaDependencies
  ) // A project may depend on code in another project. This is done by adding a dependsOn method call.
  .dependsOn(
    util
  )

lazy val simpleServer = project
  .in(file("simpleServer"))
  .settings(
    name := "simpleServer",
    organization := "spray.examples",
    settings,
    assemblySettings,                                     // This sub-project is a deliverable, so we produce a fat-jar for it.
    mainClass in assembly := Some("spray.examples.Main"), // This is the main class for simpleServer.
    // To skip the test during assembly,
    test in assembly := {},
    libraryDependencies ++= commonDependencies ++ akkaDependencies ++ Seq(
      dependencies.multipurposeInternetMailExtensions
    )
  )
  .dependsOn(
    util
  )

// DEPENDENCIES

lazy val dependencies = new {
  val akkaV         = "2.3.9"
  val sprayV        = "1.3.4"
  val logbackV      = "1.2.3"
  val scalaLoggingV = "3.8.0"

  val sprayCan                           = "io.spray"                   %% "spray-can"      % sprayV
  val sprayRouting                       = "io.spray"                   %% "spray-routing"  % sprayV
  val sprayTestkit                       = "io.spray"                   %% "spray-testkit"  % sprayV
  val akkaActor                          = "com.typesafe.akka"          %% "akka-actor"     % akkaV
  val akkaTestKit                        = "com.typesafe.akka"          %% "akka-testkit"   % akkaV
  val specificationTesting               = "org.specs2"                 %% "specs2-core"    % "2.3.11"
  val typesafeConfig                     = "com.typesafe"               % "config"          % "1.3.0"
  val multipurposeInternetMailExtensions = "org.jvnet.mimepull"         % "mimepull"        % "1.9.5"
  val logback                            = "ch.qos.logback"             % "logback-classic" % logbackV
  val scalaLogging                       = "com.typesafe.scala-logging" %% "scala-logging"  % scalaLoggingV
  val haoyiSourcecode                    = "com.lihaoyi"                %% "sourcecode"     % "0.1.4"
}

lazy val commonDependencies = Seq(
  dependencies.specificationTesting % "test",
  dependencies.typesafeConfig,
  dependencies.logback,
  dependencies.scalaLogging,
  dependencies.haoyiSourcecode
)

lazy val akkaDependencies = Seq(
  dependencies.sprayCan,
  dependencies.sprayRouting,
  dependencies.sprayTestkit % "test",
  dependencies.akkaActor,
  dependencies.akkaTestKit % "test"
)

// SETTINGS

lazy val settings = commonSettings ++ wartremoverSettings ++ scalafmtSettings

// scalacOptions from:
// - https://tpolecat.github.io/2014/04/11/scalac-flags.html
// - https://tpolecat.github.io/2017/04/25/scalac-flags.html
lazy val compilerOptions = Seq(
  "-Xlint",
  "-encoding",
  "utf-8", // Specify character encoding used by source files.
  "-Ywarn-unused-import", // Warn if an import selector is not referenced.
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-encoding",
  "utf-8", // Specify character encoding used by source files.
  "-explaintypes", // Explain type errors in more detail.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros", // Allow macro definition (besides implementation and application)
  "-language:higherKinds", // Allow higher-kinded types
  "-language:implicitConversions", // Allow definition of implicit functions called views
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
  "-Ywarn-infer-any", // Warn when a type argument is inferred to be `Any`.
  "-Ywarn-nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Ywarn-nullary-unit", // Warn when nullary methods return Unit.
  "-Ywarn-numeric-widen", // Warn when numerics are widened.
  "-Ywarn-value-discard", // Warn when non-Unit expression results are unused.
  // "-Ywarn-unused",
  "-Xfatal-warnings"
)
// Note that the Read Eval Print Loop (REPL) can’t cope with -Ywarn-unused-import or -Xfatal-warnings, so you should turn them off for the console.

lazy val commonSettings = Seq(
  version := "1.0.0",
  scalaVersion := "2.11.8",
  scalacOptions ++= compilerOptions
  // ,resolvers ++= Seq(
  //   "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
  //   Resolver.sonatypeRepo("releases"),
  //   Resolver.sonatypeRepo("snapshots")
  // )
)

lazy val wartremoverSettings = Seq(
  // To turn on all checks that are currently considered stable, but only for compilation (and not for the tests nor the sbt console), use:
  wartremoverErrors in (Compile, compile) ++= Warts.unsafe
)

// https://scalameta.org/scalafmt/docs/installation.html
lazy val scalafmtSettings = Seq(
  scalafmtOnCompile := true,
  scalafmtTestOnCompile := true,
  scalafmtVersion := "1.2.0"
)

// Deliverable sub-projects produce a fat-jar using sbt-assembly
lazy val assemblySettings = Seq(
  assemblyJarName in assembly := name.value + ".jar",
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard // discard META-INF files
    case "reference.conf"              => MergeStrategy.concat // concatenates all matching files and includes the result.
    case "application.conf"            => MergeStrategy.concat
    case _ =>
      MergeStrategy.singleOrError // bails out with an error message on conflict for everything except .conf files.
    // case _ => MergeStrategy.first // pick the first of the matching files in classpath order
  }
  // If the default is not working for you, that likely means you have multiple versions of some library pulled by your dependency graph.
  // The solution is to fix that dependency graph.
)

// The assembly task will compile your project, run your tests, and then pack your class files
// and all your dependencies into a single JAR file: target/scala_X.X.X/projectname-assembly-X.X.X.jar.
// > assembly

/*
Prepending a launch script
Your can prepend a launch script to the fat jar. This script will be a valid shell and batch script and will make the jar executable on Unix and Windows.
If you enable the shebang the file will be detected as an executable under Linux but this will cause an error message to appear on Windows.
On Windows just append a ".bat" to the files name to make it executable.
 */

import sbtassembly.AssemblyPlugin.defaultUniversalScript

assemblyOption in assembly := (assemblyOption in assembly).value
  .copy(prependShellScript = Some(defaultUniversalScript(shebang = true))) // set (shebang = false) for Windows.

assemblyJarName in assembly := s"${name.value}-${version.value}"

/*
This will prepend the following shell script to the jar:

(#!/usr/bin/env sh)
@ 2>/dev/null # 2>nul & echo off & goto BOF
:
exec java -jar $JAVA_OPTS "$0" "$@"
exit

:BOF
@echo off
java -jar %JAVA_OPTS% "%~dpnx0" %*
exit /B %errorlevel%
 */

// provides re-start and re-stop sbt commands to start and stop the application in background.
lazy val revolverSettings = Revolver.settings

// You can check your code with the scalastyle command which is provided by the scalastyle-sbt-plugin:
// > scalastyle

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
  val helloResult: Unit = helloTask.value   // helloTask happens-before helloWorldTask
  val log               = streams.value.log // streams task happens-before helloWorldTask

  // ---- helloWorldTask begins here ----
  log.info(s"World!")
}
