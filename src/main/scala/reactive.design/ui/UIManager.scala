package reactive.design.ui

import javafx.application.Platform
import monix.eval.Task
import monix.reactive.Observable
import reactive.design.data.{UIEvent, UIUpdate}

object UIManager {
  def apply(events: Observable[UIEvent]): (Panel, Task[Unit]) = {
    val manager = new UIManager()
    val task = events
      .mapEval(manager.onEvent)
      .completedL

    (manager.panel, task)
  }

}

private class UIManager() {
  val panel = new Panel()

  def onEvent(event: UIEvent) = {
    event match {
      case UIUpdate(data) =>
        val recent = data.recent()
        val coordinates = data.cursorCoordinates()

        PlatformOps.runSync{ panel.update(recent, coordinates) }
    }
  }

}
object PlatformOps {
    /** Low level stuff but necessary since UI thread is managed by the framework */
    def runSync(task: => Unit): Task[Unit] = {
      val mutex = new Object()
      mutex.synchronized {

        Platform.runLater { () => {
          task
          mutex.synchronized {
            mutex.notifyAll()
          }
        }}

        mutex.wait()
        Task.unit
      }
    }
}

