/******************************************************************************* 
 * Copyright (c) 2015 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.jboss.tools.vpe.preview.editor.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.Browser;
import org.jboss.tools.jst.web.kb.IBrowserDataProvider;
import org.jboss.tools.jst.web.kb.IPageContext;
import org.jboss.tools.jst.web.kb.internal.BrowserDataProviderManager;
import org.jboss.tools.jst.web.ui.WebUiPlugin;
import org.jboss.tools.jst.web.ui.internal.editor.editor.IVisualEditor;
import org.jboss.tools.jst.web.ui.internal.editor.jspeditor.JSPMultiPageEditor;
import org.jboss.tools.vpe.preview.core.transform.VpvDomBuilder;
import org.jboss.tools.vpe.preview.editor.Activator;
import org.jboss.tools.vpe.preview.editor.VpvEditor;

/**
 * @author Alexey Kazakov
 */
public class BrowserDataProvider implements IBrowserDataProvider {

	/*
	 * (non-Javadoc)
	 * @see org.jboss.tools.jst.web.kb.IBrowserDataProvider#evaluate(java.lang.String)
	 */
	@Override
	public Object evaluate(String js, IPageContext pageContext) {
		JSPMultiPageEditor editor = WebUiPlugin.getActiveMultiPageEditor();
		if(editor!=null) {
			IVisualEditor ve = editor.getVisualEditor();
			if(ve!=null) {
				ve.initBrowser();
				Object obj = ve.getPreviewWebBrowser();
				if(obj instanceof VpvEditor) {
					VpvEditor vpv = (VpvEditor)obj;
					Browser browser = vpv.getBrowser();
					if(browser!=null && !browser.isDisposed()) {
						Map<String, String> parameters = new HashMap<String, String>();
						parameters.put(BrowserDataProviderManager.ELEMENT_ID_PARAM, VpvDomBuilder.ATTR_VPV_ID);
						Long id = vpv.getCurrentSelectedElementId();
						if(id!=null) {
							parameters.put(BrowserDataProviderManager.ELEMENT_ID_VALUE_PARAM, id.toString());
							try {
								return browser.evaluate(BrowserDataProviderManager.format(js, parameters));
							} catch(SWTException e) {
								if(e.code != SWT.ERROR_FAILED_EVALUATE && e.code != SWT.ERROR_INVALID_RETURN_VALUE) {
									Activator.logError(e);
								}
							}
						}
					}
				}
			}
		}
		return "";
	}
}