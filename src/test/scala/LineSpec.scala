import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import gui._
import gui.data._
import gui.Style
import gui.data.Block._
import scalafx.scene.paint.Color

class LineSpec extends AnyFlatSpec {
  val default = Style.default

  "Write " should "work correctly for one character" in {
    val line = new Line()
    line.write(0, 'a', default)

    assert(line.charAt(0) == 'a')
    assert(line.len() == 1)
  }

  it should "work correctly for multiple writes of one character at the same position" in {
    val line = new Line()

    line.write(0, 'a', default)
    line.write(0, 'b', default)
    line.write(0, 'c', default)

    assert(line.charAt(0) == 'c')
    assert(line.len() == 1)
  }

  it should "work correctly when appending characters" in {
    val line = new Line()

    line.write(0, 'a', default)
    line.write(1, 'b', default)
    line.write(2, 'c', default)

    assert(line.getText() == "abc")
  }

  it should "insert spaces when appending character beyond line's length" in {
    val line = new Line()

    line.write(0, 'a', default)
    line.write(9, 'a', default)

    assert(line.charAt(0) == 'a')
    assert(line.charAt(9) == 'a')
    assert(line.len() == 10)
  }

  it should " not merge when using different styles" in {
    val line = new Line()

    line.write(0, 'a', default)
    line.write(1, 'b', default)
    line.write(2, 'c', default)
    line.write(3, 'd', Style(Color.Blue, Color.Red, false))

    assert(line.blocksSize() == 2)
  }

  it should " correctly split " in {
    val line  = new Line()
    val style = Style(Color.Blue, Color.Red, false)

    line.write(0, 'a', style)
    line.write(1, 'b', default)
    line.write(2, 'c', style)
    assert(line.blocksSize() == 3)

    line.write(1, 'd', style)
    assert(line.blocksSize() == 1)
  }

  it should "handle sequence of writes with alternating styles" in {
    val line  = new Line()
    val style = Style(Color.Blue, Color.Red, false)

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
    println(line)

    line.write(0, 'a', style)
    line.write(1, 'a', style)
    assert(line.blocksSize() == 1)
  }
}
