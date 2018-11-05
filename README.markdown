## Learning Spray and the Scala Build Tool

This is a demo project to cover the basics of Spray and the Scala Build Tool.

This demo project consists of a root project with three sub-projects. You can take a look at them using the `projects` sbt command:

~~~~ 
IJ]> projects
[info] In file:/Users/john-michaelreed/Downloads/NewDownloads/sbt-0.13/lesson/spray-template/
[info] 	 * root
[info] 	   simpleServer
[info] 	   sprayCanDemo
[info] 	   util
~~~~
 
You can switch to one of the sub-projects like so:

~~~~ 
[IJ]> project sprayCanDemo
[info] Set current project to sprayCanDemo (in build file:/Users/john-michaelreed/Downloads/NewDownloads/sbt-0.13/lesson/spray-template/)
[IJ]> projects
[info] In file:/Users/john-michaelreed/Downloads/NewDownloads/sbt-0.13/lesson/spray-template/
[info] 	   root
[info] 	   simpleServer
[info] 	 * sprayCanDemo
[info] 	   util
~~~~ 

The projects are defined in the root directory's build.sbt file:

~~~~
// util sub-project
lazy val util = (project in file("util")).settings(commonSettings)

// A project may depend on code in another project. This is done by adding a dependsOn method call.

// sprayCanDemo sub-project
lazy val sprayCanDemo = (project in file("sprayCanDemo")).settings(commonSettings).dependsOn(util)

// simpleServer sub-project
lazy val simpleServer = (project in file("simpleServer")).settings(commonSettings).dependsOn(util)

// root project is an aggregate of util, sprayCanDemo, and simpleServer. Calling "compile" on root compiles all of them.
lazy val root = (project in file(".")).aggregate(util, sprayCanDemo, simpleServer)
~~~~

To compile all sub-projects and run a style-check on them:

~~~~ 
[IJ]> project root
[info] Set current project to root (in build file:/Users/john-michaelreed/Downloads/NewDownloads/sbt-0.13/lesson/spray-template/)
[IJ]> < check_style_script.properties
Begin Script!
[info] ans: Unit = null
[info] scalastyle using config /Users/john-michaelreed/Downloads/NewDownloads/sbt-0.13/lesson/spray-template/scalastyle-config.xml
[info] scalastyle using config /Users/john-michaelreed/Downloads/NewDownloads/sbt-0.13/lesson/spray-template/scalastyle-config.xml
[info] scalastyle using config /Users/john-michaelreed/Downloads/NewDownloads/sbt-0.13/lesson/spray-template/scalastyle-config.xml
[info] scalastyle using config /Users/john-michaelreed/Downloads/NewDownloads/sbt-0.13/lesson/spray-template/scalastyle-config.xml
[info] scalastyle Processed 0 file(s)
[info] scalastyle Found 0 errors
[info] scalastyle Found 0 warnings
[info] scalastyle Found 0 infos
[info] scalastyle Finished in 10 ms
[success] created output: /Users/john-michaelreed/Downloads/NewDownloads/sbt-0.13/lesson/spray-template/target
[info] scalastyle Processed 2 file(s)
[info] scalastyle Found 0 errors
[info] scalastyle Found 0 warnings
[info] scalastyle Found 0 infos
[info] scalastyle Finished in 4 ms
[success] created output: /Users/john-michaelreed/Downloads/NewDownloads/sbt-0.13/lesson/spray-template/sprayCanDemo/target
[info] scalastyle Processed 2 file(s)
[info] scalastyle Found 0 errors
[info] scalastyle Found 0 warnings
[info] scalastyle Found 0 infos
[info] scalastyle Finished in 0 ms
[success] created output: /Users/john-michaelreed/Downloads/NewDownloads/sbt-0.13/lesson/spray-template/util/target
[info] scalastyle Processed 4 file(s)
[info] scalastyle Found 0 errors
[info] scalastyle Found 0 warnings
[info] scalastyle Found 0 infos
[info] scalastyle Finished in 1 ms
[success] created output: /Users/john-michaelreed/Downloads/NewDownloads/sbt-0.13/lesson/spray-template/simpleServer/target
[success] Total time: 2 s, completed Nov 5, 2018 12:13:01 AM
Style Check Done!
[info] ans: Unit = null
[success] Total time: 0 s, completed Nov 5, 2018 12:13:01 AM
Compile Done!
[info] ans: Unit = null
[info] Build logger prints: Hello
[info] World!
[success] Total time: 0 s, completed Nov 5, 2018 12:13:01 AM
helloWorldTask Done!
[info] ans: Unit = null
[IJ]> 
~~~~ 

