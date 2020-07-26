package reactive.design

import monix.reactive.Observable
import monix.eval.Task
import reactive.design.data.UIEvent

object Renderer {
  def apply(events: Observable[UIEvent]): Task[Unit] = {
    ???
  }
}

class Renderer {}
