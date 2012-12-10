package org.jboss.tools.vpe.vpv.views;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.jboss.tools.vpe.vpv.views.messages"; //$NON-NLS-1$
	public static String VpvView_REFRESH;
	public static String VpvView_OPEN_IN_DEFAULT_BROWSER;
	public static String VpvView_ERROR;
	public static String VpvView_COULD_NOT_OPEN_DEFAULT_BROWSER;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
