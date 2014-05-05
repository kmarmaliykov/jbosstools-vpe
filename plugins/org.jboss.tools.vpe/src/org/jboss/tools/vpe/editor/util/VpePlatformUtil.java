package org.jboss.tools.vpe.editor.util;

import org.eclipse.core.runtime.Platform;

public class VpePlatformUtil {
	public static final String LOAD_XULRUNNER_ENGINE = "org.jboss.tools.vpe.engine.xulrunner"; //$NON-NLS-1$
	private static final String SWT_GTK3 = "SWT_GTK3"; //$NON-NLS-1$
	
	public static boolean isGTK3() {
		if (Platform.WS_GTK.equals(Platform.getWS())) {
			String gtk3 = System.getProperty(SWT_GTK3);
			if (gtk3 == null) {
				gtk3 = System.getenv(SWT_GTK3);
			}
			return !"0".equals(gtk3); //$NON-NLS-1$
		}
		return false;
	}
	
	public static boolean isXulrunnerEnabled() {
		return Boolean.valueOf(System.getProperty(LOAD_XULRUNNER_ENGINE));
	}
}
