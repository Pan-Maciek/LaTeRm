package gui.data

import gui.Style
import javafx.geometry.Bounds
import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.text.{Font, Text}

import Ordering.Double.TotalOrdering
import scala.collection.mutable
import scala.collection.Searching._

final case class Data() {}

trait Drawable {
  def draw(graphicsContext: GraphicsContext): Unit
  def width: Double
  def height: Double
}

final case class TerminalLine() extends Drawable {

  private val sb     = new StringBuilder
  private val blocks = mutable.ArrayBuffer(Block.empty)

  def getText: String     = sb.toString()
  def charAt(column: Int): Char = sb.charAt(column)
  def len(): Int          = sb.size
  def blocksSize(): Int   = blocks.size

  /** Replaces character at given column.
    * If column exceeds len, then it appends spaces (with default style) until specified column is reached,
    * at which point it inserts given character. */
  def write(column: Int, v: Char, style: Style): Unit = {
    assert(column >= 0)
    val mergeI =
      if (column >= len) {
        // in case line is shorter append
        val missing = column - len

        blocks.append(Block(len(), column, Style.default))
        sb.append(" " * missing)

        blocks.append(Block(len(), style))
        sb.append(v)
        blocks.length - 1
      } else {
        // simply replace
        val blockI = findBlock(column)
        sb.replace(column, column + 1, String.valueOf(v))
        val block = blocks(blockI)

        // split if necessary
        if (block.style != style) {
          val (left, middle, right) = block.split(column, style)
          val iterable              = List(middle, right)

          blocks(blockI) = left
          blocks.insertAll(blockI + 1, iterable)
          blockI + 1
        } else {
          blockI
        }
      }

    mergeAt(mergeI)
  }

  /** Legacy code - inserts character into specified position. */
  def insert(column: Int, v: Char, style: Style): Unit = {
    if (column >= len) {
      write(column, v, style)
    } else {
      val i     = findBlock(column)
      val block = blocks(i)
      sb.insert(column, String.valueOf(v))

      val (j, merge) =
        if (block.to == column) {
          // Check neighours for merge
          val shiftFrom = if (block.style == style) {
            // append to left
            val left = block
            blocks(i) = left.copy(to = left.to + 1)
            i + 1
          } else if (i < blocks.size - 1 && blocks(i + 1).style == style) {
            // prepend to right
            val right = blocks(i + 1)
            blocks(i + 1) = right.copy(to = right.to + 1)
            i + 2
          } else {
            // create new block
            val next = Block(column, style)
            blocks.insert(i + 1, next)
            i + 2
          }
          (shiftFrom, false)
        } else {
          // split block
          val (left, middle, unshiftedRight) = block.split(column, style)
          val right                          = unshiftedRight.copy(to = unshiftedRight.to + 1)
          val iterable                       = List(middle, right)

          blocks(i) = left
          blocks.insertAll(i + 1, iterable)
          (i + 2, true)
        }

      // clean up
      val shiftFrom = j + 1
      val mergeI    = j - 1

      shiftBlocks(shiftFrom)
      if (merge)
        mergeAt(mergeI)
    }
  }

  def delete(column: Int): Unit = {
    assert(column >= 0)
    assert(column < len())

    sb.deleteCharAt(column)
    val blockI = findBlock(column)
    val block  = blocks(blockI)

    if (block.len == 1) {
      blocks.remove(blockI)
      if (blocksSize() == 0) {
        blocks.append(Block.empty)
      } else {
        shiftBlocks(blockI, -1)
        mergeAt(blockI)
      }
    } else {
      blocks(blockI) = block.copy(to = block.to - 1)
      shiftBlocks(blockI + 1, -1)
    }
  }

  /** Merges starting with block at `i`, note that this should also remove empty blocks.*/
  private def mergeAt(i: Int): Unit = {
    var block  = blocks(i)
    var blockI = i

    // merge left
    var leftI = blockI - 1
    while (leftI >= 0 && (blocks(leftI).style == block.style || blocks(leftI).len == 0)) {
      val left = blocks(leftI)
      if (left.len != 0)
        block = block.merge(left)

      blocks.remove(leftI)
      blockI -= 1
      leftI -= 1

      blocks(blockI) = block
    }

    // merge right
    var rightI = blockI + 1
    while (rightI < blocks.length && (blocks(rightI).style == block.style || blocks(rightI).len == 0)) {
      val right = blocks(rightI)
      if (right.len != 0)
        block = block.merge(right)

      blocks.remove(rightI)
      blocks(blockI) = block
    }
  }

  private def shiftBlocks(from: Int, by: Int = 1): Unit = {
    for (i <- from until blocks.length;
         block = blocks(i))
      blocks(i) = block.shiftRight(by)
  }

  private def findBlock(column: Int): Int = {
    assert(column <= len())

    blocks.search(Block(column, column + 1, Style.default)) match {
      case Found(i)          => i
      case InsertionPoint(i) => i
    }
  }

  override def toString: String = {
    val ssb = new StringBuilder
    ssb.append(f"{Line: \t${sb.toString}")
    for (block <- blocks)
      ssb.append(f"\n\t${block.toString}")

    ssb.append(f"}")
    ssb.toString
  }

  override def width: Double = blocks.foldLeft(0.0)(_ + _.width)
  override def height: Double = blocks.map(_.height).max

  def draw(graphicsContext2D: GraphicsContext): Unit = {
    if (sb.nonEmpty) {
      graphicsContext2D.save()
      for (block <- blocks) {
        if (block.len > 0) {
          block.draw(graphicsContext2D)
          graphicsContext2D.translate(block.width, 0)
        }
      }
      graphicsContext2D.restore()
    }
  }

  object Block {
    /** used by bin search */
    implicit def ordering[A <: Block]: Ordering[Block] = Ordering.by(_.to)

    /** singleton block holding one character */
    def apply(where: Int, style: Style): Block = Block(where, where + 1, style)

    /** should be used only when creating new line! */
    val empty: Block = Block(0, 0, Style.default)
  }

  case class Block(from: Int, to: Int, style: Style) extends Drawable {
    def len(): Int = to - from

    def shiftRight(by: Int = 1): Block = {
      val shiftedFrom = this.from + by
      val shiftedTo   = this.to + by

      this.copy(shiftedFrom, shiftedTo)
    }

    /** Note that blocks should be consecutive (order does not matter)
      * and have the same style.
      */
    def merge(other: Block): Block = {
      // Assert that they're consecutive and have the same style
      assert(this.style == other.style)
      assert(this.to == other.from || this.from == other.to)

      val mergedFrom = Math.min(this.from, other.from)
      val mergedTo   = Math.max(this.to, other.to)

      Block(mergedFrom, mergedTo, this.style)
    }

    /** Block(from, to) => Block(from, where) | Block(where, where + 1) | Block(where + 1, to),
      * Note that this might result in empty blocks, but they should be collected by merge
      */
    def split(where: Int, style: Style): (Block, Block, Block) = {
      assert(where >= from)
      assert(where < to)
      val left   = this.copy(from, where)
      val middle = Block(where, style)
      val right  = this.copy(where + 1, to)

      (left, middle, right)
    }

    override def toString: String = f"Block{[$from,$to] - #${style.hashCode}}"

    private lazy val bounds: Bounds = {
      val textObject = new Text(text)
      textObject.setFont(Font.default)
      textObject.getBoundsInLocal
    }

    def width: Double = bounds.getWidth
    def height: Double = bounds.getHeight

    def text: String = sb.substring(from, to)

    override def draw(graphicsContext: GraphicsContext): Unit = {
      graphicsContext.font = Font.default
      graphicsContext.fill = style.background
      graphicsContext.fillRect(0, 0, width, height)

      graphicsContext.fill = style.foreground
      graphicsContext.fillText(text, 0, 0)
    }
  }
}


