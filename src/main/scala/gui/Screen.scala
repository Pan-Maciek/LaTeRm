package gui

import java.util.{Timer, TimerTask}

import scalafx.scene.canvas.Canvas
import term.Terminal
import config.UiConfig
import scalafx.application.Platform
import scalafx.scene.paint.Color
import scalafx.beans.property.StringProperty
import gui.drawable.DrawableInstances._
import scalafx.geometry.VPos

class Screen() extends Canvas {
  implicit val gc = graphicsContext2D

  gc.font = UiConfig.font
  val terminal              = new Terminal
  def title: StringProperty = terminal.title

  private var _blink = false

  def drawBlank(): Unit = {
    gc.fill = Color.Black
    gc.fillRect(0, 0, width.get, height.get)
  }

  gc.textBaseline = VPos.Top
  def redraw(): Unit = {
    drawBlank()

    gc.save()
    // gc.translate(0, UiConfig.DefaultLineHeight)
    for (line <- terminal.lines) {
      line.draw
      gc.translate(0, line.height)
    }
    gc.restore()
  }

  def partialDraw(): Unit = {
    gc.save()
    // gc.translate(0, UiConfig.DefaultLineHeight)
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

  onKeyTyped = e => terminal.write(e.getCharacter.codePointAt(0))
}