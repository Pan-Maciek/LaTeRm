package term

import java.io.InputStream

import gui.data.LinesBuffer
import scalafx.application.Platform.runLater

import monix.reactive.Observable
import monix.eval.Task

import reactive.design.parser.Action
import reactive.design.parser.SetTitle
import reactive.design.parser.Write
import reactive.design.parser.SetStyle
import reactive.design.parser.MoveCursor
import reactive.design.parser.SetColumn
import reactive.design.parser.SetCursor
import reactive.design.parser.ClearDisplay
import reactive.design.parser.ClearLine
import reactive.design.parser.Bell
import reactive.design.parser.Ignore
import reactive.design.parser.SaveCursorPosition
import reactive.design.parser.RestoreCursorPosition
import reactive.design.parser.SetCursorVisibility
import reactive.design.parser.Warn
import reactive.design.parser.ToggleLatex

object StdoutDriver {
  def apply(
      terminal: Terminal,
      linesBuffer: LinesBuffer,
      observable: Observable[Action]
  ): Task[Unit] = {
    observable.mapEval { action =>
      Task.evalAsync {
        linesBuffer.synchronized {
          action match {
            case Write(char)     => linesBuffer.write(char)
            case SetTitle(title) => runLater(() => terminal.title.set(title))
            case SetStyle(sgr) =>
              linesBuffer.cursor.style = linesBuffer.cursor.style.applySgr(sgr)
            case MoveCursor(x, y)             => linesBuffer.cursor.translate(x, y)
            case SetColumn(n)                 => linesBuffer.cursor.setColumn(n)
            case SetCursor(y, x)              => linesBuffer.cursor.setPosition(y, x)
            case ToggleLatex                  => linesBuffer.cursor.style = linesBuffer.cursor.style.toggleLatex
            case ClearDisplay(n)              => linesBuffer.eraseInDisplay(n)
            case ClearLine(n)                 => linesBuffer.eraseInLine(n)
            case Bell                         => // play bell sound
            case Ignore                       =>
            case SaveCursorPosition           => linesBuffer.cursor.savePosition()
            case RestoreCursorPosition        => linesBuffer.cursor.restorePosition()
            case SetCursorVisibility(visible) => linesBuffer.cursor.setVisibility(visible)
            case Warn(cause)                  => println(cause)
          }
        }
        // println(s"Action: $action, Thread: ${Thread.currentThread.getName()}")
      }
    }.completedL
  }
}
