package term

import java.io.InputStream

import fastparse._
import NoWhitespace._

case class Write(val char: Char)
case class SetTitle(val str: String)
case class SetStyle(val sgr: Seq[Int])
case class MoveCursor(val x: Int, val y: Int)
case class SetColumn(val col: Int)
case class SetCursor(val x: Int, val y: Int)

object ActionParser {
  def parse(input: InputStream): Iterator[Any] = new Iterator[Any] {
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

    override def next(): Any =
      fastparse.parse(iter, NextAction(_)) match {
        case Parsed.Success(value, _) => value
        case Parsed.Failure(_, _, _)  => ???
      }
  }

  // https://en.wikipedia.org/wiki/C0_and_C1_control_codes
  // https://en.wikipedia.org/wiki/ANSI_escape_code
  def Number[_: P]: P[Int] =
    P(CharIn("0-9").rep.!.map {
      case "" => 1
      case x  => x.toInt
    })

  def Parameter[_: P]: P[Seq[Int]] =
    P(Number.rep(sep = ";"))
  def Intermediate[_: P]: P[String] =
    P(CharIn(" -/").rep(0).!)

  def ESC[_: P]: P[Unit] = P("\u001b")
  def BEL[_: P]          = P("\u0007") // Bell, Alert
  def ST[_: P]           = P("\u009c") // String Terminator

  def xOSC[_: P] =
    P(ESC ~ "]" ~ CharPred(_ != '\u0007').rep.! ~ BEL).map { // Operating System Command (xterm)
      case s"0;$title" => SetTitle(title)
      case _           => "Not Implemented"
    }

  def cursorCSI[_: P] =
    P(ESC ~ "[" ~ Number ~ CharIn("A-G").!).map {
      case (n, "A") => MoveCursor(0, -n) // CCU
      case (n, "B") => MoveCursor(0,  n) // CUD
      case (n, "C") => MoveCursor(n,  0) // CUF
      case (n, "D") => MoveCursor(-n, 0) // CUB
      case (n, "E") => MoveCursor(Int.MinValue,  n) // CNL
      case (n, "F") => MoveCursor(Int.MinValue, -n) // CPL
      case (n, "G") => SetColumn(n) // CHA
      case _ => "Not Implemented"
    }

  def foo[_: P] =
    P(CharIn("\n\r\b").!).map {
      case "\n" => MoveCursor(Int.MinValue, 1)
      case "\r" => SetColumn(1)
      case "\b" => MoveCursor(-1, 0)
    }

  def cursorAbsoluteCSI[_: P] =
    P(ESC ~ "[" ~ Parameter ~ "H").map {
      case x :: y :: Nil => SetCursor(x, y)
      case _ => ???
    }

  def SGR[_: P]: P[SetStyle] =
    P(ESC ~ "[" ~ Parameter ~ "m").map(SetStyle)

  def NextAction[_: P] = P(
    cursorAbsoluteCSI
    | foo
    | cursorCSI
    | xOSC
    | SGR
    | BEL
    | AnyChar.!.map(str => Write(str(0))))
}
