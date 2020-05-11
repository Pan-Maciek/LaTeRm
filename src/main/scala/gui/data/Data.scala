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

  }

  private def insert(column: Int, v: Char, style: Style): Unit = {
    val i     = findBlock(len)
    val block = blocks(i)

    // Todo shift all blocks to right!
    if (block.to == column) {
      // Check neighours for merge
      val shiftFrom =
        if (block.style == style) {
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
      shiftBlocks(shiftFrom)
    } else {
      // split block and then merge if possible
      val (left, right) = block.split(column)
      blocks(i) = left
    }
  }

  private def shiftBlocks(from: Int): Unit = {
    for (i     <- from until blocks.length;
         block = blocks(i))
      blocks(i) = block.shiftRight(1)
  }

  // appending at the end
  private def append(v: Char, style: Style): Unit = {
    val i     = findBlock(len)
    val block = blocks(i)

    if (block.style == style) {
      blocks(i) = block.copy(to = block.to + 1)
    } else {
      val newBlock = Block(block.to, style)
      blocks += newBlock
    }
  }

  private def appendBlock(v: Char, style: Style, prevI: Int) = {
    if (block.style == style) {
      blocks(i) = block.copy(to = block.to + 1)
    } else {
      val newBlock = Block(block.to, style)
      blocks += newBlock
    }

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

  // singleton block holding one character
  def apply(where: Int, style: Style): Block = Block(where, where + 1, style)

  // should be used only when creating new line!
  val empty: Block = Block(0, 0, Style.default)
}

final case class Block(from: Int, to: Int, style: Style) {
  def len(): Int = to - from

  def shiftRight(by: Int = 1): Block = {
    val shiftedFrom = this.from + by
    val shiftedTo   = this.to + by

    this.copy(shiftedFrom, shiftedTo)
  }

  // Block(from, to) => Block(from, where) | Block(where, to)
  def split(where: Int): (Block, Block) = {
    assert(where > from)
    assert(where < to)

    val left  = this.copy(from, where)
    val right = this.copy(where, to)

    (left, right)
  }
}
