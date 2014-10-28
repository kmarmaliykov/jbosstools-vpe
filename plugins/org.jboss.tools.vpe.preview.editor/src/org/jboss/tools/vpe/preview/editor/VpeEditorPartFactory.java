/*******************************************************************************
 * Copyright (c) 2007-2014 Exadel, Inc. and Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
 ******************************************************************************/ 
package org.jboss.tools.vpe.preview.editor;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.jboss.tools.jst.web.ui.WebUiPlugin;
import org.jboss.tools.jst.web.ui.internal.editor.bundle.BundleMap;
import org.jboss.tools.jst.web.ui.internal.editor.editor.IVisualEditor;
import org.jboss.tools.jst.web.ui.internal.editor.editor.IVisualEditorFactory;
import org.jboss.tools.jst.web.ui.internal.editor.preferences.IVpePreferencesPage;
import org.jboss.tools.vpe.editor.VpeEditorPart;
import org.jboss.tools.vpe.editor.util.VpePlatformUtil;
import org.jboss.tools.vpe.preview.core.util.SuitableFileExtensions;

/**
 * @author Konstantin Marmalyukov (kmarmaliykov)
 */
public class VpeEditorPartFactory implements IVisualEditorFactory {	
	public IVisualEditor createVisualEditor(final EditorPart multiPageEditor, StructuredTextEditor textEditor, int visualMode, BundleMap bundleMap) {
	    //Since we do not implement the option of showing Visual toolbar for preview editor into Eclipse toolbar - must always show visual toolbar within the editor
	    WebUiPlugin.getDefault().getPreferenceStore().setValue(IVpePreferencesPage.SHOW_VISUAL_TOOLBAR, true);
	    //this property is added in VPE tests to make VPE always opened 
		if (VpePlatformUtil.isXulrunnerEnabled()) {
			return getVpeEditor(multiPageEditor, textEditor, visualMode, bundleMap);
		}
		
		IEditorInput editorInput = multiPageEditor.getEditorInput();
		boolean isHtmlFile = false;
		if (editorInput instanceof IFileEditorInput) { //file opened from workspace
			String fileExtension = ((IFileEditorInput) editorInput).getFile().getFileExtension();
			isHtmlFile = SuitableFileExtensions.isHTML(fileExtension);
		} else  if (editorInput instanceof IPathEditorInput) { //handle external file
		    IPath externalFile = ((IPathEditorInput) editorInput).getPath();
		    isHtmlFile = SuitableFileExtensions.isHTML(externalFile.getFileExtension());
		} else { //not a file, maybe some html text, should use simple preview for it
			isHtmlFile = true;
		}
		
		if (isHtmlFile) {
			return getPreviewEditor(multiPageEditor, textEditor, visualMode, bundleMap);
		} else {
			return getVpeEditor(multiPageEditor, textEditor, visualMode, bundleMap);
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
		return new VpvEditorPart(multiPageEditor, textEditor, visualMode, bundleMap);
	}
	
	
}
