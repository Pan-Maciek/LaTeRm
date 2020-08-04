package reactive.design.parser

import java.io.{InputStream, InputStreamReader}
import java.nio.charset.StandardCharsets

import monix.eval.Task
import monix.reactive.{Observable, OverflowStrategy}

object ActionProvider {
  def apply(inputStream: InputStream): Observable[Action] = {
    val task = Task { inputStream }
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
