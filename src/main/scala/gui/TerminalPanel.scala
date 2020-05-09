package gui

import scalafx.scene.canvas.Canvas
import term.Terminal

import scala.io.Source
import config.UiConfig
import scalafx.scene.paint.Color
import javafx.beans.InvalidationListener
import javafx.beans.value.ObservableValue
import scalafx.application.Platform

class TerminalPanel(setTitle: String => Unit) extends Canvas {
  width = UiConfig.width
  height = UiConfig.height

  private val onResizeListener = new InvalidationListener() {
    def invalidated(observable: javafx.beans.Observable): Unit = {
      // It's place for logic when resizing.
      graphicsContext2D.fill = Color.Black
      graphicsContext2D.fillRect(0, 0, width.get, height.get)
    }
  }
  width.addListener(onResizeListener)
  height.addListener(onResizeListener)

  graphicsContext2D.fill = Color.Black
  graphicsContext2D.fillRect(0, 0, width.get, height.get)

  def start(): Unit = {
    val terminal = new Terminal
    onKeyTyped = e => terminal.stdin.write(e.getCharacter.codePointAt(0))
    new Thread(() => {
      sealed trait State
      case object Default extends State
      case object ESC extends State
      case object CSI extends State
      case object SYS extends State

      var state: State = Default
      var style = Style.default
      var x = 10
      var y = 10

      var parameter = ""
      var intermediate = ""
      for (c <- Source.fromInputStream(terminal.stdout)) {
        state = (state, c) match {
          case (_, '\u001b') => ESC
          case (ESC, '[') => parameter = ""; intermediate = ""; CSI
          case (ESC, ']') => parameter = ""; SYS
          case (SYS, '\u0007') => val tmp  = parameter; Platform.runLater(() => setTitle(tmp)); Default
          case (SYS, '\u0000') => println("sys", parameter); Default
          case (SYS, c) => parameter += c; SYS
          case (CSI, c) if 0x30 <= c && c <= 0x3F => parameter += c; CSI
          case (CSI, c) if 0x20 <= c && c <= 0x2F => intermediate += ""; CSI
          case (CSI, 'm') =>
            style = style.applySRG(parameter)
            graphicsContext2D.fill =  style.foreground
            Default
          case (Default, '\n') =>
            y += 10
            x = 0
            Default
          case _ =>
            graphicsContext2D.fillText(c.toString, x, y)
            x += 10
            Default
        }
      }
    }).start()
  }

  def bind(
      widthProp: ObservableValue[Number],
      heightProp: ObservableValue[Number]
  ): Unit = {
    width.bind(widthProp)
    height.bind(heightProp)
  }

}
