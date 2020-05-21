import gui.TerminalPanel

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import config.UiConfig
import scalafx.scene.layout.StackPane
import javafx.application.Platform
import gui.drawable.CursorView

object Main extends JFXApp {
  val panel = new TerminalPanel()
  stage = new PrimaryStage {
    scene = new Scene(UiConfig.width, UiConfig.height) {
      root = panel
    }
  }
  val terminal = panel.screen
  stage.title <== terminal.title
  terminal.width <== stage.width
  terminal.height <== stage.height
  stage.onCloseRequest = _ => System.exit(0)

  terminal.drawBlank()
  terminal.requestFocus()
}
