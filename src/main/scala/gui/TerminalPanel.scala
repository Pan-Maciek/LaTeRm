package gui

import scalafx.scene.canvas.Canvas
import term.Terminal

import scala.io.Source
import scala.reflect.api.Constants
import config.UiConfig
import scalafx.scene.paint.Color
import scalafx.beans.Observable
import javafx.beans.InvalidationListener
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.application.Platform

class TerminalPanel extends Canvas {
  val term = new Terminal
  var text = ""
  width = UiConfig.width
  height = UiConfig.height

  private val onResizeListener = new InvalidationListener() {
    def invalidated(observable: javafx.beans.Observable): Unit = {
      // It's a place for logic when resizing.
      graphicsContext2D.fill = Color.Black
      graphicsContext2D.fillRect(0, 0, width.get, height.get)

      graphicsContext2D.fill = Color.White
      graphicsContext2D.fillText(text, 20, 60)
    }
  }
  width.addListener(onResizeListener)
  height.addListener(onResizeListener)

  onKeyTyped = e => term.stdin.write(e.getCharacter.codePointAt(0))

  graphicsContext2D.fill = Color.Black
  graphicsContext2D.fillRect(0, 0, width.get, height.get)

  new Thread(() => {
    for (c <- Source.fromInputStream(term.stdout)) {
      text += c
      Platform.runLater(() => {
        graphicsContext2D.fill = Color.White
        graphicsContext2D.fillText(text, 20, 60)
      })
    }
  }).start()

  def bind(
      widthProp: ObservableValue[Number],
      heightProp: ObservableValue[Number]
  ): Unit = {
    width.bind(widthProp)
    height.bind(heightProp)
  }

}
