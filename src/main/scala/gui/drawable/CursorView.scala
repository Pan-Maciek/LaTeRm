package gui.drawable

import scalafx.scene.shape.Rectangle
import term.Cursor
import scalafx.scene.paint.Color
import scalafx.beans.property.DoubleProperty
import config.UiConfig

class CursorView(curs: Cursor) extends Rectangle {
  // Todo implement blinking and
  var _blink   = false
  var _counter = 0
  width = 7.0
  height = UiConfig.DefaultLineHeight
  fill = Color.White

  def onUpdate(): Unit = {
    val (newX, newY, w, h) = curs.viewCoords
    relocate(newX, newY)
    width = w
    height = h

    if (_counter % 10 == 0) flip()
    _counter += 1
  }

  private def flip(): Unit = {
    _blink = !_blink
    fill_=(if (_blink) {
      Color.White
    } else {
      Color.Black
    })
  }

}
