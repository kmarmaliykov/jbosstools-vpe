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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Ilya Buziuk (ibuziuk)
 */
@SuppressWarnings("restriction")
public final class EditorUtil {
	
	private EditorUtil(){
	}

	public static boolean isInCurrentEditor(IStructuredSelection selection, IEditorPart editor) {
		Node selectedNode = getNodeFromSelection(selection);
		Document selectionDocument = null;
		if (selectedNode != null) {
			selectionDocument = selectedNode.getOwnerDocument();
		}

		Document editorDocument = getEditorDomDocument(editor);

		if (selectionDocument != null && selectionDocument == editorDocument) {
			return true;
		} else {
			return false;
		}
	}

	public static Node getNodeFromSelection(IStructuredSelection selection) {
		Object firstElement = selection.getFirstElement();
		if (firstElement instanceof Node) {
			return (Node) firstElement;
		} else {
			return null;
		}
	}
	
	public static boolean isImportant(IEditorPart editor) {
		String fileExtension = getFileExtensionFromEditor(editor);
		if (editor.getAdapter(StructuredTextEditor.class) != null || SuitableFileExtensions.isCssOrJs(fileExtension)) {
			return true; // TODO check DOM model support
		}
		return false;
	}
	
	public static IFile getFileOpenedInEditor(IEditorPart editorPart) {
		IFile file = null;
		if (editorPart != null && editorPart.getEditorInput() instanceof IFileEditorInput) {
			IFileEditorInput fileEditorInput = (IFileEditorInput) editorPart.getEditorInput();
			file = fileEditorInput.getFile();
		}
		return file;
	}
	
	public static String getFileExtensionFromEditor(IEditorPart editor) {
		String fileExtension = null;
		IFile file = EditorUtil.getFileOpenedInEditor(editor);
		if (file != null && file.exists()) {
			fileExtension = file.getFileExtension();
		}
		return fileExtension;
	}
	
	private static Document getEditorDomDocument(IEditorPart editor) {
		IDOMModel editorModel = null;
		if (editor != null) {
			editorModel = (IDOMModel) editor.getAdapter(IDOMModel.class);
		}

		IDOMDocument editorIdomDocument = null;
		if (editorModel != null) {
			editorIdomDocument = editorModel.getDocument();
		}

		Element editorDocumentElement = null;
		if (editorIdomDocument != null) {
			editorDocumentElement = editorIdomDocument.getDocumentElement();
		}

		Document editorDocument = null;
		if (editorDocumentElement != null) {
			editorDocument = editorDocumentElement.getOwnerDocument();
		}
		return editorDocument;
	}

}
