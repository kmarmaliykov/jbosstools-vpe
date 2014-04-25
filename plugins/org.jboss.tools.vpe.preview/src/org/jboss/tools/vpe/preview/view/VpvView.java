/*******************************************************************************
 * Copyright (c) 2013-2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.vpe.preview.view;

import static org.jboss.tools.vpe.preview.core.server.HttpConstants.ABOUT_BLANK;
import static org.jboss.tools.vpe.preview.core.server.HttpConstants.HTTP;
import static org.jboss.tools.vpe.preview.core.server.HttpConstants.LOCALHOST;
import static org.jboss.tools.vpe.preview.core.server.HttpConstants.PROJECT_NAME;
import static org.jboss.tools.vpe.preview.core.server.HttpConstants.VIEW_ID;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.EditorReference;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.jboss.tools.vpe.preview.Activator;
import org.jboss.tools.vpe.preview.core.transform.TransformUtil;
import org.jboss.tools.vpe.preview.core.transform.VpvVisualModel;
import org.jboss.tools.vpe.preview.core.transform.VpvVisualModelHolder;
import org.jboss.tools.vpe.preview.core.util.EditorUtil;
import org.jboss.tools.vpe.preview.core.util.NavigationUtil;
import org.jboss.tools.vpe.preview.core.util.SuitableFileExtensions;
import org.w3c.dom.Node;

/**
 * @author Yahor Radtsevich (yradtsevich)
 * @author Ilya Buziuk (ibuziuk)
 */
@SuppressWarnings("restriction")
public class VpvView extends ViewPart implements VpvVisualModelHolder {
	public static final String ID = "org.jboss.tools.vpe.vpv.view.VpvView"; //$NON-NLS-1$
	private static final String GROUP_REFRESH = "org.jboss.tools.vpv.refresh"; //$NON-NLS-1$

	private Browser browser;
	private IAction refreshAction;
	private IAction openInDefaultBrowserAction;
	private IAction enableAutomaticRefreshAction;
	private IAction enableRefreshOnSaveAction;
	private boolean enableAutomaticRefresh = true; // available by default
	private IExecutionListener saveListener;
	private Job currentJob;
	private VpvVisualModel visualModel;
	private int modelHolderId;
	private EditorListener editorListener;
	private SelectionListener selectionListener;
	private IEditorPart currentEditor;
	private IDocumentListener documentListener;
	private Command saveCommand;
	private Command saveAllCommand;

	public VpvView() {
		setModelHolderId(Activator.getDefault().getVisualModelHolderRegistry().registerHolder(this));

		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		saveCommand = commandService.getCommand("org.eclipse.ui.file.save"); //$NON-NLS-1$
		saveAllCommand = commandService.getCommand("org.eclipse.ui.file.saveAll"); //$NON-NLS-1$
		saveListener = new IExecutionListener() {

			@Override
			public void postExecuteSuccess(String arg0, Object arg1) {
				refresh(browser);
			}

			@Override
			public void notHandled(String arg0, NotHandledException arg1) {
			}

			@Override
			public void postExecuteFailure(String arg0, ExecutionException arg1) {
			}

			@Override
			public void preExecute(String arg0, ExecutionEvent arg1) {
			}

		};
	}

	@Override
	public void dispose() {
		getSite().getPage().removePartListener(editorListener);
		getSite().getPage().removeSelectionListener(selectionListener);
		Activator.getDefault().getVisualModelHolderRegistry().unregisterHolder(this);
		super.dispose();
	}

	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		browser = new Browser(parent, SWT.NONE);
		browser.setUrl(ABOUT_BLANK);

		browser.addLocationListener(new LocationAdapter() {
			@Override
			public void changed(LocationEvent event) {
				NavigationUtil.disableAlert(browser);
				NavigationUtil.disableLinks(browser);

				ISelection currentSelection = getCurrentSelection();
				NavigationUtil.updateSelectionAndScrollToIt(currentSelection, browser, visualModel);
			}
		});

