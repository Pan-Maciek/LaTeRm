package reactive.design.data

import gui.Style

import scala.math._
import scalafx.scene.input.KeyCode.D

final case class CursorEvent(x: Int, y: Int, visible: Boolean, style: Style)

class CursorData(maxWidth: Int, maxHeight: Int) extends Modifiable[CursorEvent] {

  private var style: Style = Style.default
  private var modified     = true
  private var visible      = false

  private var x = 0
  private var y = 0

  private var savedX = 0
  private var savedY = 0

  override def peek(): CursorEvent = {
    modified = false
    CursorEvent(x, y, visible, style)
  }

  override def isModified(): Boolean = modified

  def savePosition(): Unit = {
    savedX = x
    savedY = y
  }

  def restorePosition(): Unit = {
    x = savedX
    y = savedY

    modified = true
  }

  def setVisibility(visible: Boolean): Unit = {
    this.visible = visible

    modified = true
  }

  def translate(offsetX: Int, offsetY: Int): Unit = {
    x = max(0, min(maxWidth, x + offsetX))
    y = max(0, min(maxHeight, y + offsetY))

    modified = true
  }

  def setPosition(row: Int, col: Int): Unit = {
    x = max(0, min(maxWidth, col - 1))
    y = max(0, min(row - 1, maxHeight))

    modified = true
  }

  def setColumn(col: Int): Unit = {
    x = max(0, min(maxWidth, col - 1))

    modified = true
  }
}
