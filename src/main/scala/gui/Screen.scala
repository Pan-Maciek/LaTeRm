package gui

import config.UiConfig
import gui.drawable.DrawableInstances._
import javafx.scene.input.KeyCode.{DOWN, LEFT, RIGHT, UP}
import scalafx.beans.property.StringProperty
import scalafx.geometry.VPos
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color
import term.Terminal

class Screen() extends Canvas {
  implicit val gc = graphicsContext2D

  gc.font = UiConfig.font
  val terminal              = new Terminal
  def title: StringProperty = terminal.title

  def drawBlank(): Unit = {
    gc.fill = Color.Black
    gc.fillRect(0, 0, width.get, height.get)
  }

  gc.textBaseline = VPos.Top
  def redraw(): Unit = {
    drawBlank()

    gc.save()
    for (line <- terminal.lines) {
      line.draw
      gc.translate(0, line.height)
    }
    gc.restore()
  }

  def partialDraw(): Unit = {
    gc.save()
    for ((line, changed) <- terminal.changedLines) {
      if (changed) {
        gc.fill = Color.Black
        gc.fillRect(0, 0, width.get, line.height)
        line.draw
      }
      gc.translate(0, line.height)
    }
    gc.restore()
  }

  width.onChange { (_, _, _) => { redraw() } }
  height.onChange { (_, _, _) => { redraw() } }

  onKeyTyped = e => terminal.write(e.getCharacter.getBytes)
  onKeyPressed =
    _.getCode match {
      case UP     => terminal.write("\u001b[A".getBytes)
      case DOWN   => terminal.write("\u001b[B".getBytes)
      case RIGHT  => terminal.write("\u001b[C".getBytes)
      case LEFT   => terminal.write("\u001b[D".getBytes)
      case _ =>
    }
}
