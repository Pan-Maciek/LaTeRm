package gui.drawable

import java.awt.Color
import java.awt.image.BufferedImage

import gui.data.TerminalLine
import javafx.embed.swing.SwingFXUtils
import javafx.geometry.Bounds
import javax.swing.JLabel
import org.scilab.forge.jlatexmath.{TeXConstants, TeXFormula}
import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.text.{Font, Text}

import scala.Ordering.Double.TotalOrdering

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
  implicit class TerminalLineOps(line: TerminalLine) extends Drawable[TerminalLine] {
    override def width: Double  = line.blocksSeq().foldLeft(0.0)(_ + _.width)
    override def height: Double = line.blocksSeq().map(_.height).max

    def draw(implicit gc: GraphicsContext): Unit = {
      if (!line.isEmpty) {
        val H = height
        gc.save()
        for (block <- line.blocksSeq()) {
          val h = (H - block.height) / 2
          gc.translate(0, h)
          block.draw
          gc.translate(block.width, -h)
        }
        gc.restore()
      }
    }

  }

  implicit class BlockOps(block: TerminalLine#Block) extends Drawable[TerminalLine#Block] {
    private def style = block.style

    private lazy val bounds: Bounds = {
      val textObject = new Text(block.text)
      textObject.setFont(Font.default)
      textObject.getBoundsInLocal
    }

    private lazy val formula = new TeXFormula(block.text)
    private lazy val icon    = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 20)

    def width: Double  = if (style.latexRendering) icon.getIconWidth else bounds.getWidth
    def height: Double = if (style.latexRendering) icon.getIconHeight else bounds.getHeight

    def draw(implicit gc: GraphicsContext): Unit = {
      gc.font = Font.default
      gc.fill = style.background
      gc.fillRect(0, 0, width, height)
      if (style.latexRendering) {
        val jl = new JLabel()
        val img = new BufferedImage(icon.getIconWidth, icon.getIconHeight, BufferedImage.TYPE_INT_RGB)
        val g2 = img.createGraphics()
        jl.setForeground(new Color((style.foreground.getRed * 255).toInt, (style.foreground.getGreen * 255).toInt, (style.foreground.getBlue * 255).toInt))
        icon.paintIcon(jl, g2, 0, 0)
        gc.drawImage(SwingFXUtils.toFXImage(img, null), 0, 0)
      } else {
        gc.fill = style.foreground
        gc.fillText(block.text, 0, 0)
      }
    }

  }

}
