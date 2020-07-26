package reactive.design.ui

import config.UiConfig
import scalafx.beans.property.StringProperty
import scalafx.geometry.VPos
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color

import reactive.design.data.TerminalLine
import reactive.design.ui.drawable.DrawableInstances._
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global

class Screen() extends Canvas {
  implicit val gc = graphicsContext2D
  gc.font = UiConfig.font
  gc.textBaseline = VPos.Top
  requestFocus()

  def redraw(lines: Iterable[TerminalLine]): Unit = {
    drawBlank()
    gc.save()
    for (line <- lines) {
      line.draw
      gc.translate(0, line.height)
    }
    gc.restore()
  }

  def drawBlank(): Unit = {
    gc.fill = Color.Black
    gc.fillRect(0, 0, width.get, height.get)
  }

}
