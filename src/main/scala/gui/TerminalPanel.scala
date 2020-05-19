package gui

import scalafx.scene.canvas.Canvas
import term.Terminal
import config.UiConfig
import scalafx.application.Platform
import scalafx.scene.paint.Color
import scalafx.beans.property.StringProperty

import gui.drawable.DrawableInstances._

class TerminalPanel() extends Canvas {
  implicit val gc = graphicsContext2D

  val terminal              = new Terminal
  def title: StringProperty = terminal.title

  def drawBlank(): Unit = {
    synchronized {
      gc.fill = Color.Black
      gc.fillRect(0, 0, width.get, height.get)
    }
  }

  private def redraw(): Unit = {
    drawBlank()

    gc.save()
    gc.translate(0, 30)
    for (line <- terminal.lines) {
      line.draw
      gc.translate(0, line.height)
    }
    gc.restore()
  }

  private def partialDraw(): Unit = {
    gc.save()
    gc.translate(0, 30)
    var runningHeight = 30.0
    for ((line, changed) <- terminal.changedLines) {
      if (changed) {
        // FIXME For some reason it does not clear the line!
        // bcs of this line is drawn multiple times and it looks as if it were bold
        gc.fill = Color.Black
        gc.fillRect(0, runningHeight, width.get, line.height)
        // ------------------------------------------------------
        line.draw

      }
      runningHeight += line.height
      gc.translate(0, line.height)
    }
    gc.restore()
  }

  def draw(): Unit = { // quick and dummy implementation
    synchronized {
      if (terminal.modified) {
        redraw()
      } else {
        partialDraw()
      }
    }
  }

  terminal.onUpdate { _ => draw() }

  width.onChange { (_, _, _) =>
    {
      // terminal.width = ???
      draw()
    }
  }
  height.onChange { (_, _, _) =>
    {
      // terminal.height = ???
      draw()
    }
  }

  onKeyTyped = e => terminal.write(e.getCharacter.codePointAt(0))
}
