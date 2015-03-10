/*******************************************************************************
 * Copyright (c) 2007-2011 Exadel, Inc. and Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.vpe.preview.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.jboss.tools.common.resref.core.ResourceReference;
import org.jboss.tools.vpe.messages.VpeUIMessages;
import org.jboss.tools.vpe.preview.core.util.FileUtil;
import org.jboss.tools.vpe.resref.core.VpvResourcesDialog;

public class VpeResourcesDialogFactory {
	public static void openVpeResourcesDialog(IEditorPart visualEditor) {
		IPath absoluteDefaultPath = null;
		Object fileLocation = null;
		IEditorInput input = visualEditor.getEditorInput();
		if (input instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput) input).getFile();
			IFolder absoluteDefaultFolder = FileUtil.getDefaultWebRootFolder(file);
			if (absoluteDefaultFolder != null) {
				absoluteDefaultPath = absoluteDefaultFolder.getLocation();
			}
			fileLocation = file;
		} else if (input instanceof ILocationProvider) {
			ILocationProvider provider = (ILocationProvider) input;
			IPath path = provider.getPath(input);
			if (path != null) {
				fileLocation = path;
			}
		}
		
		if (fileLocation != null) {
			TitleAreaDialog dialog = new VpvResourcesDialog(
						PlatformUI.getWorkbench().getDisplay().getActiveShell(), fileLocation,
						new ResourceReference(absoluteDefaultPath != null ? absoluteDefaultPath.toOSString() : "", ResourceReference.PROJECT_SCOPE)); //$NON-NLS-1$
			dialog.open();
		} else {
			Activator.getDefault().logError(VpeUIMessages.COULD_NOT_OPEN_VPE_RESOURCES_DIALOG);
		}
	}
}