The above sbt script is located in `check_style_script.properties`. 

This script runs a task, `helloWorldTask`, that we defined in the build.sbt file:

~~~~ 
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
~~~~ 

If you have ever used ant or rake, sbt tasks are kind of like that.

You can inspect each of these definitions from the sbt shell like so:

~~~~ 
[IJ]> inspect helloSetting
[info] Setting: java.lang.String = Hello
[info] Description:
[info] 	A setting that contains the String "Hello"
[info] Provided by:
[info] 	*/*:helloSetting
[info] Defined at:
[info] 	/Users/john-michaelreed/Downloads/NewDownloads/sbt-0.13/lesson/spray-template/build.sbt:77
[info] Delegates:
[info] 	simpleServer/*:helloSetting
[info] 	{.}/*:helloSetting
[info] 	*/*:helloSetting
[info] Related:
[info] 	*/*:helloSetting
[IJ]> 
[IJ]> inspect helloTask
[info] Task: Unit
[info] Description:
[info] 	A task that prints the String "Hello" obtained from the setting key helloSetting
[info] Provided by:
[info] 	*/*:helloTask
[info] Defined at:
[info] 	/Users/john-michaelreed/Downloads/NewDownloads/sbt-0.13/lesson/spray-template/build.sbt:81
[info] Delegates:
[info] 	simpleServer/*:helloTask
[info] 	{.}/*:helloTask
[info] 	*/*:helloTask
[info] Related:
[info] 	*/*:helloTask
[IJ]> 
[IJ]> inspect helloWorldTask
[info] Task: Unit
[info] Description:
[info] 	A task that executes a dependent task, helloTask, and then prints World
[info] Provided by:
[info] 	*/*:helloWorldTask
[info] Defined at:
[info] 	/Users/john-michaelreed/Downloads/NewDownloads/sbt-0.13/lesson/spray-template/build.sbt:94
[info] Delegates:
[info] 	simpleServer/*:helloWorldTask
[info] 	{.}/*:helloWorldTask
[info] 	*/*:helloWorldTask
[info] Related:
[info] 	*/*:helloWorldTask
~~~~ 

And you can execute them like so:

~~~~ 
[IJ]> helloSetting
[info] Hello
[IJ]> 
[IJ]> helloTask
[info] Build logger prints: Hello
[success] Total time: 0 s, completed Nov 5, 2018 12:54:25 AM
[IJ]> 
[IJ]> helloWorldTask
[info] Build logger prints: Hello
[info] World!
[success] Total time: 0 s, completed Nov 5, 2018 12:54:29 AM
~~~~ 

Now, the three sub-projects are `util`, `sprayCanDemo`, and `simpleServer`:

- `util` provides a custom logging trait that can be used by `sprayCanDemo` and `simpleServer`.

- `sprayCanDemo` and `simpleServer` depend on `util`.

To run the tests in `sprayCanDemo`:

~~~~ 
[IJ]> project sprayCanDemo
[info] Set current project to sprayCanDemo (in build file:/Users/john-michaelreed/Downloads/NewDownloads/sbt-0.13/lesson/spray-template/)
[IJ]> test
[info] Compiling 1 Scala source to /Users/john-michaelreed/Downloads/NewDownloads/sbt-0.13/lesson/spray-template/sprayCanDemo/target/scala-2.11/test-classes...
[DEBUG] [11/05/2018 00:16:18.230] [pool-41-thread-3] [EventStream(akka://com-example-MyServiceSpec)] logger log1-Logging$DefaultLogger started
[DEBUG] [11/05/2018 00:16:18.232] [pool-41-thread-3] [EventStream(akka://com-example-MyServiceSpec)] Default Loggers started
[DEBUG] [11/05/2018 00:16:19.473] [com-example-MyServiceSpec-akka.actor.default-dispatcher-4] [EventStream] shutting down: StandardOutLogger started
[info] MyServiceSpec
[info] 
[info] MyService should
[info] + return a greeting for GET requests to the root path
[info] + leave GET requests to other paths unhandled
[info] + return a MethodNotAllowed error for PUT requests to the root path
[info] 
[info] Total for specification MyServiceSpec
[info] Finished in 444 ms
[info] 3 examples, 0 failure, 0 error
[info] Passed: Total 3, Failed 0, Errors 0, Passed 3
[success] Total time: 16 s, completed Nov 5, 2018 12:16:21 AM
~~~~ 

To run `sprayCanDemo` as a background process using the `sbt-revolver` plugin:

~~~~ 
[IJ]> project sprayCanDemo
[info] Set current project to sprayCanDemo (in build file:/Users/john-michaelreed/Downloads/NewDownloads/sbt-0.13/lesson/spray-template/)
[IJ]> re-start
[info] Application sprayCanDemo not yet started
[info] Starting application sprayCanDemo in the background ...
sprayCanDemo Starting com.example.Boot.main()
[success] Total time: 0 s, completed Nov 5, 2018 12:25:41 AM
[IJ]> sprayCanDemo [DEBUG] [11/05/2018 00:25:42.831] [main] [EventStream(akka://on-spray-can)] logger log1-Logging$DefaultLogger started
sprayCanDemo [DEBUG] [11/05/2018 00:25:42.832] [main] [EventStream(akka://on-spray-can)] Default Loggers started
sprayCanDemo 00:25:43.460 [main] INFO  - result: List(scala.concurrent.impl.CallbackRunnable@ca263c2) - com.example.Boot(Boot.scala:27)
sprayCanDemo 00:25:43.473 [main] ERROR  - This is a test log! - com.example.Boot(Boot.scala:28)
sprayCanDemo [DEBUG] [11/05/2018 00:25:43.970] [on-spray-can-akka.actor.default-dispatcher-4] [akka://on-spray-can/user/IO-HTTP/listener-0] Binding to localhost/127.0.0.1:8080
sprayCanDemo [DEBUG] [11/05/2018 00:25:44.025] [on-spray-can-akka.actor.default-dispatcher-2] [akka://on-spray-can/system/IO-TCP/selectors/$a/0] Successfully bound to /127.0.0.1:8080
sprayCanDemo [INFO] [11/05/2018 00:25:44.028] [on-spray-can-akka.actor.default-dispatcher-4] [akka://on-spray-can/user/IO-HTTP/listener-0] Bound to localhost/127.0.0.1:8080
sprayCanDemo [DEBUG] [11/05/2018 00:26:03.549] [on-spray-can-akka.actor.default-dispatcher-4] [akka://on-spray-can/system/IO-TCP/selectors/$a/0] New connection accepted
sprayCanDemo [DEBUG] [11/05/2018 00:26:03.631] [on-spray-can-akka.actor.default-dispatcher-4] [akka://on-spray-can/user/IO-HTTP/listener-0/0] Dispatching GET request to http://127.0.0.1:8080/ to handler Actor[akka://on-spray-can/system/IO-TCP/selectors/$a/1#990745101]
re-stop
[info] Stopping application sprayCanDemo (by killing the forked JVM) ...
sprayCanDemo ... finished with exit code 143
[success] Total time: 0 s, completed Nov 5, 2018 12:26:12 AM
~~~~ 

Navigate to `127.0.0.1:8080` in your web browser. You should see something like this:

~~~~ 
Say hello to spray-routing on spray-can!
~~~~ 

Note that the `sbt-revolver` plugin can be used along with the auto-reload feature to make it so that the code auto-reloads 
every time you edit a file.
 
~~~~ 
[IJ]> ~ re-start
[info] Application sprayCanDemo not yet started
[info] Starting application sprayCanDemo in the background ...
sprayCanDemo Starting com.example.Boot.main()
[success] Total time: 0 s, completed Nov 5, 2018 12:28:01 AM
1. Waiting for source changes... (press enter to interrupt)
sprayCanDemo [DEBUG] [11/05/2018 00:28:02.866] [main] [EventStream(akka://on-spray-can)] logger log1-Logging$DefaultLogger started
sprayCanDemo [DEBUG] [11/05/2018 00:28:02.867] [main] [EventStream(akka://on-spray-can)] Default Loggers started
sprayCanDemo 00:28:03.445 [main] INFO  - result: List(scala.concurrent.impl.CallbackRunnable@ca263c2) - com.example.Boot(Boot.scala:27)
sprayCanDemo 00:28:03.447 [main] ERROR  - This is a test log! - com.example.Boot(Boot.scala:28)
sprayCanDemo [DEBUG] [11/05/2018 00:28:03.968] [on-spray-can-akka.actor.default-dispatcher-4] [akka://on-spray-can/user/IO-HTTP/listener-0] Binding to localhost/127.0.0.1:8080
sprayCanDemo [DEBUG] [11/05/2018 00:28:04.029] [on-spray-can-akka.actor.default-dispatcher-2] [akka://on-spray-can/system/IO-TCP/selectors/$a/0] Successfully bound to /127.0.0.1:8080
sprayCanDemo [INFO] [11/05/2018 00:28:04.033] [on-spray-can-akka.actor.default-dispatcher-4] [akka://on-spray-can/user/IO-HTTP/listener-0] Bound to localhost/127.0.0.1:8080
[info] Compiling 1 Scala source to /Users/john-michaelreed/Downloads/NewDownloads/sbt-0.13/lesson/spray-template/sprayCanDemo/target/scala-2.11/classes...
[info] Stopping application sprayCanDemo (by killing the forked JVM) ...
sprayCanDemo ... finished with exit code 143
[info] Starting application sprayCanDemo in the background ...
sprayCanDemo Starting com.example.Boot.main()
[success] Total time: 2 s, completed Nov 5, 2018 12:28:30 AM
2. Waiting for source changes... (press enter to interrupt)
~~~~ 
 
The `simpleServer` sub-project does not have any tests, but you can run it via the `re-start` command as well:

~~~~
[IJ]> project simpleServer
[info] Set current project to simpleServer (in build file:/Users/john-michaelreed/Downloads/NewDownloads/sbt-0.13/lesson/spray-template/)
[IJ]> re-start
[info] Application simpleServer not yet started
[info] Starting application simpleServer in the background ...
simpleServer Starting spray.examples.Main.main()
[success] Total time: 0 s, completed Nov 5, 2018 12:31:53 AM
[IJ]> simpleServer [DEBUG] [11/05/2018 00:31:54.088] [main] [EventStream(akka://default)] logger log1-Logging$DefaultLogger started
simpleServer [DEBUG] [11/05/2018 00:31:54.090] [main] [EventStream(akka://default)] Default Loggers started
simpleServer [DEBUG] [11/05/2018 00:31:55.001] [default-akka.actor.default-dispatcher-2] [akka://default/user/IO-HTTP/listener-0] Binding to localhost/127.0.0.1:8080
simpleServer [DEBUG] [11/05/2018 00:31:55.059] [default-akka.actor.default-dispatcher-4] [akka://default/system/IO-TCP/selectors/$a/0] Successfully bound to /127.0.0.1:8080
simpleServer [INFO] [11/05/2018 00:31:55.062] [default-akka.actor.default-dispatcher-4] [akka://default/user/IO-HTTP/listener-0] Bound to localhost/127.0.0.1:8080
simpleServer [INFO] [11/05/2018 00:31:55.065] [default-akka.actor.default-dispatcher-3] [akka://default/deadLetters] Message [akka.io.Tcp$Bound] from Actor[akka://default/user/IO-HTTP/listener-0#1396292109] to Actor[akka://default/deadLetters] was not delivered. [1] dead letters encountered. This logging can be turned off or adjusted with configuration settings 'akka.log-dead-letters' and 'akka.log-dead-letters-during-shutdown'.
re-stop
[info] Stopping application simpleServer (by killing the forked JVM) ...
simpleServer ... finished with exit code 143
[success] Total time: 1 s, completed Nov 5, 2018 12:32:13 AM
~~~~

Navigate to `127.0.0.1:8080` in your web browser. You should see something like this:

~~~~
Say hello to spray-can!
Defined resources:

/ping
/stream
/server-stats
/crash
/timeout
/timeout/timeout
/stop

Test file upload
[Choose Files] No file chosen
[Submit] Submit
~~~~

Remember to do `re-stop` or else the background application will not terminate.

You can run Git commands from the IntelliJ sbt shell because the `sbt-git` plugin is in the `project/plugins.sbt` file:

~~~~
// Documentation: https://johnreedlol.github.io/sbt-git/
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")
~~~~~