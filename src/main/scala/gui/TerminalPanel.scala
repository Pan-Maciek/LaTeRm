package gui

import scalafx.scene.canvas.Canvas
import term.Terminal
import config.UiConfig
import scalafx.application.Platform
import scalafx.scene.paint.Color
import scalafx.beans.property.StringProperty

class TerminalPanel() extends Canvas {
  width = UiConfig.width
  height = UiConfig.height

  val terminal = new Terminal
  def title: StringProperty = terminal.title

  def draw(): Unit = { // quick and dummy implementation
    synchronized {
      graphicsContext2D.fill = Color.Black
      graphicsContext2D.fillRect(0, 0, width.get, height.get)
      // TODO: implement lazy drawing
      graphicsContext2D.save()
      graphicsContext2D.translate(0, 30)
      for (line <- terminal.lines) {
        line.draw(graphicsContext2D)
        graphicsContext2D.translate(0, line.height)
      }
      graphicsContext2D.restore()
    }
  }
  terminal.onUpdate {
    _ => draw()
  }

  width.onChange {
    (_, _, _) => {
      // terminal.width = ???
      draw()
    }
  }
  height.onChange {
    (_, _, _) => {
      // terminal.height = ???
      draw()
    }
  }

  onKeyTyped = e => terminal.write(e.getCharacter.codePointAt(0))
}
