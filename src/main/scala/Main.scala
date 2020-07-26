import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import config.UiConfig
import scalafx.scene.layout.StackPane
import javafx.application.Platform
import com.pty4j.PtyProcess
import config.SystemConstants
import scala.jdk.CollectionConverters._
import com.pty4j.WinSize
import reactive.design.parser.ActionProvider
import monix.eval.Task
import cats.effect.IOApp
import reactive.design.DataManager
import reactive.design.UIManager
import monix.execution.Scheduler.Implicits.global

object Main extends JFXApp {
  import Pty._
  val pty              = Pty()
  val actions          = ActionProvider(Task { pty.getInputStream() })
  val events           = DataManager(actions)
  val (panel, effects) = UIManager(events, pty.write)

  stage = new PrimaryStage {
    scene = new Scene(UiConfig.width, UiConfig.height) {
      root = panel
    }
  }
  panel.screen.drawBlank()
  stage.onCloseRequest = _ => Platform.exit()
  // terminal.width <== stage.width
  // terminal.height <== stage.height

  // val task = StdoutDriver(this, linesBuffer, actions)
  // task.runAsyncAndForget  terminal.requestFocus()
  effects.runAsyncAndForget
}

object Pty {
  val defaultWidth  = 86
  val defaultHeight = 31

  def apply() = {
    val cmd = Array(SystemConstants.shell)
    val env =
      Map("TERM" -> "xterm-color") ++ SystemConstants.environment

    val ptyP = PtyProcess.exec(cmd, env.asJava)
    ptyP.setWinSize(new WinSize(defaultWidth, defaultHeight))

    ptyP
  }

  implicit class PtyOps(pty: PtyProcess) {
    def write(bytes: Array[Byte]): Unit =
      pty.getOutputStream().write(bytes)
  }
}
