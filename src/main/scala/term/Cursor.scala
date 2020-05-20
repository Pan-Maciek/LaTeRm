package term

import gui.Style
import scalafx.beans.property.IntegerProperty

import scala.math._

case class Cursor(width: IntegerProperty, height: IntegerProperty) {

  var style: Style = Style.default
  var x            = 0
  var savedX       = 0
  var y            = 0
  var savedY       = 0

  def savePosition() = {
    savedX = x
    savedY = y
  }

  def restorePosition() = {
    x = savedX
    y = savedY
  }

  def translate(offsetX: Int, offsetY: Int): Unit = {
    x = max(0, min(width.value, x + offsetX))
    y = max(0, min(height.value, y + offsetY))
  }

  def setPosition(col: Int, row: Int): Unit = {
    x = max(0, min(width.value, col - 1))
    y = max(0, min(height.value, row - 1))
  }

  def setColumn(col: Int): Unit =
    x = max(0, min(width.value, col - 1))
}
