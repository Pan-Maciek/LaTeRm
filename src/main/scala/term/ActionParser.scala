package term

import java.io.InputStream

import fastparse._
import NoWhitespace._

sealed trait Action
case class Write(char: Char) extends Action
case class SetTitle(str: String) extends Action
case class SetStyle(sgr: Seq[Int]) extends Action
case class MoveCursor(x: Int, y: Int) extends Action
case class SetColumn(col: Int) extends Action
case class SetCursor(x: Int, y: Int) extends Action
case class SetCursorVisibility(visible: Boolean) extends Action
case class ClearDisplay(clearType: Int) extends Action
case class ClearLine(clearType: Int) extends Action
case object SaveCursorPosition extends Action
case object RestoreCursorPosition extends Action
case object ToggleLatex extends Action
case object Bell extends Action

object ActionParser {
  def parse(input: InputStream): Iterator[Action] = new Iterator[Action] {
    val iter = new Iterator[String] {
      var _hasNext = true
      override def hasNext: Boolean = _hasNext
      override def next(): String   = {
        val char = input.read
        if (char == -1) _hasNext = false
        char.toChar.toString
      }
    }

    override def hasNext: Boolean = iter.hasNext

    override def next(): Action =
      fastparse.parse(iter, NextAction(_)) match {
        case Parsed.Success(value, _) => value
        case Parsed.Failure(_, _, _)  => ???
      }
  }

  // https://en.wikipedia.org/wiki/C0_and_C1_control_codes
  // https://en.wikipedia.org/wiki/ANSI_escape_code
  def Number[_: P](defaultValue: Int): P[Int] =
    P(CharIn("0-9").rep.!.map {
      case "" => defaultValue
      case x  => x.toInt
    })

  def Parameter[_: P]: P[Seq[Int]] =
    P(Number(1).rep(sep = ";"))
  def Intermediate[_: P]: P[String] =
    P(CharIn(" -/").rep(0).!)

  def ESC[_: P]: P[Unit] = P("\u001b")
  def BEL[_: P]          = P("\u0007").map(_ => Bell) // Bell, Alert
  def ST[_: P]           = P("\u009c") // String Terminator

  def xOSC[_: P] =
    P(ESC ~ "]" ~ CharPred(_ != '\u0007').rep.! ~ "\u0007").map { // Operating System Command (xterm)
      case s"0;$title" => SetTitle(title)
      case _           => ???
    }

  def cursorCSI[_: P] =
    P(ESC ~ "[" ~ Number(1) ~ CharIn("A-G").!).map {
      case (n, "A") => MoveCursor(0, -n) // CCU
      case (n, "B") => MoveCursor(0,  n) // CUD
      case (n, "C") => MoveCursor(n,  0) // CUF
      case (n, "D") => MoveCursor(-n, 0) // CUB
      case (n, "E") => MoveCursor(Int.MinValue,  n) // CNL
      case (n, "F") => MoveCursor(Int.MinValue, -n) // CPL
      case (n, "G") => SetColumn(n) // CHA
      case _ => ???
    }

  def specialCharacters[_: P] =
    P(CharIn("\n\r\b").!).map {
      case "\n" => MoveCursor(Int.MinValue, 1)
      case "\r" => SetColumn(1)
      case "\b" => MoveCursor(-1, 0)
    }

  def cursorAbsoluteCSI[_: P] =
    P(ESC ~ "[" ~ Parameter ~ "H").map {
      case y :: x :: Nil => SetCursor(y, x)
      case y :: Nil      => SetCursor(y, 1)
      case _ => ???
    }

  def cursorShowHide[_: P] =
    P(ESC ~ "[" ~ "?25" ~ CharIn("hl").!).map {
      case "h" => SetCursorVisibility(true)
      case "l" => SetCursorVisibility(false)
      case _   => ???
    }

  def cursorHistory[_: P] =
    P(ESC ~ "[" ~ CharIn("su").!).map {
      case "s" => SaveCursorPosition
      case "u" => RestoreCursorPosition
      case _  => ???
    }

  def SGR[_: P]: P[SetStyle] =
    P(ESC ~ "[" ~ Parameter ~ "m").map(SetStyle)

  def toggleLatex[_: P] =
    P(ESC ~ "[" ~ "Y").map(_ => ToggleLatex)

  def clear[_: P] =
    P(ESC ~ "[" ~ Number(0) ~ CharIn("JK").!).map {
      case (n, "J") => ClearDisplay(n)
      case (n, "K") => ClearLine(n)
      case (_, _)   => ???
    }

  def oldSchoolCursor[_: P] =
    P(ESC ~ CharIn("78").!).map {
      case "7" => println("save"); SaveCursorPosition
      case "8" => println("restore"); RestoreCursorPosition
      case _ => ???
    }

  def NextAction[_: P]: P[Action] = P(
    cursorAbsoluteCSI
    | cursorCSI
    | clear
    | cursorShowHide
    | xOSC
    | SGR
    | BEL
    | toggleLatex
    | specialCharacters
    | cursorShowHide
    | cursorHistory
    | oldSchoolCursor
    | AnyChar.!.map(str => Write(str(0))))
}
