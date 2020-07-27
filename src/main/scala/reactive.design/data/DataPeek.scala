package reactive.design.data

import reactive.design.config.UIConfig
import reactive.design.ui.drawable.DrawableInstances._

final case class DataPeek
  (private val buffer: LinesBuffer,
   private val cursor: CursorDataPeek ) {

  def cursorCoordinates(): (Double, Double) = {
    val cursorLine = buffer._lines(cursor.y)
    var cursorY    = 0.0
    var (i, _)     = lastLinesIndices()

    while (i < cursor.y) {
      cursorY += buffer._lines(i).height
      i += 1
    }
    val cursorX = cursorLine.widthTo(cursor.x)

    (cursorX, cursorY)
  }

  /**  Returns seq of lines fitting the screen */
  def recent(): Seq[TerminalLine] = {
    val (from, until) = lastLinesIndices()
    buffer._lines.slice(from, until).toSeq
  }

  private def lastLinesIndices(): (Int, Int) = {
    var i             = buffer.linesCount - 1
    var runningHeight = 0.0
    val maxH          = UIConfig.screenHeight - 25

    while (i >= 0 && runningHeight < maxH) {
      runningHeight += buffer._lines(i).height
      i -= 1
    }

    if (i < 0) {
      i += 1
    }

    if (runningHeight > maxH) {
      runningHeight -= buffer._lines(i).height
      i += 1
    }

    (i, buffer.linesCount)
  }
}
