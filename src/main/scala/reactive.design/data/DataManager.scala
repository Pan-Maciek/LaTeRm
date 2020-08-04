package reactive.design.data

import monix.eval.Task
import monix.reactive.Observable
import reactive.design.config.UIConfig
import reactive.design.parser._


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
      .intervalWithFixedDelay(UIConfig.updateInterval)
      .map(_ => Frame)

}

sealed trait FrameT
case object Frame extends FrameT

private class DataManager {
  val cursor: CursorData  = new CursorData()
  val buffer: LinesBuffer = new LinesBuffer(cursor)

  var _modified: Boolean = false
  def manageState(modified: Boolean): Task[Unit] = Task.pure { _modified = true }

  def transform(events: Observable[Either[FrameT, Action]]): Observable[UIEvent] = {
    events
      .mapEval {
        case Left(_) =>
          Task.pure { Some{ UIUpdate(DataPeek(buffer, cursor.peek())) } }
        case Right(action) =>
          manageState(true) *>
          execAction(action) *>
          Task.pure(None)
      }
      .filter(_.isDefined && _modified)
      .mapEval(manageState(false) *> Task(_))
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
        case Warn(_)                  => ()
        case SetTitle(_)              => ()
        case Bell                         => ()
        case Ignore                       => ()
      }

      buffer.appendBlankLines()
    }
  }
}
