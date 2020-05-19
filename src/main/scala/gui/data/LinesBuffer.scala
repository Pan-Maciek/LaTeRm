package gui.data

import scalafx.beans.property.IntegerProperty
import term.Cursor

import scala.collection.mutable.ArrayBuffer
import gui.drawable.DrawableInstances._

/**
  * Note that function write should be rewritten so that it handles only standard characters write
  * We have to synchronize on  _mask and _lines
  */
case class LinesBuffer(val width: IntegerProperty, val height: IntegerProperty) {
  val cursor: Cursor             = Cursor(width, height)
  var myBufferMaxLinesCount: Int = LinesBuffer.DEFAULT_MAX_LINES_COUNT

  private val _mask  = new ArrayBuffer[Boolean]() += true
  private val _lines = ArrayBuffer(TerminalLine())

  def linesCount: Int = _lines.length

  def write(char: Char): Unit = {
    char match {
      case '\n' => cursor.y += 1
      case '\b' => cursor.x -= 1
      case '\r' => cursor.x = 0
      case _ => {
        while (cursor.y >= _lines.size) {
          synchronized {
            _lines += (TerminalLine())
            _mask += true
          }
        }
        synchronized { _mask(cursor.y) = true }
        _lines(cursor.y).write(cursor.x, char, cursor.style)
        cursor.x += 1
        if (cursor.x == width.value) {
          cursor.x = 0
          cursor.y += 1
        }
      }
    }
  }

  /** Returns list of lines with boolean indicating whether it has been modified,
    *  this one shouldn't be used as it returns all lines!
    * */
  def lines: Seq[(TerminalLine, Boolean)] = {
    synchronized {
      val seq = _lines.toSeq.zip(_mask)
      _mask.mapInPlace(bool => false)
      seq
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

      _lines.slice(from, until).toSeq
    }
  }

  /**  Returns indicies of last lines fitting the screen,
    *  Note that it should be synchronized by calling method!
    */
  private def lastLinesIndices: (Int, Int) = {
    var i             = linesCount - 1
    var runningHeight = 0.0
    val maxH          = height.get

    while (i >= 0 && runningHeight < maxH) {
      runningHeight += _lines(i).height
      i -= 1
    }

    while (runningHeight > maxH) {
      runningHeight -= _lines(i).height
      i += 1
    }

    (i, linesCount)
  }
}

object LinesBuffer {
  val DEFAULT_MAX_LINES_COUNT = 5000
}
