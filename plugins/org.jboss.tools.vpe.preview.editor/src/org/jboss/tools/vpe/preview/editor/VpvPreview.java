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

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.jboss.tools.vpe.editor.mozilla.listener.EditorLoadWindowListener;

/**
 * @author Konstantin Marmalyukov (kmarmaliykov)
 */
public class VpvPreview extends VpvEditor {
	private EditorLoadWindowListener editorLoadWindowListener;

	public VpvPreview(IEditorPart sourceEditor) {
		this.sourceEditor = sourceEditor;
	}
	
	@Override
	public void createPartControl(Composite parent) {
		try {
			Browser browser = new Browser(parent, SWT.NONE);
			browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			setBrowser(browser);
			if (editorLoadWindowListener != null) {
				editorLoadWindowListener.load();
			}
		} catch (Throwable t) {
			//cannot create browser. show error message then
			errorWrapper.showError(parent, t);
		}
	}
	
	public void load() {
		reload();
	}
	
	@Override
	public void setEditorLoadWindowListener(EditorLoadWindowListener listener) {
		editorLoadWindowListener = listener;
	}
	
	@Override
	public void dispose() {
		setEditorLoadWindowListener(null);
		super.dispose();
	}
}
