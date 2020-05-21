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
  width_=(7.0)
  height_=(UiConfig.DefaultLineHeight)
  fill_=(Color.White)

  def onUpdate(): Unit = {
    val (newX, newY, w, h) = curs.viewCoords
    translateX_=(newX)
    translateY_=(newY)
    width_=(w)
    height_=(h)

    flip()
  }

  private def flip(): Unit = {
    _blink = !_blink
    _counter += 1
    fill_=(if (_blink) {
      Color.White
    } else {
      Color.Black
    })
  }

}
