package reactive.design.data

import scalafx.beans.property.IntegerProperty
import term.Cursor

import scala.collection.mutable.ArrayBuffer
import gui.drawable.DrawableInstances._
import config.{SystemConstants, UiConfig, Windows}

/**
  * Note that function write should be rewritten so that it handles only standard characters write
  * We have to synchronize on  _mask and _lines
  */
class LinesBuffer(cursor: CursorData) {
  private val _lines = ArrayBuffer(TerminalLine())

  // Cursor's x, y, width, height
  // def cursorCoords: (Double, Double, Double, Double) = {
  //   synchronized {
  //     // hot fix
  //     appendBlankLines()

  //     val cursorLine = _lines(cursor.y)
  //     var cursorY    = 0.0
  //     var (i, _)     = lastLinesIndices

  //     while (i < cursor.y) {
  //       cursorY += _lines(i).height
  //       i += 1
  //     }
  //     val cursorX = cursorLine.widthTo(cursor.x)
  //     (cursorX, cursorY, 10.0, 15.0)
  //   }
  // }

  def eraseInLine(n: Int): Unit = {
    appendBlankLines()
    n match {
      // TODO
      case 0 =>
        eraseFromCursorToLineEnd() // replace everything from cursor to end of the line with whitespace, does not change cursor position
      case 1 =>
        eraseFromLineStartToCursor() // replace everything from cursor to start of the line with whitespace, does not change cursor position
      case 2 =>
        eraseWholeLine() // replace whole line with whitespace, dose not change cursor position
      case _ => // ignore invalid argument
    }
  }

  private def eraseFromCursorToLineEnd(): Unit = {
    val line = _lines(cursor.y)
    for (i <- Range(cursor.x, line.len)) {
      line.write(i, ' ', cursor.style)
    }
  }

  private def eraseFromLineStartToCursor(): Unit = {
    val line = _lines(cursor.y)
    for (i <- Range(0, cursor.x).inclusive) {
      line.write(i, ' ', cursor.style)
    }
  }

  private def eraseWholeLine(): Unit = {
    val line = _lines(cursor.y)
    for (i <- Range(0, line.len)) {
      line.write(i, ' ', cursor.style)
    }
  }

  def eraseInDisplay(n: Int): Unit = {
    appendBlankLines()
    n match {
      case 0 =>
        eraseFromCursorToEnd() // replace everything from cursor to the end of the screen with whitespace, dose not change cursor position
      case 1 =>
        eraseFromTopToCursor() // replace everything from cursor to the start of the screen, does not change cursor position
      case 2 =>
        eraseAllAndSetCursor() // replace whole screen with whitespace, on DOS based systems move cursor to the most top left
      case 3 =>
        eraseAll() // replace whole screen with whitespace, remove all lines saved in scroll-back buffer
      case _ => // ignore invalid argument
    }
  }

  private def eraseFromCursorToEnd(): Unit = {
    eraseFromCursorToLineEnd()
    for (i <- Range(cursor.y + 1, linesCount)) {
      eraseLine(i)
    }
  }

  private def eraseFromTopToCursor(): Unit = {
    for (i <- Range(0, cursor.y)) {
      eraseLine(i)
    }
    eraseFromLineStartToCursor()
  }

  def eraseAllAndSetCursor(): Unit = {
    eraseAll()
    // if (SystemConstants.osName == Windows) {
    //   val (top, last) = lastLinesIndices
    //   cursor.setPosition(top + 1, 1)
    // }
  }

  private def eraseAll(): Unit = {
    for (i <- Range(0, linesCount)) {
      eraseLine(i)
    }
  }

  private def eraseLine(lineNo: Int): Unit = {
    for (i <- Range(0, _lines(lineNo).len)) {
      _lines(lineNo).write(i, ' ', cursor.style)
    }
  }

  private def appendBlankLines(): Unit = {
    while (cursor.y >= _lines.size) {
      _lines += (TerminalLine())
    }
  }

  def linesCount: Int = _lines.length

  def write(char: Char): Unit = {
    appendBlankLines()
    _lines(cursor.y).write(cursor.x, char, cursor.style)
    cursor.translateX(1)
  }

  /**
    * Removes all lines but one
    */
  def clearLines(): Unit = {
    val toDrop = linesCount - 2
    _lines.dropRightInPlace(toDrop)
  }

  def clearLinesAboveCursor(): Unit = {
    val y = cursor.y
    _lines.dropInPlace(y)
  }

  def clearLinesBelowCursor(): Unit = {
    val toDrop = linesCount - cursor.y
    _lines.dropRightInPlace(toDrop)
  }

  /**
    * Clears line from cursor (inclusive) to right
    */
  def clearToRight(): Unit = {
    val i    = cursor.y
    val line = _lines(i)
    line.deleteFrom(cursor.x)
  }

  /**
    * Clears start of the line up to the cursor (exclusive)
    */
  def clearToLeft(): Unit = {
    val i    = cursor.y
    val line = _lines(i)
    line.deleteTo(cursor.x)
  }

  /**  Returns list of last lines fitting the screen */
  // def lastLines: Seq[TerminalLine] = {
  //   val (from, until) = lastLinesIndices
  //   _lines.slice(from, until).toSeq
  // }

  /**  Returns indicies of last lines fitting the screen,
    *  Note that it should be synchronized by calling method!
    */
  // private def lastLinesIndices: (Int, Int) = {
  //   var i             = linesCount - 1
  //   var runningHeight = 0.0
  //   val maxH          = UiConfig.height - 25

  //   while (i >= 0 && runningHeight < maxH) {
  //     runningHeight += _lines(i).height
  //     i -= 1
  //   }

  //   if (i < 0) {
  //     i += 1
  //   }

  //   if (runningHeight > maxH) {
  //     runningHeight -= _lines(i).height
  //     i += 1
  //   }

  //   (i, linesCount)
  // }
}

object LinesBuffer {
  val DEFAULT_MAX_LINES_COUNT = 5000
}
