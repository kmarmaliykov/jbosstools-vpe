package org.jboss.tools.vpe.vpv.transform;

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
import org.jboss.tools.vpe.vpv.Activator;
import org.w3c.dom.Document;

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
					Activator.logError(e);
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
				Activator.logError(e);
			}
		}
		
		if (htmlText != null) {
			resourceAcceptor.acceptText("<!DOCTYPE html>\n" + htmlText, "text/html"); // XXX: remove doctype when selection will work in old IE
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
			mimeTypes = new MimetypesFileTypeMap(Activator.getFileUrl("lib/mime.types").openStream());
			return mimeTypes.getContentType(file);
		} catch (IOException e) {
			Activator.logError(e);
			return "application/octet-stream";
		}
	}
}
