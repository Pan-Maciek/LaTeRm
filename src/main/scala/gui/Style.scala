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
  val latexRendering: Boolean
) {
  def applySRG(parameter: String) =
    parameter.split(';').map(_.toInt).map(Style.SGR.getOrElse(_, Skip)).foldLeft(this)((style, set) => set match {
        case SetForeground(fg) => style.copy(foreground = fg)
        case SetBackground(bg) => style.copy(background = bg)
        case SetLatex(value) => style.copy(latexRendering = value)
        case Skip => println(f"Unsupported operation"); style
        case _:SetDefault => Style.default
    })
}

object Style {
  val default: Style = Style(Color.White, Color.Black, false)

  // https://en.wikipedia.org/wiki/ANSI_escape_code#SGR_parameters
  val SGR = Map(
    0 -> SetDefault(),
    30 -> SetForeground(Color.Black),
    31 -> SetForeground(Color.Red),
    32 -> SetForeground(Color.Green),
    33 -> SetForeground(Color.Yellow),
    37 -> SetForeground(Color.White),

    40 -> SetBackground(Color.Black),
    41 -> SetBackground(Color.Red),
    42 -> SetBackground(Color.Green),
    43 -> SetBackground(Color.Yellow)
  )

}