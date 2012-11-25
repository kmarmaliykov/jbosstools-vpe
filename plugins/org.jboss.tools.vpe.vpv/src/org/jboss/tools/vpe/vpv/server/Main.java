package org.jboss.tools.vpe.vpv.server;

import org.jboss.tools.vpe.vpv.transform.VpvController;

public class Main {

	public static void main(String[] args) {
		VpvController vpvController = new VpvController();
		VpvServer server = new VpvServer(vpvController);
	}
}
