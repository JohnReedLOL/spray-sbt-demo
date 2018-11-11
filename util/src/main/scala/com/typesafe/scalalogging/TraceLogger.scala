package com.typesafe.scalalogging

// Note: This isn't actually part of com.typesafe.scalalogging, it just uses the same package name so that you can use it
// as a drop-in replacement for com.typesafe.scalalogging.StrictLogging

import sourcecode.{ Enclosing, File, Line }

/**
 * This wraps a value (in this case a logger) and adds additional functionality (in this case debug tracing).
 * Note that I did not use inheritance because Loggers are final and I did not use an implicit adapter because that
 * would require additional code changes. By doing it this way, I can just replace "extends StrictLogging" with "extends TraceLogging".
 */
final case class TraceLogger(private val wrappedLogger: Logger, private val tracingDisabled: Boolean) {
  // These methods shadow the regular logging methods: trace, debug, info, warn, and error.
  // Implicit parameters (in this case the line number and file name) are provided at compile time.
  // Note that the the lineNumber, fileName, and packagePath are formatted to look like a stack trace, but no stack trace is generated at runtime.

  def trace(message: String)(implicit line: Line, enclosingPath: Enclosing, filePath: File): Unit = {
    val trace: String = if (tracingDisabled) {
      "" // return empty String
    } else {
      val fileName: String    = getFileName(filePath.value)
      val lineNumber: Int     = line.value
      val packagePath: String = enclosingPath.value
      s" - $packagePath($fileName:$lineNumber)" // s"..." means string interpolation
    }
    wrappedLogger.trace(message + trace)
  }

  def debug(message: String)(implicit line: Line, enclosingPath: Enclosing, filePath: File): Unit = {
    val trace: String = if (tracingDisabled) {
      ""
    } else {
      val fileName: String    = getFileName(filePath.value)
      val lineNumber: Int     = line.value
      val packagePath: String = enclosingPath.value
      s" - $packagePath($fileName:$lineNumber)"
    }
    wrappedLogger.debug(message + trace)
  }

  def info(message: String)(implicit line: Line, enclosingPath: Enclosing, filePath: File): Unit = {
    val trace: String = if (tracingDisabled) {
      ""
    } else {
      val fileName: String    = getFileName(filePath.value)
      val lineNumber: Int     = line.value
      val packagePath: String = enclosingPath.value
      s" - $packagePath($fileName:$lineNumber)"
    }
    wrappedLogger.info(message + trace)
  }

  def warn(message: String)(implicit line: Line, enclosingPath: Enclosing, filePath: File): Unit = {
    val trace: String = if (tracingDisabled) {
      ""
    } else {
      val fileName: String    = getFileName(filePath.value)
      val lineNumber: Int     = line.value
      val packagePath: String = enclosingPath.value
      s" - $packagePath($fileName:$lineNumber)"
    }
    wrappedLogger.warn(message + trace)
  }

  def error(message: String)(implicit line: Line, enclosingPath: Enclosing, filePath: File): Unit = {
    val trace: String = if (tracingDisabled) {
      ""
    } else {
      val fileName: String    = getFileName(filePath.value)
      val lineNumber: Int     = line.value
      val packagePath: String = enclosingPath.value
      s" - $packagePath($fileName:$lineNumber)"
    }
    wrappedLogger.error(message + trace)
  }

  // These are the same as above, but they log a Throwable.
  // Note that they don't need a debug trace because logging a Throwable already provides the file name, line number, etc.

  def trace(message: String, cause: Throwable): Unit =
    wrappedLogger.trace(message, cause)

  def debug(message: String, cause: Throwable): Unit =
    wrappedLogger.debug(message, cause)

  def info(message: String, cause: Throwable): Unit =
    wrappedLogger.info(message, cause)

  def warn(message: String, cause: Throwable): Unit =
    wrappedLogger.warn(message, cause)

  def error(message: String, cause: Throwable): Unit =
    wrappedLogger.error(message, cause)

  /**
   * Gets a file name from a path.
   */
  private def getFileName(path: String): String =
    if (path.contains("/")) { // Mac or Linux filesystem
      path.split("/").last
    } else { // Windows filesystem
      path.split("\\\\").last
    }
}
