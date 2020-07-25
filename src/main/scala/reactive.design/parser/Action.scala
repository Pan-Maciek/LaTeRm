package reactive.design.parser

sealed trait Action

final case class Write(char: Char)                     extends Action
final case class SetTitle(str: String)                 extends Action
final case class SetStyle(sgr: Seq[Int])               extends Action
final case class MoveCursor(x: Int, y: Int)            extends Action
final case class SetColumn(col: Int)                   extends Action
final case class SetCursor(y: Int, x: Int)             extends Action
final case class SetCursorVisibility(visible: Boolean) extends Action
final case class ClearDisplay(clearType: Int)          extends Action
final case class ClearLine(clearType: Int)             extends Action
case object SaveCursorPosition                         extends Action
case object RestoreCursorPosition                      extends Action
case object ToggleLatex                                extends Action
case object Ignore                                     extends Action
case object Bell                                       extends Action
final case class Warn(cause: Any)                      extends Action
