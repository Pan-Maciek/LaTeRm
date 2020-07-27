package reactive.design.ui

import reactive.design.data.TerminalLine
import reactive.design.ui.drawable.Cursor
import scalafx.scene.Group

class Panel extends Group {
  val screen: Screen = new Screen()
  val curs: Cursor  = new Cursor()

  children.addAll(screen, curs)

  def update(lines: Iterable[TerminalLine], cursorCoordinates: (Double, Double)): Unit = {
    screen.redraw(lines)
    curs.redraw(cursorCoordinates._1, cursorCoordinates._2)
  }

}
