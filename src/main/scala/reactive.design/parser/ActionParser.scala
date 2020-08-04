package reactive.design.parser

import fastparse.NoWhitespace._
import fastparse._

object ActionParser {
  def apply(it: Iterator[String]): Iterator[Action] = new Iterator[Action] {
    override def hasNext: Boolean = it.hasNext
    override def next(): Action = {
      parse(it, NextAction(_)) match {
        case Parsed.Success(value, _) => value
        case Parsed.Failure(_, _, _)  => Ignore
      }
    }
  }

  // https://en.wikipedia.org/wiki/C0_and_C1_control_codes
  // https://en.wikipedia.org/wiki/ANSI_escape_code
  def Number[_: P](defaultValue: Int): P[Int] =
    P(CharIn("0-9").rep.!.map {
      case "" => defaultValue
      case x  => x.toInt
    })

  def Parameter[_: P](defaultValue: Int): P[Seq[Int]] =
    P(Number(defaultValue).rep(sep = ";"))

  def ESC[_: P] = P(CharIn("\u001b"))
  def BEL[_: P] = P("\u0007").map(_ => Bell) // Bell, Alert
  def ST[_: P]  = P("\u009c") // String Terminator

  def xOSC[_: P] =
    P(CharPred(_ != '\u0007').rep.! ~ "\u0007").map { // Operating System Command (xterm)
      case s"0;$title" => SetTitle(title)
      case other       => Warn(other)
    }

  def cursorCSI[_: P] =
    P(Number(1) ~ CharIn("A-G").!).map {
      case (n, "A") => MoveCursor(0, -n) // CCU
      case (n, "B") => MoveCursor(0, n) // CUD
      case (n, "C") => MoveCursor(n, 0) // CUF
      case (n, "D") => MoveCursor(-n, 0) // CUB
      case (n, "E") => MoveCursor(Int.MinValue, n) // CNL
      case (n, "F") => MoveCursor(Int.MinValue, -n) // CPL
      case (n, "G") => SetColumn(n) // CHA
      case _        => Ignore
    }

  def specialCharacters[_: P] =
    P(CharIn("\n\r\b").!).map {
      case "\n" => MoveCursor(Int.MinValue, 1)
      case "\r" => SetColumn(1)
      case "\b" => MoveCursor(-1, 0)
    }

  def cursorAbsoluteCSI[_: P] =
    P(Parameter(1) ~ "H").map {
      case y :: x :: Nil => SetCursor(y, x)
      case y :: Nil      => SetCursor(y, 1)
      case _             => Ignore
    }

  def cursorShowHide[_: P] =
    P("25" ~ CharIn("hl").!).map {
      case "h" => SetCursorVisibility(true)
      case "l" => SetCursorVisibility(false)
      case _   => Ignore
    }

  def cursorHistory[_: P] =
    P(CharIn("su").!).map {
      case "s" => SaveCursorPosition
      case "u" => RestoreCursorPosition
      case _   => Ignore
    }

  def SGR[_: P]: P[SetStyle] =
    P(Parameter(0) ~ "m").map(SetStyle)

  def toggleLatex[_: P] = P("Y").map(_ => ToggleLatex)

  def clear[_: P] =
    P(Number(0) ~ CharIn("JK").!).map {
      case (n, "J") => ClearDisplay(n)
      case (n, "K") => ClearLine(n)
      case (_, _)   => Ignore
    }

  def oldSchoolCursor[_: P] =
    P(CharIn("78q").!).map {
      case "7" => SaveCursorPosition
      case "8" => RestoreCursorPosition
      case _   => Ignore
    }

  def ignored1[_: P] = P(CharIn("0-9").rep(0) ~ "h").map(_ => Ignore)
  def ignored2[_: P] = P(CharIn("0-9").rep(1) ~ " q").map(_ => Ignore)

  def parser[_: P] = P(
    "\u001b" ~ (
      ("[" ~ (
        clear
          | cursorCSI
          | cursorAbsoluteCSI
          | toggleLatex
          | cursorHistory
          | SGR
          | ignored2
          | ("?" ~ (cursorShowHide
            | ignored1))
      ))
        | ("]" ~ xOSC)
        | "=".!.map(_ => Ignore)
        | oldSchoolCursor
    )
  )

  def NextAction[_: P]: P[Action] =
    P(
      parser
        | BEL
        | specialCharacters
        | "\u001b".!.map(_ => Ignore)
        | AnyChar.!.map(str => Write(str(0)))
    )
}
