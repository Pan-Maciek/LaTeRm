package gui

import scalafx.scene.canvas.Canvas
import term.Terminal
import config.UiConfig
import scalafx.application.Platform
import scalafx.scene.paint.Color
import scalafx.beans.property.StringProperty

import gui.drawable.DrawableInstances._

class TerminalPanel() extends Canvas {
  width = UiConfig.width
  height = UiConfig.height

  implicit val gc = graphicsContext2D

  val terminal              = new Terminal
  def title: StringProperty = terminal.title

  def draw(): Unit = { // quick and dummy implementation
    synchronized {
      gc.fill = Color.Black
      gc.fillRect(0, 0, width.get, height.get)
      // TODO: implement lazy drawing
      gc.save()
      gc.translate(0, 30)
      for (line <- terminal.lines) {
        line.draw
        gc.translate(0, line.height)
      }
      gc.restore()
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
