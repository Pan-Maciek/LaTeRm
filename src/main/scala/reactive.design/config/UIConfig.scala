package reactive.design.config

import java.io.{File, FileInputStream}

import org.scilab.forge.jlatexmath.DefaultTeXFont
import scalafx.scene.text.Font

import scala.concurrent.duration._

object UIConfig {
  lazy val screenWidth  = 847.0
  lazy val screenHeight = 650.0

  lazy val fontSize: Float = 16
  lazy val defaultLineHeight: Float = fontSize
  lazy val fontFamily        = "Fira Code Regular Nerd Font Complete Mono.ttf"
  lazy val font = Font.loadFont(new FileInputStream(new File("./src/main/resources/fonts/fira-nerd.ttf")), fontSize)
  lazy val latexFont = new DefaultTeXFont(fontSize)

  lazy val defaultMaxCharsInLine: Int = 86

  lazy val updateInterval = 80.millis
}
