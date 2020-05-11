package gui.data

import gui.Style
import gui.Style._
import scala.collection.mutable
import scala.collection.Searching._
import scala.collection.Searching
import scala.math.signum

trait Functionality {
  def write(row: Int, column: Int, v: Char, style: Style): Unit
}

final case class Data() {}

final class Line() {
  import Block._
  private val sb     = new StringBuilder
  private val blocks = mutable.ArrayBuffer(Block.empty)

  def getText(): String = sb.toString()
  def len(): Int        = sb.size

  def write(column: Int, v: Char, style: Style): Unit = {
    assert(column >= 0)
    signum(column compare len) match {
      case 0  => sb += v
      case -1 => sb.insert(column, v)
      case _  => throw new AssertionError("Trying to write beyond line's length!")
    }

    insert(column, v, style)
  }

  private def insert(column: Int, v: Char, style: Style): Unit = {
    val i     = findBlock(len)
    val block = blocks(i)

    val (shiftFrom, merge) =
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
        // split block and then merge if possible
        val (left, right) = block.split(column)
        val middle        = Block(column, v, style)
        val iterable      = List(middle, right)

        blocks(i) = left
        blocks.insertAll(i + 1, iterable)
        (i + 2, true)
      }

    // clean up
    shiftBlocks(shiftFrom)
    if (merge)
      mergeAt(shiftFrom - 1)
  }

  /** Merges starting with block at `i` */
  def mergeAt(i: Int): Unit = {
    var block  = blocks(i)
    var blockI = i

    // merge left
    var leftI = blockI - 1
    while (leftI >= 0 && blocks(leftI).style == block.style) {
      val left = blocks(leftI)
      block = block.merge(left)

      blocks.remove(leftI)
      blockI -= 1
      leftI -= 1

      blocks(blockI) = block
    }

    // merge right
    var rightI = blockI + 1
    while (rightI < blocks.length && blocks(rightI).style == block.style) {
      val right = blocks(leftI)
      block = block.merge(right)

      blocks.remove(rightI)
      blocks(blockI) = block
    }
  }

  private def shiftBlocks(from: Int): Unit = {
    for (i     <- from until blocks.length;
         block = blocks(i))
      blocks(i) = block.shiftRight(1)
  }

  def findBlock(column: Int): Int = {
    assert(column <= len())

    blocks.search(Block(column, column, Style.default)) match {
      case Found(i)          => i
      case InsertionPoint(i) => i
    }
  }
}

object Block {
  implicit def ordering[A <: Block]: Ordering[Block] = Ordering.by(_.to)

  /** singleton block holding one character */
  def apply(where: Int, style: Style): Block = Block(where, where + 1, style)

  /** should be used only when creating new line! */
  val empty: Block = Block(0, 0, Style.default)
}

final case class Block(from: Int, to: Int, style: Style) {
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

  /** Block(from, to) => Block(from, where) | Block(where, to) */
  def split(where: Int): (Block, Block) = {
    assert(where > from)
    assert(where < to)

    val left  = this.copy(from, where)
    val right = this.copy(where, to)

    (left, right)
  }
}
