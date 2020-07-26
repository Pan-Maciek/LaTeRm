package reactive.design

import monix.reactive.Observable
import reactive.design.parser.Action
import monix.eval.Task

/**
  * Responsible for keeping state and forwarding updates to UI by returning Observable[UiUpdate]
  *
  * Implementation note -> if it cannot handle some action for example setTitle it should simply forward it further..
  * It was actually our previous design mistake, which led to spaghetti code.
  */
object DataManager {

  def apply(actions: Observable[Action]): Task[Unit] = ???

}

private class DataManager {}
