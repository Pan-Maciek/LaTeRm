package reactive.design.ui

import config.UiConfig
import javafx.scene.input.KeyCode.{DOWN, LEFT, RIGHT, UP}
import scalafx.beans.property.StringProperty
import scalafx.geometry.VPos
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color
import term.Terminal

import reactive.design.data.TerminalLine
import reactive.design.ui.drawable.DrawableInstances._

class Screen(writeCallback: Array[Byte] => Unit) extends Canvas {
  implicit val gc = graphicsContext2D
  gc.font = UiConfig.font
  gc.textBaseline = VPos.Top

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

  onKeyTyped = e => writeCallback(e.getCharacter.getBytes)
  onKeyPressed = _.getCode match {
    case UP    => writeCallback("\u001b[A".getBytes)
    case DOWN  => writeCallback("\u001b[B".getBytes)
    case RIGHT => writeCallback("\u001b[C".getBytes)
    case LEFT  => writeCallback("\u001b[D".getBytes)
    case _     =>
  }
}
