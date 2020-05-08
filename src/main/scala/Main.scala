import scalafx.application
import scalafx.application.JFXApp

object Main extends JFXApp {
  stage = new application.JFXApp.PrimaryStage {
    title.value = "Hello Stage"
  }

}
