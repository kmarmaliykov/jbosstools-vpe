package org.jboss.tools.vpe.vpv.server;

import java.io.ObjectInputStream.GetField;

import org.jboss.tools.vpe.vpv.transform.VpvController;
import org.jboss.tools.vpe.vpv.transform.VpvDomBuilder;
import org.jboss.tools.vpe.vpv.transform.VpvTemplateProvider;

public class Main {

	public static void main(String[] args) {
		VpvTemplateProvider vpvTemplateProvider = new VpvTemplateProvider();
		VpvDomBuilder vpvDomBuilder = new VpvDomBuilder(vpvTemplateProvider);
		VpvController vpvController = new VpvController(vpvDomBuilder);
		VpvServer server = new VpvServer(vpvController);
	}
}
