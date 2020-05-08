package gui

import scalafx.scene.canvas.Canvas
import term.Terminal

import scala.io.Source

class TerminalPanel extends Canvas {
  width = 200
  height= 200

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