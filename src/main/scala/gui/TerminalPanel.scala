package gui

import scalafx.scene.canvas.Canvas
import term.Terminal

import scala.io.Source
import config.UiConfig
import gui.data.{TerminalLine}
import scalafx.scene.paint.Color
import javafx.beans.InvalidationListener
import javafx.beans.value.ObservableValue
import scalafx.application.Platform

class TerminalPanel(setTitle: String => Unit) extends Canvas {
  width = UiConfig.width
  height = UiConfig.height

  private var lines: List[TerminalLine] = List(TerminalLine())
  val terminal                          = new Terminal

  def draw(): Unit = { // quick and dummy implementation
    // TODO: implement lazy drawing
    graphicsContext2D.save()
    for (line <- lines) {
      drawBackground()
      line.draw(graphicsContext2D)
      graphicsContext2D.translate(0, line.height)
    }
    graphicsContext2D.restore()
  }

  private val onResizeListener = new InvalidationListener() {
    def invalidated(observable: javafx.beans.Observable): Unit = {
      // TODO: resize calculate new dimensions for terminal; resize terminal and fix screen
      // draw()
    }
  }
  width.addListener(onResizeListener)
  height.addListener(onResizeListener)

  graphicsContext2D.fill = Color.Black
  graphicsContext2D.fillRect(0, 0, width.get, height.get)

  def start(): Unit = {
    onKeyTyped = e => terminal.stdin.write(e.getCharacter.codePointAt(0))
    new Thread(() => {
      sealed trait State
      case object Default extends State
      case object ESC     extends State
      case object CSI     extends State
      case object SYS     extends State

      var state: State = Default
      var style        = Style.default
      var x            = 0
      var y            = 0

      var parameter    = ""
      var intermediate = ""
      for (c <- Source.fromInputStream(terminal.stdout)) {
        state = (state, c) match {
          case (_, '\u001b') => ESC
          case (ESC, '[')    => parameter = ""; intermediate = ""; CSI
          case (ESC, ']')    => parameter = ""; SYS
          case (SYS, '\u0007') =>
            val tmp = parameter; Platform.runLater(() => setTitle(tmp)); Default
          case (SYS, '\u0000')                    => println("sys", parameter); Default
          case (SYS, c)                           => parameter += c; SYS
          case (CSI, c) if 0x30 <= c && c <= 0x3F => parameter += c; CSI
          case (CSI, c) if 0x20 <= c && c <= 0x2F => intermediate += ""; CSI
          case (Default, '\u0007') =>
            print("Bell!")
            Default
          case (Default, '\b') =>
            x -= 1
            Default
          case (CSI, 'm') =>
            style = style.applySRG(parameter)
            Default
          case (CSI, 'K') =>
            // delete in line
            lines(y).delete(x)
            Default
          case (Default, '\n') =>
            x = 0
            y += 1
            lines = lines :+ TerminalLine()
            Default
          case _ =>
            lines(y).write(x, c, style)
            x += 1
            Default
        }
        draw() // TODO: update line
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

  private def drawBackground(): Unit = {
    graphicsContext2D.fill = Color.Black
    graphicsContext2D.fillRect(0, 0, width.get, height.get)
  }
}