		browser.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent event) {
				String stringToEvaluate = "return document.elementFromPoint(" + event.x + ", " + event.y + ").outerHTML;"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				String result = (String) browser.evaluate(stringToEvaluate);
				String selectedElementId = TransformUtil.getSelectedElementId(result, "(?<=data-vpvid=\").*?(?=\")"); //$NON-NLS-1$

				NavigationUtil.outlineSelectedElement(browser, Long.parseLong(selectedElementId));

				String fileExtension = EditorUtil.getFileExtensionFromEditor(currentEditor);

				if (SuitableFileExtensions.isHTML(fileExtension)) {
					try {
						Node visualNode = TransformUtil.getVisualNodeByVpvId(visualModel, selectedElementId);
						Node sourseNode = TransformUtil.getSourseNodeByVisualNode(visualModel, visualNode);

						if (sourseNode != null && sourseNode instanceof IDOMNode) {
							int startOffset = ((IDOMNode) sourseNode).getStartOffset();
							int endOffset = ((IDOMNode) sourseNode).getEndOffset();

							StructuredTextEditor editor = (StructuredTextEditor) currentEditor.getAdapter(StructuredTextEditor.class);
							editor.selectAndReveal(startOffset, endOffset - startOffset);

						}

					} catch (XPathExpressionException e) {
						Activator.logError(e);
					}
				}
			}

		});

		inizializeSelectionListener();
		inizializeEditorListener(browser, modelHolderId);

		makeActions();
		contributeToActionBars();
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(refreshAction);
		manager.add(openInDefaultBrowserAction);
		manager.add(new Separator(GROUP_REFRESH));
		manager.appendToGroup(GROUP_REFRESH, enableAutomaticRefreshAction);
		manager.appendToGroup(GROUP_REFRESH, enableRefreshOnSaveAction);
	}

	private void inizializeEditorListener(Browser browser, int modelHolderId) {
		editorListener = new EditorListener();
		getSite().getPage().addPartListener(editorListener);
		editorListener.showBootstrapPart();
	}

	private void inizializeSelectionListener() {
		selectionListener = new SelectionListener();
		getSite().getPage().addPostSelectionListener(selectionListener);
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

	public void editorChanged(IEditorPart editor) {
		if (currentEditor == editor) {
			// do nothing
		} else if (editor == null) {
			browser.setUrl(ABOUT_BLANK);
			setCurrentEditor(null);
		} else if (EditorUtil.isImportant(editor)) {
			formRequestToServer(editor);
			setCurrentEditor(editor);
		} else {
			browser.setUrl(ABOUT_BLANK);
			setCurrentEditor(null);
		}
	}

	private void setCurrentEditor(IEditorPart currentEditor) {
		if (this.currentEditor != null) {
			IDocument document = (IDocument) this.currentEditor.getAdapter(IDocument.class);
			if (document != null) {
				document.removeDocumentListener(getDocumentListener());
			}
		}

		this.currentEditor = currentEditor;

		if (this.currentEditor != null) {
			IDocument document = (IDocument) this.currentEditor.getAdapter(IDocument.class);
			String fileExtension = EditorUtil.getFileExtensionFromEditor(this.currentEditor);
			
			// Trying to extract document for js and css files
			if (document == null && SuitableFileExtensions.isCssOrJs(fileExtension)) {
				document = ((AbstractDecoratedTextEditor) this.currentEditor).getDocumentProvider().getDocument(currentEditor.getEditorInput());
			}

			if (document != null) {
				document.addDocumentListener(getDocumentListener());
			}
		}
	}

	private IDocumentListener getDocumentListener() {
		if (documentListener == null) {
			documentListener = new IDocumentListener() {

				@Override
				public void documentAboutToBeChanged(DocumentEvent event) {
				}

				@Override
				public void documentChanged(DocumentEvent event) {
					if (enableAutomaticRefresh) {
						String fileExtension = EditorUtil.getFileExtensionFromEditor(currentEditor);
						if (SuitableFileExtensions.isCssOrJs(fileExtension)) {
							getActivePage().saveEditor(currentEditor, false); // saving all js and css stuff							
						}
						updatePreview();
					}
				}

			};
		}

		return documentListener;
	}

	private void makeActions() {
		makeRefreshAction();
		makeOpenInDefaultBrowserAction();
		makeEnableAutomaticRefreshAction();
		makeEnableRefreshOnSaveAction();
	}

	private void makeEnableAutomaticRefreshAction() {
		enableAutomaticRefreshAction = new Action(Messages.VpvView_ENABLE_AUTOMATIC_REFRESH, IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				if (enableAutomaticRefreshAction.isChecked()) {
					enableAutomaticRefresh = true;

					enableRefreshOnSaveAction.setChecked(false);
					saveCommand.removeExecutionListener(saveListener);
					saveAllCommand.removeExecutionListener(saveListener);
				} else {
					enableAutomaticRefresh = false;
				}
			}
		};

		enableAutomaticRefreshAction.setChecked(true);
		enableAutomaticRefreshAction.setImageDescriptor(Activator.getImageDescriptor("icons/refresh_on_change.png")); //$NON-NLS-1$
	}

	private void makeEnableRefreshOnSaveAction() {
		enableRefreshOnSaveAction = new Action(Messages.VpvView_ENABLE_REFRESH_ON_SAVE, IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				if (enableRefreshOnSaveAction.isChecked()) {
					saveCommand.addExecutionListener(saveListener);
					saveAllCommand.addExecutionListener(saveListener);
					
					enableAutomaticRefreshAction.setChecked(false);
					enableAutomaticRefresh = false;
				} else {
					saveCommand.removeExecutionListener(saveListener);
					saveAllCommand.removeExecutionListener(saveListener);
				}
			}
		};

		enableRefreshOnSaveAction.setChecked(false);
		enableRefreshOnSaveAction.setImageDescriptor(Activator.getImageDescriptor("icons/refresh_on_save.png")); //$NON-NLS-1$
	}

	private void makeOpenInDefaultBrowserAction() {
		openInDefaultBrowserAction = new Action() {
			public void run() {
				URL url;
				try {
					url = new URL(browser.getUrl()); // validate URL (to do not open 'about:blank' and similar)
					Program.launch(url.toString());
				} catch (MalformedURLException e) {
					Activator.logError(e);
				}
			}
		};

		openInDefaultBrowserAction.setText(Messages.VpvView_OPEN_IN_DEFAULT_BROWSER);
		openInDefaultBrowserAction.setToolTipText(Messages.VpvView_OPEN_IN_DEFAULT_BROWSER);
		openInDefaultBrowserAction.setImageDescriptor(Activator.getImageDescriptor("icons/open_in_default_browser.gif")); //$NON-NLS-1$
	}

	private void makeRefreshAction() {
		refreshAction = new Action() {
			public void run() {
				refresh(browser);
			}
		};
		refreshAction.setText(Messages.VpvView_REFRESH);
		refreshAction.setToolTipText(Messages.VpvView_REFRESH);
		refreshAction.setImageDescriptor(Activator.getImageDescriptor("icons/refresh.gif")); //$NON-NLS-1$
	}

	private Job createPreviewUpdateJob() {
		Job job = new UIJob("Preview Update") { //$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (!browser.isDisposed()) {
					refresh(browser);
				}
				return Status.OK_STATUS;
			}
		};
		return job;
	}

	private void updatePreview() {
		if (currentJob == null || currentJob.getState() != Job.WAITING) {
			if (currentJob != null && currentJob.getState() == Job.SLEEPING) {
				currentJob.cancel();
			}
			currentJob = createPreviewUpdateJob();
		}

		currentJob.schedule(500);
	}

	private void refresh(Browser browser) {
		browser.setUrl(browser.getUrl());
	}

	private ISelection getCurrentSelection() {
		Activator activator = Activator.getDefault();
		IWorkbench workbench = activator.getWorkbench();
		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
		ISelectionService selectionService = workbenchWindow.getSelectionService();
		ISelection selection = selectionService.getSelection();
		return selection;
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
		String url = HTTP + LOCALHOST + ":" + port + "/" + projectRelativePath + "?" + PROJECT_NAME + "=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				+ projectName + "&" + VIEW_ID + "=" + modelHolderId; //$NON-NLS-1$ //$NON-NLS-2$

		return url;
	}

	private void formRequestToServer(IEditorPart editor) {
		IFile file = EditorUtil.getFileOpenedInEditor(editor);
		String fileExtension = null;
		if (file != null && file.exists()) {
			fileExtension = file.getFileExtension();
		}

		if (SuitableFileExtensions.contains(fileExtension)) {
			if (SuitableFileExtensions.isHTML(fileExtension)) {
				String url = formUrl(file);
				browser.setUrl(url);
			}
		} else {
			browser.setUrl(ABOUT_BLANK);
		}
	}

	private class EditorListener implements IPartListener2 {

		@Override
		public void partActivated(IWorkbenchPartReference partRef) {
			Activator.logInfo(partRef + " is Activated"); //$NON-NLS-1$
			if (partRef instanceof EditorReference) {
				Activator.logInfo("instance of Editor reference"); //$NON-NLS-1$
				IEditorPart editor = ((EditorReference) partRef).getEditor(false);
				editorChanged(editor);
			}
		}

		@Override
		public void partOpened(IWorkbenchPartReference partRef) {
			Activator.logInfo(partRef + " is Opened"); //$NON-NLS-1$
		}

		@Override
		public void partClosed(IWorkbenchPartReference partRef) {
			Activator.logInfo(partRef + " is Closed"); //$NON-NLS-1$
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
			IEditorPart activeEditor = getActivePage().getActiveEditor();
			formRequestToServer(activeEditor);
		}

	}

	private class SelectionListener implements ISelectionListener {

		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (selection instanceof IStructuredSelection && EditorUtil.isInCurrentEditor((IStructuredSelection) selection, currentEditor)) {
				NavigationUtil.updateSelectionAndScrollToIt(selection, browser, visualModel);
			}
		}
	}
	
	private IWorkbenchPage getActivePage() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}
		
}
