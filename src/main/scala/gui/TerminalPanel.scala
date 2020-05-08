package gui

import scalafx.scene.canvas.Canvas
import term.Terminal

import scala.io.Source
import scala.reflect.api.Constants
import config.UiConfig

class TerminalPanel extends Canvas {
  width = UiConfig.width
  height = UiConfig.height

  val term = new Terminal
  onKeyTyped = e => term.stdin.write(e.getCharacter.codePointAt(0))

  new Thread(() => {
    var text = ""
    for (c <- Source.fromInputStream(term.stdout)) {
      text += c
      graphicsContext2D.fillText(text, 20, 60)
    }
  }).start()

}
