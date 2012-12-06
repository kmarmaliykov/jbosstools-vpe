package org.jboss.tools.vpe.vpv.views;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.jboss.tools.vpe.vpv.views.messages"; //$NON-NLS-1$
	public static String VpvView_REFRESH;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
