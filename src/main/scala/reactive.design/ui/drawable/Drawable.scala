package reactive.design.ui.drawable

import java.awt.Color
import java.awt.image.BufferedImage

import javafx.embed.swing.SwingFXUtils
import javafx.geometry.Bounds
import javax.swing.JLabel
import org.scilab.forge.jlatexmath.{TeXConstants, TeXFormula}
import reactive.design.config.UIConfig
import reactive.design.data.TerminalLine
import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.paint.{Color => ColorFX}
import scalafx.scene.text.Text

import scala.Ordering.Double.TotalOrdering
import scala.language.implicitConversions

trait Drawable[A] {
  def draw(implicit graphicsContext: GraphicsContext): Unit
  def width: Double
  def height: Double
}

object Drawable {
  def draw[A: Drawable](graphicsContext: GraphicsContext): Unit =
    implicitly[Drawable[A]].draw(graphicsContext)

}

object DrawableInstances {
  implicit def colorConversion(color: ColorFX): Color = {
    val r = (color.red * 255).toInt
    val g = (color.green * 255).toInt
    val b = (color.blue * 255).toInt

    new Color(r, g, b)
  }

  implicit class TerminalLineOps(line: TerminalLine) extends Drawable[TerminalLine] {
    override def width: Double = line.blocksSeq().foldLeft(0.0)(_ + _.width)
    override def height: Double =
      if (line.isEmpty) UIConfig.defaultLineHeight
      else line.blocksSeq().map(_.height).max

    def draw(implicit gc: GraphicsContext): Unit = {
      if (!line.isEmpty) {
        gc.save()
        val H = height
        for (block <- line.blocksSeq()) {
          val h = (H - block.height) / 2
          gc.translate(0, h)
          block.draw
          gc.translate(block.width, -h)
        }
        gc.restore()
      }
    }

    def widthTo(column: Int): Double = {
      if (column >= line.len)
        return width
      val blocks = line.blocksSeq()

      var i            = 0
      var pos          = 0
      var runningWidth = 0.0
      while (pos <= column) {
        val block = blocks(i)

        pos += block.len
        runningWidth += block.widthTo(column)
        i += 1
      }

      runningWidth
    }

  }

  implicit class BlockOps(block: TerminalLine#Block) extends Drawable[TerminalLine#Block] {
    private def style = block.style
    private lazy val formula =
      try {
        Some(new TeXFormula(block.text))
      } catch {
        case _: Exception => None
      }

    private lazy val icon =
      formula.map(_.createTeXIcon(TeXConstants.STYLE_DISPLAY, 20))

    private lazy val bounds: Bounds = {
      val textObject = new Text(block.text)
      textObject.setFont(UIConfig.font)
      textObject.getBoundsInLocal
    }

    def width: Double =
      (style.latexRendering, icon) match {
        case (true, Some(icon)) => icon.getIconWidth
        case (_, _)             => bounds.getWidth
      }

    def height: Double =
      (style.latexRendering, icon) match {
        case (true, Some(icon)) => icon.getIconHeight
        case (_, _)             => bounds.getHeight
      }

    def widthTo(col: Int): Double = {
      if (col >= block.to)
        return width

      val until   = col - block.from
      val textObj = new Text(block.text.substring(0, until))

      textObj.setFont(UIConfig.font)
      textObj.getBoundsInLocal.getWidth
    }

    def draw(implicit gc: GraphicsContext): Unit = {
      gc.fill = style.background
      gc.fillRect(0, 0, width, height)
      (style.latexRendering, icon) match {
        case (true, Some(icon)) =>
          val jl = new JLabel()
          val img =
            new BufferedImage(icon.getIconWidth, icon.getIconHeight, BufferedImage.TYPE_INT_RGB)
          val g2 = img.createGraphics()
          jl.setForeground(style.foreground)
          icon.paintIcon(jl, g2, 0, 0)
          gc.drawImage(SwingFXUtils.toFXImage(img, null), 0, 0)
        case (true, None) =>
          gc.fill = ColorFX.Red
          gc.fillText(block.text, 0, 0)
        case (false, _) =>
          gc.fill = style.foreground
          gc.fillText(block.text, 0, 0)
      }
    }

  }

}
