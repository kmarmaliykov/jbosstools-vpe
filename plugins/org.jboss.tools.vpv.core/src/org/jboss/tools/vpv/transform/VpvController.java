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
package org.jboss.tools.vpv.transform;

import java.io.File;
import java.io.IOException;

import javax.activation.MimetypesFileTypeMap;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.jboss.tools.vpv.core.VpvCorePlugin;
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
			Document sourceDocument = null;
			if (sourceModel instanceof IDOMModel) {
				IDOMModel sourceDomModel = (IDOMModel) sourceModel;
				sourceDocument = sourceDomModel.getDocument();
			}
			
			if (sourceDocument != null) {
				try {
					visualModel = domBuilder.buildVisualModel(sourceDocument);
				} catch (ParserConfigurationException e) {
					VpvCorePlugin.logError(e);
				}
			}
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
				VpvCorePlugin.logError(e);
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
		} else {
			resourceAcceptor.acceptError();
		}
	}
	
	private static String getMimeType(File file) {
		MimetypesFileTypeMap mimeTypes;
		try {
			mimeTypes = new MimetypesFileTypeMap(VpvCorePlugin.getFileUrl("lib/mime.types").openStream()); //$NON-NLS-1$
			return mimeTypes.getContentType(file);
		} catch (IOException e) {
			VpvCorePlugin.logError(e);
			return "application/octet-stream"; //$NON-NLS-1$
		}
	}
}
