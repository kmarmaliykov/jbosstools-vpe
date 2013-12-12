package org.jboss.tools.vpe.browsersim.devtools;


import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

public class DevToolsWebSocketServlet extends WebSocketServlet {

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
		return new DevToolsWebSocket(getServletContext());
	}
}
