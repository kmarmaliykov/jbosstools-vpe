/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.vpe.preview.core.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.jboss.tools.vpe.preview.core.Activator;
import org.jboss.tools.vpe.preview.core.transform.VpvController;

/**
 * @author Yahor Radtsevich (yradtsevich)
 * @author Ilya Buziuk (ibuziuk)
 */
public class VpvServer implements Runnable {

	private ServerSocket serverSocket;
	private VpvController vpvController;
	
	boolean socketIsAboutToBeClosed = false;
	
	public VpvServer(VpvController vpvController) {
		this.vpvController = vpvController;
		new Thread(this).start();
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(0, 0, InetAddress.getByName(HttpConstants.LOCALHOST)); 
			while (true) {
				Socket clientSocket = serverSocket.accept();
				VpvSocketProcessor serverProcessor = new VpvSocketProcessor(clientSocket, vpvController);
				new Thread(serverProcessor).start();
			}
		} catch (SocketException e) {
			if (!socketIsAboutToBeClosed) {
				Activator.logError(e);
			}
		} catch (IOException e) {
			Activator.logError(e);
		}
	}
	
	public void stop() {
		try {
			socketIsAboutToBeClosed = true;
			serverSocket.close();
		} catch (IOException e) {
			Activator.logError(e);
		}
	}
	
	public int getPort() {
		return serverSocket.getLocalPort();
	}
}
