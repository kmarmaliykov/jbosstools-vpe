/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.vpe.preview.core.util;

/**
 * @author Ilya Buziuk (ibuziuk)
 */
public final class PlatformUtil {
	private static final String MAC = "mac"; //$NON-NLS-1$
	private static final String DARWIN = "darwin"; //$NON-NLS-1$
	private static final String WIN = "win"; //$NON-NLS-1$
	private static final String LINUX = "nux"; //$NON-NLS-1$
	private static OS detectedOs;

	private PlatformUtil() {
	}

	public static OS getOs() {
		if (detectedOs == null) {
			String currentOs = System.getProperty("os.name", "generic").toLowerCase(); //$NON-NLS-1$ //$NON-NLS-2$
			if ((currentOs.indexOf(MAC) >= 0) || (currentOs.indexOf(DARWIN) >= 0)) {
				detectedOs = OS.MACOS;
			} else if (currentOs.indexOf(WIN) >= 0) {
				detectedOs = OS.WINDOWS;
			} else if (currentOs.indexOf(LINUX) >= 0) {
				detectedOs = OS.LINUX;
			} else {
				detectedOs = OS.OTHER;
			}
		}
		return detectedOs;
	}
	
	public static boolean isWindows() {
		return OS.WINDOWS.equals(getOs());
	}
	
	public static boolean isMacOS() {
		return OS.MACOS.equals(getOs());
	}
}
