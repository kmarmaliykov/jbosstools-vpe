package org.jboss.tools.vpe.preview.core.exceptions;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.jboss.tools.vpe.preview.core.exceptions.messages"; //$NON-NLS-1$
	public static String NO_ENGINE_ERROR;
	public static String MOZILLA_LOADING_ERROR;
	public static String MOZILLA_LOADING_ERROR_LINK_TEXT;
	public static String MOZILLA_LOADING_ERROR_LINK;
	public static String CANNOT_SHOW_EXTERNAL_FILE;
	public static String EDITOR_DOES_NOT_SUPPORT_DOM_MODEL;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
