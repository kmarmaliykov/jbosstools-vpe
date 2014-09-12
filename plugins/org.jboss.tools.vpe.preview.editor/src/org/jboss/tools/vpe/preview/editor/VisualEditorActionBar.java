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
package org.jboss.tools.vpe.preview.editor;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.browser.Browser;
import org.jboss.tools.jst.web.ui.internal.editor.editor.IVisualController;
import org.jboss.tools.vpe.preview.core.util.ActionBar;
import org.jboss.tools.vpe.preview.core.util.NavigationUtil;

/**
 * @author Konstantin Marmalyukov (kmarmaliykov)
 */

public class VisualEditorActionBar extends ActionBar{
	private IVisualController controller;
	
	public VisualEditorActionBar(Browser browser1, IPreferenceStore preferences, IVisualController controller) {
		super(browser1, preferences);
		this.controller = controller;
	}
	
	@Override
	protected void refresh(Browser browser) {
		if (controller.isVisualEditorVisible()) {
			browser.setUrl(NavigationUtil.fixUrl(browser));
		}
	}
}
