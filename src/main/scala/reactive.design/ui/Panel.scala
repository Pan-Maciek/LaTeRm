package reactive.design.ui

import scalafx.scene.layout.StackPane
import java.{util => ju}
import config.UiConfig
import scalafx.scene.layout.Pane
import scalafx.scene.Group
import reactive.design.ui.drawable.Cursor
import reactive.design.ui.Screen
import reactive.design.data.TerminalLine

class Panel() extends Group {
  val screen: Screen = new Screen()
  val curso: Cursor  = new Cursor()

  children.addAll(screen, curso)

  def update(lines: Iterable[TerminalLine], cursorCoords: (Double, Double)) = {
    screen.redraw(lines)
    curso.onUpdate(cursorCoords._1, cursorCoords._2)
  }

}
