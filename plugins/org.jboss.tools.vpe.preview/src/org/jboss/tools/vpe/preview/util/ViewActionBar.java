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
package org.jboss.tools.vpe.preview.util;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.browser.Browser;
import org.eclipse.ui.IEditorPart;
import org.jboss.tools.vpe.preview.core.util.ActionBar;
import org.jboss.tools.vpe.preview.core.util.EditorUtil;
import org.jboss.tools.vpe.preview.core.util.NavigationUtil;
import org.jboss.tools.vpe.preview.core.util.PlatformUtil;
import org.jboss.tools.vpe.preview.core.util.SuitableFileExtensions;
import org.jboss.tools.vpe.preview.view.VpvView;

/**
 * @author Ilya Buziuk (ibuziuk)
 */
public class ViewActionBar extends ActionBar {
	
	private VpvView view;

	public ViewActionBar(Browser browser, IPreferenceStore preferences, VpvView view) {
		super(browser, preferences);
		this.view = view;
	}
	
	@Override
	protected void refresh(Browser browser) {
		String url = browser.getUrl();
		if (PlatformUtil.isWindows()) {
			IEditorPart currentEditor = view.getCurrentEditor();
			if (currentEditor != null) {
				String ext = EditorUtil.getFileExtensionFromEditor(currentEditor);
				if (SuitableFileExtensions.isCssOrJs(ext)) {
					browser.refresh(); // Files are saved - need to perform refresh
				} else {
					browser.setUrl(NavigationUtil.fixUrl(browser));
				}
			}
		} else {
			browser.setUrl(url);
		}
	}

}
