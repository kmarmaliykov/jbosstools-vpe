package org.jboss.tools.vpe.vpv.listener;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.browser.Browser;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.internal.EditorReference;
import org.jboss.tools.vpe.vpv.Activator;
import static org.jboss.tools.vpe.vpv.server.HttpConstants.*;

public class EditorListener implements IPartListener2 {
	
	private Browser browser;
	private int modelHolderId;

	public EditorListener(Browser browser, int modelHolderId) {
		this.browser = browser;
		this.modelHolderId = modelHolderId;
	}

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		Activator.logInfo(partRef + " is Activated");
	}
	

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
		Activator.logInfo(partRef + " is Opened");
		if (partRef instanceof EditorReference){
			Activator.logInfo("instance of Editor reference");
			IEditorPart editorPart = (IEditorPart) partRef.getPart(false);
			IFile ifile = getFileOpenedInEditor(editorPart);
			if (ifile != null){
				String url = formUrl(ifile);
				browser.setUrl(url);
			}
		}
	}
	
	private String formUrl(IFile ifile) {
		String projectName = ifile.getProject().getName();
		String projectRelativePath = ifile.getProjectRelativePath().toString();
		int port = Activator.getDefault().getServer().getPort();
		String url = HTTP + LOCALHOST + ":" + port + "/" + projectRelativePath + "?" + PROJECT_NAME + "=" + projectName + "&"
					 + VIEW_ID + "=" + modelHolderId;
		
		return url;
	}

	private IFile getFileOpenedInEditor(IEditorPart editorPart) {
		IFile file = null;
		if (editorPart.getEditorInput() instanceof IFileEditorInput) {
			IFileEditorInput fileEditorInput = (IFileEditorInput) editorPart.getEditorInput();
			file = fileEditorInput.getFile();
		}
		return file;
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		Activator.logInfo(partRef + " is Closed");

	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}


	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}


	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}
}
