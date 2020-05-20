package gui.data

import scalafx.beans.property.IntegerProperty
import term.Cursor

import scala.collection.mutable.ArrayBuffer
import gui.drawable.DrawableInstances._
import config.UiConfig

/**
  * Note that function write should be rewritten so that it handles only standard characters write
  * We have to synchronize on  _mask and _lines
  */
case class LinesBuffer(width: IntegerProperty, height: IntegerProperty) {

  def eraseInLine(n: Int): Unit = {
    n match {
      // TODO
      case 0 => ??? // replace everything from cursor to end of the line with whitespace, does not change cursor position
      case 1 => ??? // replace everything from cursor to start of the line with whitespace, does not change cursor position
      case 2 => ??? // replace whole line with whitespace, dose not change cursor position
      case _ =>     // ignore invalid argument
    }
  }

  def eraseInDisplay(n: Int): Unit =
    n match {
      // TODO
      case 0 => ??? // replace everything from cursor to the end of the screen with whitespace, dose not change cursor position
      case 1 => ??? // replace everything from cursor to the start of the screen, does not change cursor position
      case 2 => ??? // replace whole screen with whitespace, on DOS based systems move cursor to the most top left
      case 3 => ??? // replace whole screen with whitespace, remove all lines saved in scroll-back buffer
      case _ =>     // ignore invalid argument
    }


  val cursor: Cursor             = Cursor(width, height)
  var myBufferMaxLinesCount: Int = LinesBuffer.DEFAULT_MAX_LINES_COUNT

  private val _mask     = new ArrayBuffer[Boolean]() += true
  private val _lines    = ArrayBuffer(TerminalLine())
  private var _modified = true

  def linesCount: Int   = _lines.length
  def modified: Boolean = _modified

  def write(char: Char): Unit = {
    synchronized {
      while (cursor.y >= _lines.size) {
        _lines += (TerminalLine())
        _mask += true
        _modified = true
      }
      _mask(cursor.y) = true
      _lines(cursor.y).write(cursor.x, char, cursor.style)
      cursor.x += 1
      if (cursor.x == width.value) {
        cursor.x = 0
        cursor.y += 1
      }
    }
  }

  /**
    * Removes all lines but one
    */
  def clearLines(): Unit = {
    synchronized {
      val toDrop = linesCount - 2
      _mask.dropRightInPlace(toDrop)
      _lines.dropRightInPlace(toDrop)
      _modified = true
    }
  }

  def clearLinesAboveCursor(): Unit = {
    synchronized {
      val y = cursor.y
      _lines.dropInPlace(y)
      _mask.dropInPlace(y)
      _modified = true
    }
  }

  def clearLinesBelowCursor(): Unit = {
    synchronized {
      val toDrop = linesCount - cursor.y
      _lines.dropRightInPlace(toDrop)
      _mask.dropRightInPlace(toDrop)
      _modified = true
    }
  }

  /**
    * Clears line from cursor (inclusive) to right
    */
  def clearToRight(): Unit = {
    synchronized {
      val i    = cursor.y
      val line = _lines(i)
      line.deleteFrom(cursor.x)
      _mask(i) = true
    }
  }

  /**
    * Clears start of the line up to the cursor (exclusive)
    */
  def clearToLeft(): Unit = {
    synchronized {
      val i    = cursor.y
      val line = _lines(i)
      line.deleteTo(cursor.x)
      _mask(i) = true
    }
  }

  /** Returns list of last lines fitting the screen
    *  with boolean indicating whether this particular line has been modified.
    * */
  def lastLinesChanged: Seq[(TerminalLine, Boolean)] = {
    synchronized {
      val (from, until) = lastLinesIndices
      val slicedMask    = _mask.slice(from, until)
      val slicedLines   = _lines.slice(from, until).toSeq

      for (i <- Range(from, until)) {
        _mask(i) = false
      }

      slicedLines.zip(slicedMask)
    }
  }

  /**  Returns list of last lines fitting the screen */
  def lastLines: Seq[TerminalLine] = {
    synchronized {
      val (from, until) = lastLinesIndices
      for (i <- Range(from, until)) {
        _mask(i) = false
      }

      _modified = false
      _lines.slice(from, until).toSeq
    }
  }

  /**  Returns indicies of last lines fitting the screen,
    *  Note that it should be synchronized by calling method!
    */
  private def lastLinesIndices: (Int, Int) = {
    var i             = linesCount - 1
    var runningHeight = 0.0
    val maxH          = UiConfig.height - 25

    while (i >= 0 && runningHeight < maxH) {
      runningHeight += _lines(i).height
      i -= 1
    }

    if (i < 0) {
      i += 1
    }

    if (runningHeight > maxH) {
      runningHeight -= _lines(i).height
      i += 1
    }

    (i, linesCount)
  }
}

object LinesBuffer {
  val DEFAULT_MAX_LINES_COUNT = 5000
}
