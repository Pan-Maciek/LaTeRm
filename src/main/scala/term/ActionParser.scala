package term

import java.io.InputStream

import fastparse._
import NoWhitespace._

case class Write(val char: Char)
case class SetTitle(val str: String)
case class Sgr(val seq: Seq[Int])

object ActionParser {
  def parse(input: InputStream) = new Iterator[Any] {
    val iter = new Iterator[String] {
      override def hasNext: Boolean = true
      override def next(): String   = input.read.toChar.toString
    }

    override def hasNext: Boolean = true

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
      case "" => 0
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

  def SGR[_: P] =
    P(ESC ~ "[" ~ Parameter ~ "m").map(Sgr)

  def NextAction[_: P] = P(xOSC | SGR | AnyChar.!.map(str => Write(str(0))))
}
