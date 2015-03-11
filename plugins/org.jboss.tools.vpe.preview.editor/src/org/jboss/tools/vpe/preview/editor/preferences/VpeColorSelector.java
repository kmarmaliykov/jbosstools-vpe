/*******************************************************************************
 * Copyright (c) 2012 Exadel, Inc. and Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.vpe.preview.editor.preferences;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.jboss.tools.vpe.preview.editor.Activator;

public class VpeColorSelector extends ColorSelector {

	private Text colorText; 
	
	public VpeColorSelector(Composite parent) {
		super(parent);
		colorText = new Text(parent, SWT.NONE);
		colorText.setEditable(false);
		colorText.setTextLimit(9);
	}
	
	@Override
	protected void updateColorImage() {
		super.updateColorImage();
		colorText.setText(rgbToString(getColorValue()));
	}

	public Text getColorText() {
		return colorText;
	}
	
	
	/**
	 * Method for convert RGB to String
	 *
	 * @param rgb RGB color
	 * @return String color
	 */
	public static String rgbToString(RGB rgb) {
		String colorStr = "#0000FF"; //$NON-NLS-1$
		if (rgb != null) {
			colorStr = "#" //$NON-NLS-1$
					+ (rgb.red < 10 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
					+ Integer.toHexString(rgb.red)
					+ (rgb.green < 10 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
					+ Integer.toHexString(rgb.green)
					+ "" //$NON-NLS-1$
					+ (rgb.blue < 10 ? "0" : "") //$NON-NLS-1$ //$NON-NLS-2$
					+ Integer.toHexString(rgb.blue);
			colorStr = colorStr.toUpperCase();
		} else {
			Activator.getDefault().logWarning("Cannot convert RGB color to string, because it is null"); //$NON-NLS-1$
		}
		return colorStr;
	}
}
