package reactive.design

import reactive.design.data.LinesBuffer
import reactive.design.data.CursorData
import reactive.design.data.UIUpdate
import reactive.design.data.UIEvent
import reactive.design.parser.Action
import reactive.design.parser.SetTitle
import reactive.design.parser.SetStyle
import reactive.design.parser.MoveCursor
import reactive.design.parser.SetColumn
import reactive.design.parser.SetCursor
import reactive.design.parser.ToggleLatex
import reactive.design.parser.ClearDisplay
import reactive.design.parser.ClearLine
import reactive.design.parser.Bell
import reactive.design.parser.Ignore
import reactive.design.parser.SaveCursorPosition
import reactive.design.parser.RestoreCursorPosition
import reactive.design.parser.SetCursorVisibility
import reactive.design.parser.Warn
import reactive.design.parser.Write

import scala.concurrent.duration._
import monix.reactive.Observable
import monix.eval.Task

/**
  * Responsible for keeping state and forwarding updates to UI by returning Observable[UiUpdate]
  *
  * Note that whoever subscribes must do it synchronously and keep backpressure since we pass around mutable data!
  */
object DataManager {
  def apply(actionsObservable: Observable[Action]): Observable[UIEvent] = {
    val frames  = framesObservable().map(Left(_))
    val actions = actionsObservable.map(Right(_))
    val merged  = Observable(actions, frames).merge

    new DataManager().transform(merged)
  }

  private def framesObservable(): Observable[FrameT] =
    Observable
      .intervalWithFixedDelay(40.millis)
      .map(_ => Frame)

}

sealed trait FrameT
final case object Frame extends FrameT

private class DataManager {
  val cursor: CursorData  = new CursorData()
  val buffer: LinesBuffer = new LinesBuffer(cursor)

  def transform(events: Observable[Either[FrameT, Action]]): Observable[UIEvent] = {
    events
      .mapEval { event =>
        event match {
          case Left(_)       => Task { Some(UIUpdate(buffer, cursor.peek())) }
          case Right(action) => execAction(action) *> Task.pure(None)
        }
      }
      .filter(_.isDefined)
      .map(_.get)
  }

  def execAction(action: Action): Task[Unit] = {
    Task {
      action match {
        case Write(char)                  => buffer.write(char)
        case SetStyle(sgr)                => cursor.applySgr(sgr)
        case MoveCursor(x, y)             => cursor.translate(x, y)
        case SetColumn(n)                 => cursor.setColumn(n)
        case SetCursor(y, x)              => cursor.setPosition(y, x)
        case ToggleLatex                  => cursor.toggleLatex()
        case ClearDisplay(n)              => buffer.eraseInDisplay(n)
        case ClearLine(n)                 => buffer.eraseInLine(n)
        case SaveCursorPosition           => cursor.savePosition()
        case RestoreCursorPosition        => cursor.restorePosition()
        case SetCursorVisibility(visible) => cursor.setVisibility(visible)
        case Warn(cause)                  => ()
        case SetTitle(title)              => ()
        case Bell                         => ()
        case Ignore                       => ()
      }
    }
  }
}
