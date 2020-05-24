package config

import java.io.FileInputStream
import scalafx.scene.text.Font
import java.io.File

object UiConfig {
  val width  = 847.0
  val height = 650.0

  val DefaultLineHeight = 15
  val fontFamily        = "Fira Code Regular Nerd Font Complete Mono.ttf"
  val font =
    Font.loadFont(new FileInputStream(new File("./src/main/resources/fonts/fira-nerd.ttf")), 16)

  val updatePeriod = 50L // ms
}
