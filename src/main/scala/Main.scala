import gui.TerminalPanel

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.layout.BorderPane
import config.UiConfig
import scalafx.scene.layout.StackPane

object Main extends JFXApp {
  val terminal = new TerminalPanel
  stage = new PrimaryStage {
    title = "LaTeRm"
    scene = new Scene(UiConfig.width, UiConfig.height) {
      root = new StackPane() {
        children.add(terminal)
        resizable.set(true)
      }
    }
  }

  terminal.bind(stage.getScene.widthProperty, stage.getScene.heightProperty)
  terminal.requestFocus()
}
