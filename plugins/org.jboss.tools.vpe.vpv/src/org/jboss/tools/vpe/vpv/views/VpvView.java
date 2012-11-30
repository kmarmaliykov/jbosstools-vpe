package org.jboss.tools.vpe.vpv.views;

/**
 * @author Yahor Radtsevich (yradtsevich)
 */

import static org.jboss.tools.vpe.vpv.server.HttpConstants.HTTP;
import static org.jboss.tools.vpe.vpv.server.HttpConstants.LOCALHOST;
import static org.jboss.tools.vpe.vpv.server.HttpConstants.PROJECT_NAME;
import static org.jboss.tools.vpe.vpv.server.HttpConstants.VIEW_ID;
import static org.jboss.tools.vpe.vpv.server.HttpConstants.ABOUT_BLANK;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.EditorReference;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.jboss.tools.vpe.vpv.Activator;
import org.jboss.tools.vpe.vpv.transform.VpvVisualModel;
import org.jboss.tools.vpe.vpv.transform.VpvVisualModelHolder;

public class VpvView extends ViewPart implements VpvVisualModelHolder {

	public static final String ID = "org.jboss.tools.vpe.vpv.views.VpvView";

	private Browser browser;
	
	private VpvVisualModel visualModel;
	private int modelHolderId;

	private EditorListener editorListener;
	private SelectionListener selectionListener;
	
	private IEditorPart currentEditor;
	
	
	public VpvView() {
		setModelHolderId(Activator.getDefault().getVisualModelHolderRegistry().registerHolder(this));
	}
	
	@Override
	public void dispose() {
		Activator.getDefault().getVisualModelHolderRegistry().unregisterHolder(this);
		getSite().getPage().removePartListener(editorListener);
		getSite().getPage().removeSelectionListener(selectionListener);
		super.dispose();
	}
	
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		
		browser = new Browser(parent, SWT.NONE);
		browser.setUrl(ABOUT_BLANK);
		inizializeSelectionListener();	
		inizializeEditorListener(browser, modelHolderId);
	}

	private void inizializeEditorListener(Browser browser, int modelHolderId ) {
		EditorListener editorListener = new EditorListener();
		getSite().getPage().addPartListener(editorListener);
		editorListener.showBootstrapPart();
	}

	private void inizializeSelectionListener() {
		selectionListener = new SelectionListener();
		getSite().getPage().addSelectionListener(selectionListener);	
	}

	public void setFocus() {
		browser.setFocus();
	}

	@Override
	public void setVisualModel(VpvVisualModel visualModel) {
		this.visualModel = visualModel;
	}

	public void setModelHolderId(int modelHolderId) {
		this.modelHolderId = modelHolderId;
	}
	
	
	public IEditorPart getCurrentEditor() {
		return currentEditor;
	}

	public void setCurrentEditor(IEditorPart currentEditor) {
		this.currentEditor = currentEditor;
	}
	
	private boolean isCurrentEditor(IEditorPart editorPart) {
		if (currentEditor == editorPart) {
			return true;
		}
		return false;
	}
	
	private String formUrl(IFile ifile) {
		String projectName = ifile.getProject().getName();
		String projectRelativePath = ifile.getProjectRelativePath().toString();
		int port = Activator.getDefault().getServer().getPort();
		String url = HTTP + LOCALHOST + ":" + port + "/" + projectRelativePath + "?" + PROJECT_NAME + "="
				+ projectName + "&" + VIEW_ID + "=" + modelHolderId;

		return url;
	}

	private IFile getFileOpenedInEditor(IEditorPart editorPart) {
		IFile file = null;
		if (editorPart != null && editorPart.getEditorInput() instanceof IFileEditorInput) {
			IFileEditorInput fileEditorInput = (IFileEditorInput) editorPart.getEditorInput();
			file = fileEditorInput.getFile();
		}
		return file;
	}
	
	private void formRequestToServer(IEditorPart editor) {
		IFile ifile = getFileOpenedInEditor(editor);
		if (ifile != null && SuitableFileExtensions.contains(ifile.getFileExtension().toString())) {
			String url = formUrl(ifile);
			browser.setUrl(url, null, new String[] { "Cache-Control: no-cache, no-store" });
		} else {
			browser.setUrl(ABOUT_BLANK);
		}
	}

	private class EditorListener implements IPartListener2 {

		public void editorChanged(IEditorPart editor) {
			if (currentEditor == editor) {
				return;
			} else if (editor == null) {
				browser.setUrl(ABOUT_BLANK);
			} else if (isImportant(editor)) {
				formRequestToServer(editor);
			} else {
				browser.setUrl(ABOUT_BLANK);
			}

			setCurrentEditor(editor);
		}

		private boolean isImportant(IEditorPart editor) {
			if (editor.getAdapter(StructuredTextEditor.class) != null){
				return true; // TODO check DOM model support
			}
			return false;
		}

		@Override
		public void partActivated(IWorkbenchPartReference partRef) {
			Activator.logInfo(partRef + " is Activated");
			if (partRef instanceof EditorReference) {
				Activator.logInfo("instance of Editor reference");
				IEditorPart editor = ((EditorReference) partRef).getEditor(false);
				editorChanged(editor);
			}
		}

		@Override
		public void partOpened(IWorkbenchPartReference partRef) {
			Activator.logInfo(partRef + " is Opened");
		}

		@Override
		public void partClosed(IWorkbenchPartReference partRef) {
			Activator.logInfo(partRef + " is Closed");
			if (partRef instanceof EditorReference) {
				IEditorPart editorPart = ((EditorReference) partRef).getEditor(false);
				if (isCurrentEditor(editorPart)) {
					editorChanged(null);
				}
			}
		}

		@Override
		public void partBroughtToTop(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partDeactivated(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partHidden(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partVisible(IWorkbenchPartReference partRef) {
		}

		@Override
		public void partInputChanged(IWorkbenchPartReference partRef) {
		}

		public void showBootstrapPart() {
			IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.getActiveEditor();
			formRequestToServer(activeEditor);
		}

	}
	
	private class SelectionListener implements ISelectionListener {

		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if(selection instanceof IStructuredSelection){
				
			}
		}
		
	}
}

