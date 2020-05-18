import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import scalafx.scene.paint.Color
import org.scalatestplus.scalacheck.Checkers
import gui._
import gui.data._
import gui.Style
import org.scalacheck.Gen
import org.scalacheck.Arbitrary

class TerminalLineSpec extends AnyFlatSpec with Checkers {
  val default = Style.default
  val style   = Style(Color.Blue, Color.Red, false)

  "write" should "work correctly for one character" in {
    val line = new TerminalLine()
    line.write(0, 'a', default)

    assert(line.charAt(0) == 'a')
    assert(line.len == 1)
  }

  it should "work correctly for multiple writes of one character at the same position" in {
    val line = new TerminalLine()

    line.write(0, 'a', default)
    line.write(0, 'b', default)
    line.write(0, 'c', default)

    assert(line.charAt(0) == 'c')
    assert(line.len == 1)
  }

  it should "work correctly when appending characters" in {
    val line = new TerminalLine()

    line.write(0, 'a', default)
    line.write(1, 'b', default)
    line.write(2, 'c', default)

    assert(line.getText == "abc")
  }

  it should "insert spaces when appending character beyond line's length" in {
    val line = new TerminalLine()

    line.write(0, 'a', default)
    line.write(9, 'a', default)

    assert(line.charAt(0) == 'a')
    assert(line.charAt(9) == 'a')
    assert(line.len == 10)
  }

  it should " not merge when using different styles" in {
    val line = new TerminalLine()

    line.write(0, 'a', default)
    line.write(1, 'b', default)
    line.write(2, 'c', default)
    line.write(3, 'd', Style(Color.Blue, Color.Red, false))

    assert(line.blocksSize() == 2)
  }

  it should " correctly split " in {
    val line = new TerminalLine()

    line.write(0, 'a', style)
    line.write(1, 'b', default)
    line.write(2, 'c', style)
    assert(line.blocksSize() == 3)

    line.write(1, 'd', style)
    assert(line.blocksSize() == 1)
  }

  it should "handle sequence of writes with alternating styles" in {
    val line = new TerminalLine()

    line.write(0, 'a', style)
    line.write(1, 'b', default)
    line.write(2, 'c', style)
    line.write(3, 'd', default)
    assert(line.blocksSize() == 4)

    line.write(1, 'a', style)
    assert(line.blocksSize() == 2)

    line.write(3, 'a', style)
    assert(line.blocksSize() == 1)

    line.write(4, 'a', style)
    assert(line.blocksSize() == 1)

    line.write(1, 'a', default)
    assert(line.blocksSize() == 3)

    line.write(0, 'a', default)
    assert(line.blocksSize() == 2)

    line.write(0, 'a', style)
    line.write(1, 'a', style)
    assert(line.blocksSize() == 1)
  }

  "insert" should "work correctly for one character" in {
    val line = new TerminalLine()
    line.insert(0, 'a', default)
    line.insert(0, 'a', default)
    line.insert(0, 'a', default)

    assert(line.charAt(0) == 'a')
    assert(line.len == 3)
  }

  it should "work correctly for multiple characters" in {
    val line = new TerminalLine()
    line.insert(0, 'a', default)
    line.insert(1, 'a', default)
    line.insert(0, 'x', Style(Color.Red, Color.Blue, false))

    assert(line.charAt(0) == 'x')
    assert(line.len == 3)
    assert(line.blocksSize() == 2)
  }

  it should "correctly split when inserting different style" in {
    val line = new TerminalLine()

    line.insert(0, 'a', default)
    line.insert(1, 'b', default)
    line.insert(2, 'c', default)
    line.insert(1, 'd', Style(Color.Blue, Color.Red, false))

    assert(line.blocksSize() == 3)
    assert(line.len == 4)
  }

  it should "handle sequence of insertions with alternating styles" in {
    val line = new TerminalLine()

    line.insert(0, 'a', style)
    line.insert(1, 'b', default)
    line.insert(2, 'c', style)
    line.insert(3, 'd', default)
    assert(line.blocksSize() == 4)

    line.insert(1, 'a', style)
    assert(line.blocksSize() == 4)

    line.insert(3, 'x', style)
    assert(line.blocksSize() == 4)

    line.insert(5, 'c', default)

    line.insert(1, 'c', default)
    assert(line.blocksSize() == 6)

    line.write(3, 'c', style)
    assert(line.blocksSize() == 4)
  }

  "deleteAt" should "handle sequence of removes, merge blocks" in {
    val line = new TerminalLine()

    line.insert(0, 'a', style)
    line.insert(1, 'b', default)
    line.insert(2, 'c', style)
    line.insert(3, 'd', default)

    line.deleteAt(0)
    assert(line.len == 3)
    assert(line.blocksSize() == 3)

    line.deleteAt(1)
    assert(line.len == 2)
    assert(line.blocksSize() == 1)
  }

  it should "handle removing all characters" in {
    val line = new TerminalLine()

    line.insert(0, 'a', style)
    line.insert(1, 'b', default)
    line.insert(2, 'c', style)
    line.insert(3, 'd', default)

    line.deleteAt(0)
    assert(line.len == 3)
    assert(line.blocksSize() == 3)

    line.deleteAt(1)
    assert(line.len == 2)
    assert(line.blocksSize() == 1)

    line.deleteAt(1)
    line.deleteAt(0)
    assert(line.len == 0)
    assert(line.blocksSize() == 1)
  }

  "deleteFrom" should "work..." in {
    val line = new TerminalLine()

    line.insert(0, 'a', style)
    line.insert(1, 'b', style)
    line.insert(2, 'c', style)
    line.insert(3, 'd', default)
    assert(line.blocksSize() == 2)

    line.deleteFrom(1)
    assert(line.len == 1)
    assert(line.blocksSize() == 1)

    line.deleteFrom(0)
    assert(line.len == 0)
    assert(line.blocksSize() == 1)

    line.write(0, 'x', style)
    line.deleteFrom(0)
    assert(line.len == 0)
    assert(line.blocksSize() == 1)
  }

  "deleteTo" should "work..." in {
    val line = new TerminalLine()

    line.insert(0, 'a', style)
    line.insert(1, 'b', default)
    line.insert(2, 'c', style)
    line.insert(3, 'd', default)

    line.deleteTo(1)
    assert(line.len == 3)
    assert(line.blocksSize() == 3)

    println(line)
    line.deleteTo(line.len)
    assert(line.len == 0)
    assert(line.blocksSize() == 1)
  }

  implicit val lineGen: Gen[TerminalLine]                     = Gen.const(new TerminalLine)
  implicit val arbitraryTerminalLine: Arbitrary[TerminalLine] = Arbitrary({ lineGen })

  "invaiant" should "hold: for n insertions line's length is increased by n" in {
    check((line: TerminalLine, seq: String) => {
      val prevLen = line.len
      for (c <- seq) {
        line.insert(0, c, Style.default)
      }

      line.len == seq.size + prevLen
    })
  }
}
