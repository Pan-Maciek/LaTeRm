package gui.data

import scalafx.beans.property.IntegerProperty
import term.Cursor

import scala.collection.mutable.ArrayBuffer

case class LinesBuffer(val width: IntegerProperty, val height: IntegerProperty) {
  val cursor: Cursor = Cursor(width, height)
  var myBufferMaxLinesCount: Int = LinesBuffer.DEFAULT_MAX_LINES_COUNT

  private val _lines = ArrayBuffer(TerminalLine())

  def write(char: Char): Unit = {
    while (cursor.y >= _lines.size)
      _lines.append(TerminalLine())
    _lines(cursor.y).write(cursor.x, char, cursor.style)
    cursor.x += 1
    if (cursor.x == width.value) {
      cursor.x = 0
      cursor.y += 1
    }
  }

  def lines: Seq[TerminalLine] = _lines.toSeq
}

object LinesBuffer {
  val DEFAULT_MAX_LINES_COUNT = 5000
}
