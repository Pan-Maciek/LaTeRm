package gui.drawable

import java.awt.Color
import java.awt.image.BufferedImage

import config.UiConfig
import javafx.embed.swing.SwingFXUtils
import javafx.geometry.Bounds
import javax.swing.JLabel
import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.text.{Font, Text}
import scalafx.scene.paint.{Color => ColorFX}
import org.scilab.forge.jlatexmath.{TeXConstants, TeXFormula}

import scala.Ordering.Double.TotalOrdering
import scala.language.implicitConversions
import gui.data.TerminalLine

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
    override def width: Double  = line.blocksSeq().foldLeft(0.0)(_ + _.width)
    override def height: Double =
      if (line.isEmpty) UiConfig.DefaultLineHeight
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
      // for some reason cursor can be at line.len??
      if (column >= line.len) {
        return width
      }

      val blocks = line.blocksSeq()

      var i     = 0
      var x     = 0.0
      var block = blocks(i)
      while (!(block.from <= column && block.to < column)) {
        x += block.width
        i += 1
        block = blocks(i)
      }

      x + block.widthTo(column - block.from)
    }

  }

  implicit class BlockOps(block: TerminalLine#Block) extends Drawable[TerminalLine#Block] {
    private def style = block.style

    private lazy val bounds: Bounds = {
      val textObject = new Text(block.text)
      textObject.setFont(UiConfig.font)
      textObject.getBoundsInLocal
    }

    private lazy val formula = new TeXFormula(block.text)
    private lazy val icon    = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 20)

    def width: Double  = if (style.latexRendering) icon.getIconWidth else bounds.getWidth
    def height: Double = if (style.latexRendering) icon.getIconHeight else bounds.getHeight

    def widthTo(i: Int): Double = width / block.len() * i

    def draw(implicit gc: GraphicsContext): Unit = {
      gc.fill = style.background
      gc.fillRect(0, 0, width, height)
      if (style.latexRendering) {
        val jl = new JLabel()
        val img =
          new BufferedImage(icon.getIconWidth, icon.getIconHeight, BufferedImage.TYPE_INT_RGB)
        val g2 = img.createGraphics()
        jl.setForeground(style.foreground)
        icon.paintIcon(jl, g2, 0, 0)
        gc.drawImage(SwingFXUtils.toFXImage(img, null), 0, 0)
      } else {
        gc.fill = style.foreground
        gc.fillText(block.text, 0, 0)
      }
    }

  }

}
