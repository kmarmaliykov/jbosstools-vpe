package org.jboss.tools.vpe.vpv.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.jboss.tools.vpe.vpv.Activator;
import org.jboss.tools.vpe.vpv.transform.VpvController;

public class VpvServer implements Runnable {

	private ServerSocket serverSocket;
	private VpvController vpvController;
	
	public VpvServer(VpvController vpvController) {
		this.vpvController = vpvController;
		new Thread(this).start();
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(0);
			while (true) {
				Socket clientSocket = serverSocket.accept();
				VpvSocketProcessor serverProcessor = new VpvSocketProcessor(clientSocket, vpvController);
				new Thread(serverProcessor).start();
			}
		} catch (IOException e) {
			Activator.logError(e);
		}
	}
	
	public void stop() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			Activator.logError(e);
		}
	}
	
	public int getPort() {
		return serverSocket.getLocalPort();
	}
}
