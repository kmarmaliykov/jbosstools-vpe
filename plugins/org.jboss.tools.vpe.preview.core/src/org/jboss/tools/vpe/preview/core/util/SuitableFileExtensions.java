/*******************************************************************************
 * Copyright (c) 2013-2014 Red Hat, Inc.
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
public enum SuitableFileExtensions {
	HTML("html"), //$NON-NLS-1$
	HTM("htm"), //$NON-NLS-1$
	XHTML("xhtml"), //$NON-NLS-1$
	CSS("css"), //$NON-NLS-1$
	JS("js"), //$NON-NLS-1$
	JSP("jsp"); //$NON-NLS-1$

	private final String value;

	private SuitableFileExtensions(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static boolean contains(String fileExtension) {
		for (SuitableFileExtensions extension : SuitableFileExtensions.values()) {
			if (extension.value.equals(fileExtension)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isCssOrJs(String fileExtension) {
		if (JS.getValue().equals(fileExtension) || CSS.getValue().equals(fileExtension)) {
			return true;
		}
		return false;
	}
	
	public static boolean isHTML(String fileExtension) {
		if (HTML.getValue().equals(fileExtension) || HTM.getValue().equals(fileExtension)) {
			return true;
		}
		return false;
	}

}