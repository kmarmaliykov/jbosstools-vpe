package org.jboss.tools.vpe.vpv.server;

public class Main {

	public static void main(String[] args) {
		VpvServer server = VpvServer.getInstance();
		new Thread(server).start();
	}
}
