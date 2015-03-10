/*******************************************************************************
// * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
 ******************************************************************************/ 
package org.jboss.tools.vpe.preview.core.util;

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.eclipse.ui.ide.IDE;
import org.jboss.tools.common.model.util.EclipseResourceUtil;
import org.jboss.tools.common.resref.core.ResourceReference;
import org.jboss.tools.jst.web.WebUtils;
import org.jboss.tools.vpe.preview.core.Activator;
import org.jboss.tools.vpe.resref.core.AbsoluteFolderReferenceList;
import org.jboss.tools.vpe.resref.core.RelativeFolderReferenceList;


public class FileUtil {    
    public static IFile getFile(IEditorInput input, String fileName) {
		IPath tagPath = new Path(fileName);
		if (tagPath.isEmpty()) return null;

		if (input instanceof IFileEditorInput) {
			return getFile(fileName, ((IFileEditorInput) input).getFile());
		} else if (input instanceof ILocationProvider) {
    	    IPath path = ((ILocationProvider)input).getPath(input);
    	    if(path == null || path.segmentCount() < 1) return null;
    	    path = path.removeLastSegments(1).append(fileName);
    	    return EclipseResourceUtil.getFile(path.toString());			
		}
        return null;
    }

	/**
	 * @param fileName
	 * @param includeFile
	 * @return
	 */
	public static IFile getFile(String fileName, IFile includeFile) {
		IFile file = null;
		if (fileName.startsWith("/")) { //$NON-NLS-1$
			ResourceReference[] resources = AbsoluteFolderReferenceList
					.getInstance().getAllResources(includeFile);
			if (resources.length == 1) {
				String location = resources[0].getLocation() + fileName;
				IPath path = new Path(location);
				file = ResourcesPlugin.getWorkspace().getRoot()
						.getFileForLocation(path);
			} else {
				//WebArtifactEdit edit = WebArtifactEdit
				//		.getWebArtifactEditForRead(includeFile.getProject());
				IContainer[] webRootFolders = WebUtils.getWebRootFolders(includeFile.getProject());
				if (webRootFolders.length > 0) {
					for (IContainer webRootFolder : webRootFolders) {
						IFile handle = webRootFolder.getFile(new Path(fileName));
						if (handle.exists()) {
							file = handle;
							break;
						}
					}
				} else {
					/* Yahor Radtsevich (yradtsevich):
					 * Fix of JBIDE-4416: assume that the parent directory
					 * of the opened file is the web-root directory */
					file = resolveRelatedPath(includeFile, fileName);
				}
			}
		} else {
			ResourceReference[] resources = RelativeFolderReferenceList
					.getInstance().getAllResources(includeFile);
			if (resources.length == 1) {
				String location = resources[0].getLocation() + File.separator
						+ fileName;
				IPath path = new Path(location);
				file = ResourcesPlugin.getWorkspace().getRoot()
						.getFileForLocation(path);
			} else {
				file = resolveRelatedPath(includeFile, fileName);
			}
		}
		return file;
	}
	
	public static IFolder getDefaultWebRootFolder(IFile file) {
		IProject project = file.getProject();
		if (project == null) {
			return null;
		}

		IContainer[] webRootFolders = WebUtils.getWebRootFolders(project);
		IPath defaultWebRootPath = null;
		if (webRootFolders.length > 0) {
			defaultWebRootPath = webRootFolders[0].getFullPath();
		}
		if (defaultWebRootPath == null) {
			return null;
		}
		
		return ResourcesPlugin.getWorkspace().getRoot().getFolder(defaultWebRootPath);
	}

	/**
	 * Appends {@code relatedFilePath} to the parent directory of
	 * {@code baseFile}. Returns {@code null} if the file does not exist.
	 */
	private static IFile resolveRelatedPath(IFile baseFile,
			String relatedFilePath) {
		IPath currentFolder = baseFile.getParent().getFullPath();
		IPath path = currentFolder.append(relatedFilePath);
		IFile handle = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		return handle.exists() ? handle : null;
	}

	/**
	 * open editor
	 * @param file
	 */
	public static void openEditor(IFile file) {

		IWorkbenchPage workbenchPage = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			if (file != null) {
				IDE.openEditor(workbenchPage, file, true);
			}
		} catch (PartInitException ex) {
			Activator.logError(ex);
		}

	}
	
	/**
	 * 
	 * @param input
	 *            The editor input
	 * @return Path
	 */
	public static IPath getInputPath(IEditorInput input) {
		IPath inputPath = null;
		if (input instanceof ILocationProvider) {
			inputPath = ((ILocationProvider) input).getPath(input);
		} else if (input instanceof IFileEditorInput) {
			IFile inputFile = ((IFileEditorInput) input).getFile();
			if (inputFile != null) {
				inputPath = inputFile.getLocation();
			}
		}
		return inputPath;
	}
	
}
