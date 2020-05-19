import gui.TerminalPanel

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import config.UiConfig
import scalafx.scene.layout.StackPane
import javafx.application.Platform

object Main extends JFXApp {
  val terminal = new TerminalPanel()
  stage = new PrimaryStage {
    scene = new Scene(UiConfig.width, UiConfig.height) {
      root = new StackPane() {
        children.add(terminal)
        resizable.set(false)
      }
    }
  }
  stage.title <== terminal.title

  terminal.width <== stage.width
  terminal.height <== stage.height

  terminal.drawBlank()
  terminal.requestFocus()
}
