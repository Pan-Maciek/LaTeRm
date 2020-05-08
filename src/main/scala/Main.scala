import java.nio.charset.StandardCharsets

import com.pty4j.PtyProcess
import scalafx.application.JFXApp
import scalafx.geometry.Pos
import scalafx.scene.Scene
import scalafx.scene.control.Label
import scalafx.scene.input.KeyEvent
import scalafx.scene.layout.{BorderPane, Pane}
import scalafx.scene.paint.Color
import scalafx.Includes._
import scalafx.application.JFXApp.PrimaryStage

import scala.io.Source

object Main extends JFXApp {
  var command: String = ""
  val pane: Pane = new Pane {
    prefWidth = Config.WIDTH
    prefHeight = Config.HEIGHT
  }

  val label: Label = new Label {
    text = "Hi there"
    alignmentInParent = Pos.Center
  }

  stage = new PrimaryStage {
    title.value = "LaTeRm"
    scene = new Scene(Config.WIDTH, Config.HEIGHT) {
      root = new BorderPane {
        prefWidth = Config.WIDTH
        prefHeight = Config.HEIGHT
        // Place for some top level component
        top = label
        // Place for main component
        center = pane
      }
    }
  }

  stage.handleEvent(KeyEvent.KeyTyped) { event: KeyEvent =>
    {
      command = event.getCharacter match {
        case "\n" => ""
        case chr  => command ++ chr
      }

      label.setText(command)
    }
  }

}
