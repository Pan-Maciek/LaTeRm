package gui.drawable

import config.UiConfig
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import term.Cursor

class CursorView(curs: Cursor) extends Rectangle {
  var _counter = 0
  width = 8
  height = UiConfig.DefaultLineHeight
  fill = Color.White

  def onUpdate(): Unit = {
    val (newX, newY, _, _) = curs.viewCoords
    if (_counter % 2 == 0)
      relocate(newX, newY)

    if (_counter % 10 == 0) flip()
    _counter += 1
  }

  private def flip(): Unit =
    visible = !visible.value

}
