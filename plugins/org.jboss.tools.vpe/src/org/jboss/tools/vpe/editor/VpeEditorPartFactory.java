/*******************************************************************************
 * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
 ******************************************************************************/ 
package org.jboss.tools.vpe.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.jboss.tools.jst.web.ui.internal.editor.bundle.BundleMap;
import org.jboss.tools.jst.web.ui.internal.editor.editor.IVisualEditor;
import org.jboss.tools.jst.web.ui.internal.editor.editor.IVisualEditorFactory;
import org.jboss.tools.vpe.xulrunner.browser.XulRunnerBrowser;

public class VpeEditorPartFactory implements IVisualEditorFactory {
	public static final String LOAD_XULRUNNER_ENGINE = "org.jboss.tools.vpe.engine.xulrunner"; //$NON-NLS-1$
	private static final String SWT_GTK3 = "SWT_GTK3"; //$NON-NLS-1$
	
	public IVisualEditor createVisualEditor(final EditorPart multiPageEditor, StructuredTextEditor textEditor, int visualMode, BundleMap bundleMap) {
		//this property is added in VPE tests to make VPE always opened 
		if (Boolean.valueOf(System.getProperty(LOAD_XULRUNNER_ENGINE))) {
			return getVpeEditor(multiPageEditor, textEditor, visualMode, bundleMap);
		}
		
		boolean isHtmlFile = "html".equals(((org.eclipse.ui.IFileEditorInput)multiPageEditor.getEditorInput()).getFile().getFileExtension()); //$NON-NLS-1$
		if (isHtmlFile) {
			return getPreviewEditor(multiPageEditor, textEditor, visualMode, bundleMap);
		} else {
			if (XulRunnerBrowser.isCurrentPlatformOfficiallySupported() && !isGTK3()) {
				return getVpeEditor(multiPageEditor, textEditor, visualMode, bundleMap);
			} else {
				return getPreviewEditor(multiPageEditor, textEditor, visualMode, bundleMap);				
			}
		}
	}
	
	private IVisualEditor getVpeEditor(final EditorPart multiPageEditor, StructuredTextEditor textEditor, int visualMode, BundleMap bundleMap) {
		return new VpeEditorPart(multiPageEditor, textEditor, visualMode, bundleMap) {
			public void doSave(IProgressMonitor monitor) {
				multiPageEditor.doSave(monitor);
			}
		};
	}

	private IVisualEditor getPreviewEditor(final EditorPart multiPageEditor, StructuredTextEditor textEditor, int visualMode, BundleMap bundleMap) {
		return new VpvEditor2(multiPageEditor, textEditor, visualMode, bundleMap);
	}
	
	private static boolean isGTK3() {
		if (Platform.WS_GTK.equals(Platform.getWS())) {
			String gtk3 = System.getProperty(SWT_GTK3);
			if (gtk3 == null) {
				gtk3 = System.getenv(SWT_GTK3);
			}
			return !"0".equals(gtk3); //$NON-NLS-1$
		}
		return false;
	}
}
