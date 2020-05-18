package term

import gui.Style
import scalafx.beans.property.IntegerProperty

import scala.math._

case class Cursor(val width: IntegerProperty, val height: IntegerProperty) {
  var style: Style = Style.default
  var x            = 0
  var y            = 0

  def translate(offsetX: Int, offsetY: Int): Unit = {
    x = max(0, min(width.value, offsetX))
    y = max(0, min(height.value, offsetY))
  }
}
