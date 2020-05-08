package term

import java.io.{InputStream, OutputStream}

import com.pty4j.PtyProcess

class Terminal {
  private val pty = PtyProcess.exec(Array("/bin/bash"), Array("TERM=xterm-color"))
  val stdin: OutputStream = pty.getOutputStream
  val stdout: InputStream = pty.getInputStream
}