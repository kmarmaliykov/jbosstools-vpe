package org.jboss.tools.vpe.vpv.server;

import org.jboss.tools.vpe.vpv.transform.VpvController;
import org.jboss.tools.vpe.vpv.transform.VpvDomBuilder;
import org.jboss.tools.vpe.vpv.transform.VpvTemplateProvider;
import org.jboss.tools.vpe.vpv.transform.VpvVisualModelHolderRegistry;

public class Main {

	public static void main(String[] args) {
		VpvTemplateProvider vpvTemplateProvider = new VpvTemplateProvider();
		VpvDomBuilder vpvDomBuilder = new VpvDomBuilder(vpvTemplateProvider);
		VpvVisualModelHolderRegistry visualModelHolderRegistry = new VpvVisualModelHolderRegistry();
		VpvController vpvController = new VpvController(vpvDomBuilder, visualModelHolderRegistry);
		VpvServer server = new VpvServer(vpvController);
	}
}
