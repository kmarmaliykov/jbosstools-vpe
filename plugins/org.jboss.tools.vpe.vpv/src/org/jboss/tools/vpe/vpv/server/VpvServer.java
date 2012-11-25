package org.jboss.tools.vpe.vpv.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.jboss.tools.vpe.vpv.Activator;

public class VpvServer implements Runnable {

	private static VpvServer instance;

	private int port;
	private ServerSocket serverSocket;
	
	private VpvServer() {
	}

	public static VpvServer getInstance() {
		if (instance == null) {
			instance = new VpvServer();
		}

		return instance;
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(0);
			port = serverSocket.getLocalPort();
			System.out.println(port);
			while (true) {
				Socket clientSocket = serverSocket.accept();
				VpvSocketProcessor serverProcessor = new VpvSocketProcessor(clientSocket);
				new Thread(serverProcessor).start();
			}
		} catch (IOException e) {
			Activator.logError(e);
		}
	}
	
	public int getPort() {
		return serverSocket.getLocalPort();
	}
}
