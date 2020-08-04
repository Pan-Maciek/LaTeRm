package reactive.design.data

sealed trait UIEvent
final case class UIUpdate(peek: DataPeek) extends UIEvent
