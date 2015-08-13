package de.tu_berlin.impro3.frontend

import org.scalatra.ScalatraServlet
import org.scalatra.scalate.ScalateSupport
import org.fusesource.scalate.TemplateEngine
import org.fusesource.scalate.layout.DefaultLayoutStrategy

class MainServlet extends ScalatraServlet with ScalateSupport {
  get("/") {
    contentType = "text/html"

    val name = params.getOrElse("name", "world")

    ssp("locations.ssp", "name" -> name)
  }

  get("/hashtags") {
    contentType = "text/html"
    ssp("hashtags.ssp")
  }

  override protected def defaultTemplatePath: List[String] =
    List("/WEB-INF/views")

  override protected def createTemplateEngine(config: ConfigT) = {
    val engine = super.createTemplateEngine(config)
    engine.layoutStrategy = new DefaultLayoutStrategy(engine,
      TemplateEngine.templateTypes.map("/WEB-INF/layouts/default." + _): _*)
    engine.packagePrefix = "templates"
    engine
  }
}

