import java.nio.charset.StandardCharsets

import com.pty4j.PtyProcess
import scalafx.application
import scalafx.application.JFXApp

import scala.io.Source

object Main extends JFXApp {
  stage = new application.JFXApp.PrimaryStage {
    title.value = "Hello Stage"
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
