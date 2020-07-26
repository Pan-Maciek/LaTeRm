package reactive.design.data

import gui.Style

sealed trait UIEvent

final case class UIUpdate(lines: LinesBuffer, cursor: CursorDataPeek) extends UIEvent
