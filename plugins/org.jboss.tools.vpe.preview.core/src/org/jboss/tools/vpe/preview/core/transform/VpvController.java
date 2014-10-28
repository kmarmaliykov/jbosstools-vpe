/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.vpe.preview.core.transform;

import java.io.File;
import java.io.IOException;

import javax.activation.MimetypesFileTypeMap;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.jboss.tools.vpe.preview.core.Activator;
import org.jboss.tools.vpe.preview.core.util.SuitableFileExtensions;
import org.w3c.dom.Document;

/**
 * @author Yahor Radtsevich (yradtsevich)
 * @author Ilya Buziuk (ibuziuk)
 */
@SuppressWarnings("restriction")
public class VpvController {
	private VpvDomBuilder domBuilder;	
	private VpvVisualModelHolderRegistry visualModelHolderRegistry;	
	
	public VpvController(VpvDomBuilder domBuilder, VpvVisualModelHolderRegistry visualModelHolderRegistry) {
		this.domBuilder = domBuilder;
		this.visualModelHolderRegistry = visualModelHolderRegistry;
	}

	public void getResource(String path, Integer viewId, ResourceAcceptor resourceAcceptor) {
		Path workspacePath = new Path(path);
		IFile requestedFile = ResourcesPlugin.getWorkspace().getRoot().getFile(workspacePath);
		
		VpvVisualModel visualModel = null;
		IStructuredModel sourceModel = null;
		try {
			sourceModel = StructuredModelManager.getModelManager().getExistingModelForRead(requestedFile);
			if (sourceModel == null) {
			    ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
			    ITextFileBuffer buffer = null;
			    IFileStore fileStore = EFS.getStore(workspacePath.toFile().toURI());
    			bufferManager.connectFileStore(fileStore, new NullProgressMonitor());
    			buffer = bufferManager.getFileStoreTextFileBuffer(fileStore);
    			
    			IDocument document = buffer.getDocument();
    			if (document instanceof IStructuredDocument) {
        			IModelManager modelManager = StructuredModelManager.getModelManager();
        			sourceModel = modelManager.getModelForEdit((IStructuredDocument)document);
    			}
			}
			Document sourceDocument = null;
			if (sourceModel instanceof IDOMModel) {
				IDOMModel sourceDomModel = (IDOMModel) sourceModel;
				sourceDocument = sourceDomModel.getDocument();
			}
			
			if (sourceDocument != null) {
				try {
					visualModel = domBuilder.buildVisualModel(sourceDocument);
				} catch (ParserConfigurationException e) {
					Activator.logError(e);
				}
			}
		} catch (Exception e) {
            Activator.logError(e);
        } finally {
			if (sourceModel != null) {
				sourceModel.releaseFromRead();
			}
		}
		
		VpvVisualModelHolder visualModelHolder = visualModelHolderRegistry.getHolderById(viewId);
		if (visualModelHolder != null) {
			visualModelHolder.setVisualModel(visualModel);
		}
		
		String htmlText = null;
		if (visualModel != null) {
			try {
				htmlText = DomUtil.nodeToString(visualModel.getVisualDocument());
			} catch (TransformerException e) {
				Activator.logError(e);
			}
		}
		
		if (htmlText != null) {
			resourceAcceptor.acceptText("<!DOCTYPE html>\n" + htmlText, "text/html"); // XXX: remove doctype when selection will work in old IE  //$NON-NLS-1$//$NON-NLS-2$
		} else if (requestedFile.getLocation() != null 
				&& requestedFile.getLocation().toFile() != null
				&& requestedFile.getLocation().toFile().exists()) {
			File file = requestedFile.getLocation().toFile();
			String mimeType = getMimeType(file);
			resourceAcceptor.acceptFile(file, mimeType);
		} else if(workspacePath.isAbsolute() && workspacePath.toFile().exists()) {
		    File file = workspacePath.toFile();
            String mimeType = getMimeType(file);
            resourceAcceptor.acceptFile(file, mimeType);
		} else {
			resourceAcceptor.acceptError();
		}
	}
	
	private static String getMimeType(File file) {
		MimetypesFileTypeMap mimeTypes;
		try {
			mimeTypes = new MimetypesFileTypeMap(Activator.getFileUrl("lib/mime.types").openStream()); //$NON-NLS-1$
			return mimeTypes.getContentType(file);
		} catch (IOException e) {
			Activator.logError(e);
			return "application/octet-stream"; //$NON-NLS-1$
		}
	}
}
