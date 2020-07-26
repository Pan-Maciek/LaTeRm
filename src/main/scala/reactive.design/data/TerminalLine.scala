package reactive.design.data

import scala.collection.Searching._
import scala.collection.mutable
import scala.language.implicitConversions

final class TerminalLine {

  private val sb     = new StringBuilder
  private val blocks = mutable.ArrayBuffer(Block.empty)

  def len: Int                  = sb.size
  def getText: String           = sb.toString()
  def isEmpty: Boolean          = sb.isEmpty
  def charAt(column: Int): Char = sb.charAt(column)
  def blocksSize(): Int         = blocks.size
  def blocksSeq(): Seq[Block]   = blocks.toSeq

  /** Replaces character at given column.
    * If column exceeds len, then it appends spaces (with default style) until specified column is reached,
    * at which point it inserts given character. */
  def write(column: Int, v: Char, style: Style): Unit = {
    assert(column >= 0)
    val mergeI =
      if (column >= len) {
        // in case line is shorter append
        val missing = column - len

        blocks.append(Block(len, column, Style.default))
        sb.append(" " * missing)

        blocks.append(Block(len, style))
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

  def deleteAt(column: Int): Unit = {
    assert(column >= 0)
    assert(column < len)

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

  /** Deletes characters up to specified column (exclusive) */
  def deleteTo(column: Int): Unit = {
    val blockI = findBlock(column)
    sb.delete(0, column)

    val block = blocks(blockI)
    blocks.dropInPlace(blockI)
    val shiftFrom =
      if (block.to == column) {
        blocks.dropInPlace(1); 0
      } else {
        val to       = block.to - column
        val adjusted = block.copy(from = 0, to = to)
        blocks(0) = adjusted; 1
      }

    val shiftBy = -column
    shiftBlocks(shiftFrom, -column)

    if (blocksSize() == 0)
      blocks.append(Block.empty)
  }

  /** Deletes characters from (inclusive) till the end (len()) */
  def deleteFrom(column: Int): Unit = {
    val blockI   = findBlock(column)
    val deleteNo = blocksSize() - blockI - 1
    sb.delete(column, len)

    blocks.dropRightInPlace(deleteNo)
    val block    = blocks(blockI)
    val adjusted = block.copy(to = column)

    if (adjusted.len == 0) {
      if (blocksSize() == 1) {
        blocks(0) = Block.empty
      } else {
        blocks.dropRightInPlace(1)
      }
    } else
      blocks(blockI) = adjusted
  }

  /** Deletes all characters in line */
  def clearLine(): Unit = deleteFrom(0)

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
    for (i     <- from until blocks.length;
         block = blocks(i))
      blocks(i) = block.shiftRight(by)
  }

  private def findBlock(column: Int): Int = {
    assert(column <= len)

    blocks.search(Block(column, column + 1, Style.default)) match {
      case Found(i)                               => i
      case InsertionPoint(i) if i >= blocksSize() => blocksSize() - 1
      case InsertionPoint(i)                      => i
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

  object Block {

    /** used by bin search */
    implicit def ordering[A <: Block]: Ordering[Block] = Ordering.by(_.to - 1)

    /** singleton block holding one character */
    def apply(where: Int, style: Style): Block = Block(where, where + 1, style)

    /** should be used only when creating new line! */
    val empty: Block = Block(0, 0, Style.default)
  }

  case class Block(from: Int, to: Int, style: Style) {
    def len(): Int   = to - from
    def text: String = sb.substring(from, to).replace('\n', ' ')

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
  }
}
