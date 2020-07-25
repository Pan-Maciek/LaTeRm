package reactive.design

import monix.reactive.Observable
import reactive.design.parser.Action

/**
  * Responsible for keeping state and forwarding updates to UI by returning Observable[UiUpdate]
  *
  * Note that if it cannot handle some action for example setTitle he should simply forward it further..
  * It was actually our previous design mistake, which led to spaghetti code.
  */
object DataManager {}
