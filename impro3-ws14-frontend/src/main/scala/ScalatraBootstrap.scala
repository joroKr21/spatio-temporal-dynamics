import javax.servlet.ServletContext
import org.scalatra.LifeCycle
import de.tu_berlin.impro3.frontend._

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {

    // Mount servlets.
    context.mount(new LocationController, "/api/location/*")
    context.mount(new HashtagController, "/api/hashtag/*")
    context.mount(new MainServlet, "/*")

  }
}