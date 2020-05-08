import gui.TerminalPanel

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.layout.BorderPane
import config.UiConfig

object Main extends JFXApp {
  val terminal = new TerminalPanel
  stage = new PrimaryStage {
    title = "LaTeRm"
    scene = new Scene(UiConfig.width, UiConfig.height) {
      root = new BorderPane {
        center = terminal
      }
    }
  }
  terminal.requestFocus()
}
