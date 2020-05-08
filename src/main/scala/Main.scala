import gui.TerminalPanel

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.layout.BorderPane

object Main extends JFXApp {
  val terminal = new TerminalPanel
  stage = new PrimaryStage {
    title = "Foo"
    scene = new Scene(600, 400) {
      root = new BorderPane {
        center = terminal
      }
    }
  }
  terminal.requestFocus()
}
