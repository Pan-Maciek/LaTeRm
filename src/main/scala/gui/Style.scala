package gui

import scalafx.scene.paint.Color


sealed trait StyleSetter
case class SetForeground(val color: Color) extends StyleSetter
case class SetBackground(val color: Color) extends StyleSetter
case class SetLatex(val on: Boolean) extends StyleSetter
case class SetDefault() extends StyleSetter
case class Skip() extends StyleSetter

case class Style (
  val foreground: Color,
  val background: Color,
  val latexRendering: Boolean,
  val bold: Boolean
) {
  def toggleLatex: Style = copy(latexRendering = !latexRendering)

  def applySgr(sgr: Seq[Int]): Style =
    applySgr(this, sgr)

  @scala.annotation.tailrec
  private def applySgr(style: Style, sgr: Seq[Int]): Style = {
    sgr match {
      case Nil => style

      case fg :: tail if 30 <= fg && fg <= 37 =>
        applySgr(style copy (foreground = Style.foreground4bit(fg)), tail)
      case fg :: tail if 90 <= fg && fg <= 97 =>
        applySgr(style copy (foreground = Style.foreground4bit(fg)), tail)
      case 38 :: 5 :: n :: tail =>
        applySgr(style, tail)
      case 38 :: 2 :: r :: g :: b :: tail =>
        applySgr(style copy (foreground = Color.rgb(r, g, b)), tail)
      case 39 :: tail =>
        applySgr(style copy (foreground = Style.default.foreground), tail)

      case bg :: tail if 30 <= bg && bg <= 47 =>
        applySgr(style copy (background = Style.background4bit(bg)), tail)
      case bg :: tail if 100 <= bg && bg <= 107 =>
        applySgr(style copy (background = Style.background4bit(bg)), tail)
      case 48 :: 5 :: n :: tail =>
        applySgr(style, tail)
      case 48 :: 2 :: r :: g :: b :: tail =>
        applySgr(style copy (background = Color.rgb(r, g, b)), tail)
      case 49 :: tail =>
        applySgr(style copy (background = Style.default.background), tail)

      case 1 :: tail => applySgr(style.copy(bold = true), tail)
      case 0 :: tail => applySgr(Style.default, tail)
    }
  }
}

object Style {
  val default: Style = Style(
    foreground = Color.White,
    background = Color.Black,
    latexRendering = false,
    bold = false
  )

  // https://en.wikipedia.org/wiki/ANSI_escape_code#SGR_parameters
  val Sgr4BitColor = Map(
    30 -> Color.rgb(1,1,1), // Black
    31 -> Color.rgb(222,56,43), // Red
    32 -> Color.rgb(57,181,74), // Green
    33 -> Color.rgb(255,199,6), // Yellow
    34 -> Color.rgb(0,111,184), // Blue
    35 -> Color.rgb(118,38,113), // Magenta
    36 -> Color.rgb(44,181,233), // Cyan
    37 -> Color.rgb(204,204,204), // White

    90 -> Color.rgb(128,128,128), // Bright Black
    91 -> Color.rgb(255,0,0), // Bright Red
    92 -> Color.rgb(0,255,0), // Bright Green
    93 -> Color.rgb(255,255,0), // Bright Yellow
    94 -> Color.rgb(0,0,255), // Bright Blue
    95 -> Color.rgb(255,0,255), // Bright Magenta
    96 -> Color.rgb(0,255,255), // Bright Cyan
    97 -> Color.rgb(255,255,255), // Bright White
  )

  def foreground4bit(fg: Int): Color = Sgr4BitColor(fg)
  def background4bit(bg: Int): Color = Sgr4BitColor(bg - 10)
}
