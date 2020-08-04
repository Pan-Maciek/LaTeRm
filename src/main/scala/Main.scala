import com.pty4j.{PtyProcess, WinSize}
import javafx.application.Platform
import javafx.scene.input.KeyCode.{DOWN, LEFT, RIGHT, UP}
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import reactive.design.config.{SystemConstants, UIConfig}
import reactive.design.data.DataManager
import reactive.design.parser.ActionProvider
import reactive.design.ui.UIManager
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene

import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

object Main extends JFXApp {
  import Pty._
  val pty              = Pty()
  val actions          = ActionProvider(pty.getInputStream)
  val events           = DataManager(actions)
  val (panel, effects) = UIManager(events)

  stage = new PrimaryStage {
    scene = new Scene(UIConfig.screenWidth, UIConfig.screenHeight) {
      root = panel
      onKeyTyped = e => pty.write(e.getCharacter.getBytes)
      onKeyPressed = e => {
        val bytes = e.getCode match {
          case UP    => Some("\u001b[A".getBytes)
          case DOWN  => Some("\u001b[B".getBytes)
          case RIGHT => Some("\u001b[C".getBytes)
          case LEFT  => Some("\u001b[D".getBytes)
          case _     => None
        }
        Task { bytes.map(pty.write) }.runToFuture
      }
    }
  }
  stage.onCloseRequest = _ => Platform.exit()
  stage.title.value_=("LaTerm")

  panel.screen.width <== stage.width
  panel.screen.height <== stage.height
  panel.screen.drawBlank()

  effects.delayExecution(20.millis).runAsyncAndForget
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
      pty.getOutputStream.write(bytes)
  }
}
