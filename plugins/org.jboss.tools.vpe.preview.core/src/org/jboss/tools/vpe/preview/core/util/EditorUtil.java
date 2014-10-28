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

import static org.jboss.tools.vpe.preview.core.server.HttpConstants.HTTP;
import static org.jboss.tools.vpe.preview.core.server.HttpConstants.LOCALHOST;
import static org.jboss.tools.vpe.preview.core.server.HttpConstants.VIEW_ID;
import static org.jboss.tools.vpe.preview.core.server.HttpConstants.WEBROOT_PATH;
import static org.jboss.tools.vpe.preview.core.server.VpvSocketProcessor.UTF_8;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.jboss.tools.common.resref.core.ResourceReference;
import org.jboss.tools.common.web.WebUtils;
import org.jboss.tools.vpe.resref.core.AbsoluteFolderReferenceList;
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
	
	public static IWorkbenchPage getActivePage() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
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
		} else if (editor.getEditorInput() instanceof IPathEditorInput) {
            fileExtension = ((IPathEditorInput)editor.getEditorInput()).getPath().getFileExtension();
        }
		return fileExtension;
	}

	public static String formUrl(IFile file, int modelHolderId, String serverPort) throws UnsupportedEncodingException {
		String projectRelativePath = file.getProjectRelativePath().toString();
		IPath webroot = getWebRoot(file);
		String webrootRelativePathString = projectRelativePath;
		String webrootString = null;
		if(webroot!=null) {
			IPath webrootRelativePath = file.getFullPath().makeRelativeTo(webroot);
			webrootRelativePathString = webrootRelativePath.toString();
			webrootString = webroot.toString();
		}  else {
			String projectName = file.getProject().getName();
			webrootString = "/" + projectName; //$NON-NLS-1$
		}
		String parameters = WEBROOT_PATH + "=" + URLEncoder.encode(webrootString, UTF_8) + "&" + VIEW_ID + "=" + modelHolderId; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return HTTP + LOCALHOST + ":" + serverPort + "/" + webrootRelativePathString + "?" + parameters; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/** Forms URL for external file to be opened in visual editor 
	 * 
	 * @param file external file to be opened 
	 * @param modelHolderId
	 * @param serverPort 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String formUrl(IPath file, int modelHolderId, String serverPort) throws UnsupportedEncodingException {
           return HTTP + LOCALHOST + ":" + serverPort + "/" + file.toOSString() + "?" + VIEW_ID + "=" + modelHolderId; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
	
	private static IPath getWebRoot(IFile file) {
		ResourceReference[] resources = AbsoluteFolderReferenceList.getInstance().getAllResources(file);
		IResource webroot = null;
		if (resources.length == 1) {
			String location = resources[0].getLocation();
			IPath path = new Path(location);
			webroot = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
		}
		if (webroot == null) {
			webroot = WebUtils.getWebRootFolder(file);
		}
		return webroot != null ? webroot.getFullPath() : null;
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
