package gui.drawable

import gui.Style
import javafx.embed.swing.SwingFXUtils
import javafx.geometry.Bounds
import javax.swing.JLabel
import org.scilab.forge.jlatexmath.{TeXConstants, TeXFormula}
import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.text.{Font, Text}
import gui.data.TerminalLine
import java.awt.image.BufferedImage
import java.awt.Color

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
    override def width: Double  = 0  // blocks.foldLeft(0.0)(_ + _.width)
    override def height: Double = 15 // blocks.map(_.height).max

    def draw(implicit gc: GraphicsContext): Unit = {
      synchronized {
        if (!line.isEmpty) {
          gc.save()
          for (block <- line.blocksSeq()) {
            if (block.len > 0) {
              block.draw
              gc.translate(block.width, 0)
            }
          }
          gc.restore()
        }
      }
    }

  }

  implicit class BlockOps(block: TerminalLine#Block) extends Drawable[TerminalLine#Block] {
    private def style = block.style

    def width: Double  = if (style.latexRendering) icon.getIconWidth else bounds.getWidth
    def height: Double = if (style.latexRendering) icon.getIconHeight else bounds.getHeight

    private lazy val bounds: Bounds = {
      val textObject = new Text(block.text)
      textObject.setFont(Font.default)
      textObject.getBoundsInLocal
    }

    private lazy val formula = new TeXFormula(block.text)
    private lazy val icon    = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 20)

    def draw(implicit gc: GraphicsContext): Unit = {
      gc.font = Font.default
      gc.fill = block.style.background
      gc.fillRect(0, 0, width, height)
      if (block.style.latexRendering) {
        val jl = new JLabel()
        val img =
          new BufferedImage(icon.getIconWidth, icon.getIconHeight, BufferedImage.TYPE_INT_RGB)
        val g2 = img.createGraphics()
//        jl.setForeground(colorConversion(style.foreground))
        jl.setForeground(Color.yellow)
        icon.paintIcon(jl, g2, 0, 0)
        gc.drawImage(SwingFXUtils.toFXImage(img, null), 0, 0)
      } else {
        gc.fill = block.style.foreground
        gc.fillText(block.text, 0, 0)
      }
    }

  }

}
