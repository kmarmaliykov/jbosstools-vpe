package org.jboss.tools.vpe.browsersim.devtools;

import java.io.IOException;

import javafx.application.Platform;
import javafx.util.Callback;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

import com.sun.javafx.scene.web.Debugger;


public class DevToolsDebuggerServer {
	private static Debugger debugger;
	private static Server server;
	
	public static void startDebugServer(Debugger debugger) throws Exception {
		server = new Server(8087);
		debugger.setEnabled(true);
    	
    	final ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
    	servletHandler.addServlet(DevToolsWebSocketServlet.class, "/devtools/page/dtdb");
    	
    	ResourceHandler resourceHandler = new ResourceHandler();
    	resourceHandler.setDirectoriesListed(true);
    	resourceHandler.setResourceBase("./inspector-front-end");
    	
    	HandlerList handlerList = new HandlerList();
    	handlerList.setHandlers(new Handler[]{servletHandler, resourceHandler});
    	server.setHandler(handlerList);
        server.start();
        
		DevToolsDebuggerServer.debugger = debugger;
		debugger.setMessageCallback(new Callback<String, Void>() {
			@Override
			public Void call(String data) {
				DevToolsWebSocket mainSocket = (DevToolsWebSocket) servletHandler.getServletContext().getAttribute("org.jboss.tools.vpe.browsersim.devtools.DevToolsWebSocket");
				if (mainSocket != null) {
					try {
						mainSocket.sendMessage(data);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				return null;
			}
		});
	}
	
	public static void stopDebugServer() throws Exception {
		server.stop();
		server.join();
	}
	
	
	public static void sendMessageToBrowser(final String data) {
		 Platform.runLater(new Runnable() {// Display.asyncExec won't be successful here
	            @Override public void run() {
	            	debugger.sendMessage(data);
	            }
		 });
	}
}