package org.eclipse.swt.browser;

import java.lang.reflect.Field;

import org.eclipse.core.runtime.Platform;

public class BrowserInitializer {

	private static final String PROPERTY_DEFAULTTYPE = "org.eclipse.swt.browser.DefaultType"; //$NON-NLS-1$
	private static final String SWT_GTK3 = "SWT_GTK3"; //$NON-NLS-1$

	public static boolean isGTK3() {
		if (Platform.WS_GTK.equals(Platform.getWS())) {
			try {
				Class<?> clazz = Class.forName("org.eclipse.swt.internal.gtk.OS"); //$NON-NLS-1$
				Field field = clazz.getDeclaredField("GTK3"); //$NON-NLS-1$
				boolean gtk3 = field.getBoolean(field);
				return gtk3;
			} catch (ClassNotFoundException e) {
				return isGTK3Env();
			} catch (NoSuchFieldException e) {
				return false;
			} catch (SecurityException e) {
				return isGTK3Env();
			} catch (IllegalArgumentException e) {
				return isGTK3Env();
			} catch (IllegalAccessException e) {
				return isGTK3Env();
			}
		}
		return false;
	}

	private static boolean isGTK3Env() {
		String gtk3 = System.getProperty(SWT_GTK3);
		if (gtk3 == null) {
			gtk3 = System.getenv(SWT_GTK3);
		}
		return !"0".equals(gtk3); //$NON-NLS-1$
	}
	
	static {
		/* Under Linux instantiation of WebKit should be avoided,
		 * because WebKit and XULRunner running simultaneously
		 * may cause native errors.
		 * 
		 * Also see JBIDE-9144, JBIDE-10185 and JBDS-2900. 	 */
		if (Platform.OS_LINUX.equals(Platform.getOS())) {
			String defaultType = System.getProperty(PROPERTY_DEFAULTTYPE);
			if (defaultType == null) {
				//loadxulrunner flag on linux must disable only xulrunner engine. But we can use WebKit for Preview
				defaultType = isGTK3() || !XULRunnerInitializer.EMBEDDED_XULRUNNER_ENABLED ? "webkit" : "mozilla"; //$NON-NLS-1$ //$NON-NLS-2$
				System.setProperty(PROPERTY_DEFAULTTYPE, defaultType);
			}
		}
	}
}
