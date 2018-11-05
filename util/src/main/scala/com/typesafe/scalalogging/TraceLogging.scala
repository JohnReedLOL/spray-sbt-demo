package com.typesafe.scalalogging

// Note: This isn't actually part of com.typesafe.scalalogging, it just uses the same package name so that you can use TraceLogger
// as a drop-in replacement for com.typesafe.scalalogging.StrictLogging

/**
  * This provides a logger with additional debug tracing functionality.
  * You can turn this functionality off by setting the environment variable DISABLE_SOURCE_CODE_TRACING to true,
  * or you can go back to using "extends StrictLogging".
  */
trait TraceLogging {

  /**
    * true if debug tracing is disabled.
    */
  val tracingDisabled: Boolean = {
    val propertyName = "DISABLE_SOURCE_CODE_TRACING"
    val environmentVariable: String = System.getenv(propertyName)
    val systemProperty: String = System.getProperty(propertyName)
    if ((environmentVariable != null && environmentVariable.trim.equalsIgnoreCase("true")) ||
      (systemProperty != null && systemProperty.trim.equalsIgnoreCase("true"))) {
      true
    } else {
      false
    }
  }

  /**
    * This is the logger that we are providing to the user.
    * We do not not need to provide the class file name because the debug trace has that info.
    */
  val logger: TraceLogger = TraceLogger(com.typesafe.scalalogging.Logger(org.slf4j.LoggerFactory.getLogger("")), tracingDisabled)
  // No need for: Logger(LoggerFactory.getLogger(getClass.getName))
}
