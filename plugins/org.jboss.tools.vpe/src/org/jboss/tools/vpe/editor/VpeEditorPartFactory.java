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

import org.eclipse.ui.part.EditorPart;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.jboss.tools.jst.web.ui.internal.editor.bundle.BundleMap;
import org.jboss.tools.jst.web.ui.internal.editor.editor.IVisualEditor;
import org.jboss.tools.jst.web.ui.internal.editor.editor.IVisualEditorFactory;

public class VpeEditorPartFactory implements IVisualEditorFactory {

	public IVisualEditor createVisualEditor(final EditorPart multiPageEditor, StructuredTextEditor textEditor, int visualMode, BundleMap bundleMap) {
		return new VpvEditor2(multiPageEditor, textEditor, visualMode);
//		return new VpeEditorPart(multiPageEditor, textEditor, visualMode, bundleMap) {
//			public void doSave(IProgressMonitor monitor){
//				multiPageEditor.doSave(monitor);
//			}
//		};
	}

}
