package reactive.design.ui.drawable

import reactive.design.config.UIConfig
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle

class Cursor extends Rectangle {
  width = 7.0
  height = UIConfig.defaultLineHeight
  fill = Color.White

  var _blink   = false
  var _counter = 0

  def redraw(newX: Double, newY: Double): Unit = {
    relocate(newX, newY)
    if (_counter % 10 == 0) blink()
    _counter += 1
  }

  private def blink(): Unit = {
    _blink = !_blink
    fill_=(if (_blink) {
      Color.White
    } else {
      Color.Black
    })
  }

}
