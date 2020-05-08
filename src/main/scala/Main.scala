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

  val pane: Pane = new Pane {
    prefWidth = Config.WIDTH
    prefHeight = Config.HEIGHT
  }

  val label: Label = new Label {
    text = "Hi there"
    alignmentInParent = Pos.Center
  }

  stage = new PrimaryStage {
    title.value = "Foo"
    scene = new Scene(Config.WIDTH, Config.HEIGHT) {
      fill = Color.Blue

      root = new BorderPane {
        prefWidth = Config.WIDTH
        prefHeight = Config.HEIGHT
        // Place for some top level component
        top = label
        // Place for main component
        center = pane
        fill = Color.Black
      }
    }
  }

  stage.handleEvent(KeyEvent.KeyTyped) {
    event: KeyEvent => {
      val response = event.eventType match {
        case KeyEvent.KeyPressed  => f"Key pressed ${event.character}"
        case KeyEvent.KeyReleased => f"Key pressed ${event.character}"
        case KeyEvent.KeyTyped    => f"Key typed ${event.character}"
        case _                    => "I don't know what you just did"
      }

      label.setText(response)
    }
  }

  trait KeyHandler {
    def handle: KeyEvent => Unit
  }

  new Thread(() => {
    val cmd = Array("/home/maciek/foo/a.out")
    val env = Array("TERM=xterm-color")

    val pty = PtyProcess.exec(cmd, env)

    val stdout = Source.fromInputStream(pty.getInputStream)
    val stdin = pty.getOutputStream

    def write(data: String): Unit = {
      stdin.write(data.getBytes(StandardCharsets.UTF_8))
      stdin.flush()
    }
    write("142 \r\n")

    for (c <- stdout)
      print(c)

    val result = pty.waitFor
  }).start()
}

