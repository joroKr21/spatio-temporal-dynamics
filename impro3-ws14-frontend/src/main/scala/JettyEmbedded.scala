import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext

object JettyEmbedded extends App{
    val server = new Server()
    val connector = new SelectChannelConnector()
    connector.setPort(args(0).toInt)
    server.addConnector(connector)
    val context: WebAppContext = new WebAppContext(getClass.getClassLoader.getResource("webapp").toExternalForm, "/")
    context.setServer(server)
    server.setHandler(context)

    try {
      server.start()
      server.join()
    } catch {
      case e: Exception => {
        e.printStackTrace()
        System.exit(1)
      }
    }
}