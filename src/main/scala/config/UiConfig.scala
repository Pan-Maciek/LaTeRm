package config

import java.io.{File, FileInputStream}

import org.scilab.forge.jlatexmath.DefaultTeXFont
import scalafx.scene.text.Font

object UiConfig {
  val width  = 847.0
  val height = 650.0

  val fontSize: Float = 16
  val DefaultLineHeight: Float = fontSize
  val fontFamily        = "Fira Code Regular Nerd Font Complete Mono.ttf"
  val font = Font.loadFont(new FileInputStream(new File("./src/main/resources/fonts/fira-nerd.ttf")), fontSize)
  val latexFont = new DefaultTeXFont(fontSize)

  val updatePeriod = 50L // ms
}
