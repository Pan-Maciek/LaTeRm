package gui.drawable

import scalafx.scene.shape.Rectangle
import term.Cursor
import scalafx.scene.paint.Color
import scalafx.beans.property.DoubleProperty
import config.UiConfig

class CursorView(cursor: Cursor) extends Rectangle {
  // Todo implement blinking and
  var _blink   = false
  var _counter = 0
  width_=(7.0)
  height_=(UiConfig.DefaultLineHeight)
  fill_=(Color.White)

  def onUpdate(): Unit = {
    x_=(cursor.x)
    y_=(cursor.y * UiConfig.DefaultLineHeight)
    flip()
  }

  private def flip(): Unit = {
    _blink = !_blink
    _counter += 1
    fill_=(if (_blink) {
      Color.BlueViolet
    } else {
      Color.Green
    })
  }

}
