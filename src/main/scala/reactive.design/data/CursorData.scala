package reactive.design.data

import gui.Style

import scala.math._
import scalafx.scene.input.KeyCode.D
import reactive.design.config.TerminalSettings

final case class CursorDataPeek(x: Int, y: Int, visible: Boolean, style: Style)

class CursorData(maxWidth: Int = TerminalSettings.defaultMaxCharsInLine) {
  private var _style: Style = Style.default
  private var visible       = false

  private var _x = 0
  private var _y = 0

  private var savedX = 0
  private var savedY = 0

  def x: Int       = _x
  def y: Int       = _y
  def style: Style = _style

  def applySgr(sgr: Seq[Int]): Unit = {
    _style = _style.applySgr(sgr)
  }

  def toggleLatex(): Unit = {
    _style = _style.toggleLatex
  }

  def peek(): CursorDataPeek =
    CursorDataPeek(_x, _y, visible, _style)

  def savePosition(): Unit = {
    savedX = _x
    savedY = _y
  }

  def restorePosition(): Unit = {
    _x = savedX
    _y = savedY
  }

  def setVisibility(visible: Boolean): Unit =
    this.visible = visible

  def translateX(offsetX: Int) = {
    _x += 1
    if (_x >= maxWidth) {
      _x = 0
      _y += 1
    }
  }

  def translate(offsetX: Int, offsetY: Int): Unit = {
    _x = max(0, min(maxWidth, _x + offsetX))
    _y = max(0, _y + offsetY)
  }

  def setPosition(row: Int, col: Int): Unit = {
    _x = max(0, min(maxWidth, col - 1))
    _y = max(0, row - 1)
  }

  def setColumn(col: Int): Unit =
    _x = max(0, min(maxWidth, col - 1))
}
