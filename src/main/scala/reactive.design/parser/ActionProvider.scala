package reactive.design.parser

import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

import monix.reactive.Observable
import monix.eval.Task
import monix.reactive.OverflowStrategy

object ActionProvider {
  def apply(inputStreamT: Task[InputStream]): Observable[Action] = {
    val task = inputStreamT
      .map { _.iterator() }
      .map { ActionParser(_) }
    Observable
      .fromIterator(task)
      .asyncBoundary(OverflowStrategy.Unbounded)
      .executeAsync
  }

  implicit class InputStreamOps(inputStream: InputStream) {
    val reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)

    def iterator(): Iterator[String] = new Iterator[String] {
      override def hasNext: Boolean = true
      override def next(): String   = reader.read().toChar.toString
    }
  }

}
