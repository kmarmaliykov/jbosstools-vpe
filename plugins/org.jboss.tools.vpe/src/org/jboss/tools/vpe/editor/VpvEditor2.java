package org.jboss.tools.vpe.editor;

/**
 * @author Yahor Radtsevich (yradtsevich)
 */

import static org.jboss.tools.vpe.preview.core.server.HttpConstants.ABOUT_BLANK;
import static org.jboss.tools.vpe.preview.core.server.HttpConstants.HTTP;
import static org.jboss.tools.vpe.preview.core.server.HttpConstants.LOCALHOST;
import static org.jboss.tools.vpe.preview.core.server.HttpConstants.PROJECT_NAME;
import static org.jboss.tools.vpe.preview.core.server.HttpConstants.VIEW_ID;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.EditorReference;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.jboss.tools.common.model.ui.editor.IModelObjectEditorInput;
import org.jboss.tools.jst.web.ui.WebUiPlugin;
import org.jboss.tools.jst.web.ui.internal.editor.editor.IVisualController;
import org.jboss.tools.jst.web.ui.internal.editor.editor.IVisualEditor;
import org.jboss.tools.jst.web.ui.internal.editor.jspeditor.StorageRevisionEditorInputAdapter;
import org.jboss.tools.jst.web.ui.internal.editor.preferences.IVpePreferencesPage;
import org.jboss.tools.vpe.IVpeHelpContextIds;
import org.jboss.tools.vpe.VpePlugin;
import org.jboss.tools.vpe.editor.xpl.CustomSashForm;
import org.jboss.tools.vpe.editor.xpl.CustomSashForm.ICustomSashFormListener;
import org.jboss.tools.vpe.preview.core.transform.DomUtil;
import org.jboss.tools.vpe.preview.core.transform.VpvDomBuilder;
import org.jboss.tools.vpe.preview.core.transform.VpvVisualModel;
import org.jboss.tools.vpe.preview.core.transform.VpvVisualModelHolder;
import org.jboss.tools.vpe.preview.core.util.SuitableFileExtensions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class VpvEditor2 extends EditorPart implements VpvVisualModelHolder, IVisualEditor {

	public static final String ID = "org.jboss.tools.vpe.vpv.views.VpvView"; //$NON-NLS-1$

	private Browser browser;
	
	private Job currentJob;
	
	private VpvVisualModel visualModel;
	private int modelHolderId;

//	private EditorListener editorListener;
	private SelectionListener selectionListener;
	
	private IEditorPart sourceEditor;
	private EditorPart multiPageEditor;//parent editor
	private int visualMode;
	private CustomSashForm container;
	/*
	 * VPE visual components.
	 */
	private Composite sourceContent = null;
	private Composite visualContent = null;
	private Composite previewContent = null;

	IEditorPart activeEditor;
	
	private IDocumentListener documentListener;

	
	public VpvEditor2(EditorPart multiPageEditor,
			StructuredTextEditor textEditor, int visualMode) {
		setModelHolderId(Activator.getDefault().getVisualModelHolderRegistry().registerHolder(this));
		this.sourceEditor = textEditor;
		this.visualMode = visualMode;
		this.multiPageEditor = multiPageEditor;
	}
	
	@Override
	public void setInput(IEditorInput input) {
		super.setInput(input);
	}
	
	@Override
	public void dispose() {
//		getSite().getPage().removePartListener(editorListener);
		getSite().getPage().removeSelectionListener(selectionListener);
		Activator.getDefault().getVisualModelHolderRegistry().unregisterHolder(this);
		super.dispose();
	}
	
	public void createPartControl(final Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IVpeHelpContextIds.VISUAL_PAGE_EDITOR);
		/*
		 * Container composite for editor part
		 */
		/*
		 *  Fix for https://jira.jboss.org/jira/browse/JBIDE-5744
		 *  Where is a problem with composite parent redrawing in a case
		 *  cmpEdTl = new Composite (parent, SWT.NONE)
		 *  P. S.: Reproducible under Win x64 on Eclipse 32
		 *  see https://bugs.eclipse.org/bugs/show_bug.cgi?id=302950
		 */
		Composite cmpEdTl =  parent;
		GridLayout layoutEdTl = new GridLayout(2, false);
		layoutEdTl.verticalSpacing = 0;
		layoutEdTl.marginHeight = 0;
		layoutEdTl.marginBottom = 3;
		layoutEdTl.marginWidth = 0;
		cmpEdTl.setLayout(layoutEdTl);
		cmpEdTl.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		/*
		 * The Visual Page Editor itself
		 */
		Composite cmpEd = new Composite(cmpEdTl, SWT.BORDER);
		GridLayout layoutEd = new GridLayout(1, false);
		layoutEd.marginBottom = 0;
		layoutEd.marginHeight = 1;
		layoutEd.marginWidth = 0;
		layoutEd.marginRight = 0;
		layoutEd.marginLeft = 1;
		layoutEd.verticalSpacing = 0;
		layoutEd.horizontalSpacing = 0;
		cmpEd.setLayout(layoutEd);
		cmpEd.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		/*
		 * Creating selection bar at the bottom of the editor
		 */
//		selectionBar.createToolBarComposite(cmpEdTl, true);
		
		//container = new SashForm(cmpEd, SWT.VERTICAL);
		/*
		 * https://jira.jboss.org/jira/browse/JBIDE-4152 
		 * Editors orientation is based on preference's settings.
		 */
		container = new CustomSashForm(cmpEd, CustomSashForm
			.getSplittingDirection(WebUiPlugin.getDefault().getPreferenceStore()
					.getString(IVpePreferencesPage.VISUAL_SOURCE_EDITORS_SPLITTING)));

		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		if (CustomSashForm.isSourceEditorFirst(WebUiPlugin.getDefault().getPreferenceStore()
				.getString(IVpePreferencesPage.VISUAL_SOURCE_EDITORS_SPLITTING))) {
		    sourceContent = new Composite(container, SWT.NONE);
		    visualContent = new Composite(container, SWT.NONE);
		} else {
		    visualContent = new Composite(container, SWT.NONE);
		    sourceContent = new Composite(container, SWT.NONE);
		}
		sourceContent.setLayout(new FillLayout());
		visualContent.setLayout(new FillLayout());


		// Create a preview content
		previewContent = new Composite(container, SWT.NONE);
		//previewContent.setLayout(new FillLayout());
		previewContent.setLayout(new GridLayout());

		if (sourceEditor == null)
			sourceEditor = new StructuredTextEditor() {
				public void safelySanityCheckState(IEditorInput input) {
					super.safelySanityCheckState(input);
				}
			};
		container.setSashBorders(new boolean[] { true, true, true });

		final ControlListener controlListener = new ControlListener() {
			public void controlMoved(ControlEvent event) {}
			public void controlResized(ControlEvent event) {
				container.layout();
			}
		};
		parent.addControlListener(controlListener);
		parent.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				parent.removeControlListener(controlListener);
				parent.removeDisposeListener(this);
			}
			
		});
		
		final ControlListener visualContentControlListener = new ControlListener() {
			public void controlMoved(ControlEvent event) {
			
			}

			public void controlResized(ControlEvent event) {
				updateVisualEditorVisibility();
			}
		};
		visualContent.addControlListener(visualContentControlListener);
		visualContent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				visualContent.removeControlListener(visualContentControlListener);
				visualContent.removeDisposeListener(this);
			}
		});

		// createVisualEditor();

		// createPreviewBrowser();

		try {
			IEditorInput input = getEditorInput();
			if (!( input instanceof IModelObjectEditorInput) && input instanceof IStorageEditorInput) {
				input = new StorageRevisionEditorInputAdapter((IStorageEditorInput) input);
			}
			sourceEditor.init(getEditorSite(), input);

			if (sourceContent != null) {
				sourceEditor.createPartControl(sourceContent);
				sourceContent.addListener(SWT.Activate, new Listener() {
					public void handleEvent(Event event) {
						if (event.type == SWT.Activate) {
							if (activeEditor != sourceEditor) {
								activeEditor = sourceEditor;
								setFocus();
							}
						}
					}
				});
			}
			
			activeEditor = sourceEditor;

		} catch (CoreException e) {
			VpePlugin.reportProblem(e);
		}
		// setVisualMode(visualMode);
		// ///////////////////////////////////////
		// ///// Add preference listener

		// ///////////////////////////////////////
		
		browser = new Browser(visualContent, SWT.WEBKIT);
		browser.setUrl(ABOUT_BLANK);
		browser.addProgressListener(new ProgressAdapter() {
			@Override
			public void completed(ProgressEvent event) {
				ISelection currentSelection = getCurrentSelection();
				updateSelectionAndScrollToIt(currentSelection);
			}
		});
		
		browser.addLocationListener(new LocationListener() {
			
			@Override
			public void changing(LocationEvent arg0) {
				System.out.println("Location changing"); //$NON-NLS-1$
			}
			
			@Override
			public void changed(LocationEvent arg0) {
				System.out.println("location changed"); //$NON-NLS-1$
			}
		});
		
		inizializeSelectionListener();	
		editorChanged(activeEditor);
//		inizializeEditorListener(browser, modelHolderId);
//		
//		setCurrentEditor(activeEditor);
		
		cmpEd.layout();
		
		container.addCustomSashFormListener(new ICustomSashFormListener() {
			public void dividerMoved(int firstControlWeight, int secondControlWeight) {
				WebUiPlugin.getDefault().getPreferenceStore().
				setValue(IVpePreferencesPage.VISUAL_SOURCE_EDITORS_WEIGHTS, secondControlWeight);
			}
		});
		
		
	}

//	private void inizializeEditorListener(Browser browser, int modelHolderId ) {
//		editorListener = new EditorListener();
//		getSite().getPage().addPartListener(editorListener);
//		editorListener.showBootstrapPart();
//	}

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
		return sourceEditor;
	}
	
	public void editorChanged(IEditorPart editor) {
/*		if (sourceEditor == editor) {
//			// do nothing
//		} else*/ if (editor == null) {
			browser.setUrl(ABOUT_BLANK);
			setCurrentEditor(null);
		} else if (isImportant(editor)) {
			formRequestToServer(editor);
			setCurrentEditor(editor);
		} else {
			browser.setUrl(ABOUT_BLANK);
			setCurrentEditor(null);
		}
	}
	
	private boolean isImportant(IEditorPart editor) {
		if (editor.getAdapter(StructuredTextEditor.class) != null){
			return true; // TODO check DOM model support
		}
		return false;
	}

	private void setCurrentEditor(IEditorPart currentEditor) {
		if (this.sourceEditor != null) {
			IDocument document = (IDocument) this.sourceEditor.getAdapter(IDocument.class);
			if (document != null) {
				document.removeDocumentListener(getDocumentListener());
			}
		}
		
		this.sourceEditor = currentEditor;
		
		if (this.sourceEditor != null) {
			IDocument document = (IDocument) this.sourceEditor.getAdapter(IDocument.class);
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
					// Don't handle this event
				}

				@Override
				public void documentChanged(DocumentEvent event) {
					if (currentJob == null || currentJob.getState() != Job.WAITING) {
						if (currentJob != null && currentJob.getState() == Job.SLEEPING) {
							currentJob.cancel();
						}
						currentJob = createPreviewUpdateJob(event);
					}

					currentJob.schedule(500);
				}

			};
		}

		return documentListener;
	}
	   	
	private Job createPreviewUpdateJob(final DocumentEvent event) {
		Job job = new UIJob("Preview Update") { //$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (!browser.isDisposed()) {
//					preRefresh(event);
					refresh(browser);
				}
				return Status.OK_STATUS;
			}
		};
		return job;
	}
	
//	private void preRefresh(DocumentEvent event) {
//		IDocument document = event.getDocument();
//		int startSelectionPosition = event.getOffset();
//		int endSelectionPosition = startSelectionPosition + event.getText().length();
//
//		Node firstSelectedNode = getNodeBySourcePosition(document, startSelectionPosition);
//		Node lastSelectedNode = getNodeBySourcePosition(document, endSelectionPosition);
//		VpvDomBuilder domBuilder = Activator.getDefault().getDomBuilder();
//		Document sourceDocument = firstSelectedNode.getOwnerDocument();
//		if (domBuilder != null) {
//			Node commonParent = getCommonNode(firstSelectedNode, lastSelectedNode);
//			final VisualMutation mutation = domBuilder.rebuildSubtree(browser, visualModel, sourceDocument, commonParent);
//			try {
//				final String newParentHtml = DomUtil
//						.nodeToString(mutation.getNewParentNode())
//						.replace("\\", "\\\\").replace("\n", "\\n")
//						.replace("\r", "\\r").replace("\"", "\\\"")
//						.replace("\'", "\\\'");
//				browser.getDisplay().asyncExec(new Runnable() {
//					@Override
//					public void run() {
//						browser.execute("var oldElement = document.querySelector('[" + VpvDomBuilder.ATTR_VPV_ID + "=\"" + mutation.getOldParentId() + "\"]');"
//								+ "oldElement.insertAdjacentHTML('beforebegin', '" + newParentHtml + "');"
//								+ "oldElement.parentElement.removeChild(oldElement);");
//					}
//				});
//			} catch (TransformerException e) {
//				Activator.logError(e);
//			}
//		}
//	}
	
	private void refresh(Browser browser){
		formRequestToServer(activeEditor);
//		browser.refresh();//setUrl(browser.getUrl());
	}

	private boolean isCurrentEditor(IEditorPart editorPart) {
		if (sourceEditor == editorPart) {
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
			browser.setUrl(url);
		} else {
			browser.setUrl(ABOUT_BLANK);
		}
	}

//	private class EditorListener implements IPartListener2 {
//
//		@Override
//		public void partActivated(IWorkbenchPartReference partRef) {
//			Activator.logInfo(partRef + " is Activated"); //$NON-NLS-1$
//			if (partRef instanceof EditorReference) {
//				Activator.logInfo("instance of Editor reference"); //$NON-NLS-1$
//				IEditorPart editor = ((EditorReference) partRef).getEditor(false);
//				editorChanged(editor);
//			}
//		}
//
//		@Override
//		public void partOpened(IWorkbenchPartReference partRef) {
//			Activator.logInfo(partRef + " is Opened"); //$NON-NLS-1$
//		}
//
//		@Override
//		public void partClosed(IWorkbenchPartReference partRef) {
//			Activator.logInfo(partRef + " is Closed"); //$NON-NLS-1$
//			if (partRef instanceof EditorReference) {
//				IEditorPart editorPart = ((EditorReference) partRef).getEditor(false);
//				if (isCurrentEditor(editorPart)) {
//					editorChanged(null);
//				}
//			}
//		}
//
//		@Override
//		public void partBroughtToTop(IWorkbenchPartReference partRef) {
//		}
//
//		@Override
//		public void partDeactivated(IWorkbenchPartReference partRef) {
//		}
//
//		@Override
//		public void partHidden(IWorkbenchPartReference partRef) {
//		}
//
//		@Override
//		public void partVisible(IWorkbenchPartReference partRef) {
//		}
//
//		@Override
//		public void partInputChanged(IWorkbenchPartReference partRef) {
//		}
//
//		public void showBootstrapPart() {
//			IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
//					.getActiveEditor();
//			formRequestToServer(activeEditor);
//		}
//
//	}
	
	private class SelectionListener implements ISelectionListener {

		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (selection instanceof IStructuredSelection && isInCurrentEditor((IStructuredSelection) selection)) {
				updateSelectionAndScrollToIt(selection);
			}
		}
	}
	
	private ISelection getCurrentSelection() {
		Activator activator = Activator.getDefault();
		IWorkbench workbench = activator.getWorkbench();
		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
		ISelectionService selectionService = workbenchWindow.getSelectionService();
		ISelection selection = selectionService.getSelection();
		return selection;
	}

	private boolean isInCurrentEditor(IStructuredSelection selection) {
		Node selectedNode = getNodeFromSelection(selection);
		Document selectionDocument = null;
		if (selectedNode != null) {
			selectionDocument = selectedNode.getOwnerDocument();
		}
		
		Document editorDocument = getEditorDomDocument();
		
		if (selectionDocument != null && selectionDocument == editorDocument) {
			return true;
		} else {
			return false;
		}
	}

	private Document getEditorDomDocument() {
		IDOMModel editorModel = null;
		if (sourceEditor != null) {
			editorModel = (IDOMModel) sourceEditor.getAdapter(IDOMModel.class);
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
	
	private Node getNodeFromSelection(IStructuredSelection selection) {
		Object firstElement = selection.getFirstElement();
		if (firstElement instanceof Node) {
			return (Node) firstElement;
		} else {
			return null;
		}
	}
	
	public Long getIdForSelection(Node selectedSourceNode, VpvVisualModel visualModel) {
		Long id = null;
		if (selectedSourceNode != null && visualModel != null) {
			Map<Node, Node> sourceVisuaMapping = visualModel.getSourceVisualMapping();
			
			Node visualNode = null;
			Node sourceNode = selectedSourceNode;
			do {
				visualNode = sourceVisuaMapping.get(sourceNode);
				sourceNode = DomUtil.getParentNode(sourceNode);
			} while (visualNode == null && sourceNode != null);
			
			if (!(visualNode instanceof Element)) { // text node, comment, etc
				visualNode = DomUtil.getParentNode(visualNode); // should be element now or null
			}
			
			String idString = null;
			if (visualNode instanceof Element) {
				Element elementNode = (Element) visualNode;
				idString = elementNode.getAttribute(VpvDomBuilder.ATTR_VPV_ID);
			}
			
			if (idString != null && !idString.isEmpty()) {
				try {
					id = Long.parseLong(idString);
				} catch (NumberFormatException e) {
					Activator.logError(e);
				}
			}
		}
		return id;
	}
	
	private void updateBrowserSelection(Long currentSelectionId) {
		String selectionStyle;
		if (currentSelectionId == null) {
			selectionStyle = ""; //$NON-NLS-1$
		} else {
			selectionStyle = "'[" + VpvDomBuilder.ATTR_VPV_ID + "=\"" + currentSelectionId + "\"] {outline: 2px solid blue;}'"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		
		browser.execute(
		"(function(css) {" + //$NON-NLS-1$
			"var style=document.getElementById('VPV-STYLESHEET');" + //$NON-NLS-1$
//			"if ('\\v' == 'v') /* ie only */ {alert('ie');" +
//				"if (style == null) {" +
//					"style = document.createStyleSheet();" +
//				"}" +
//				"style.cssText = css;" +
//			"}" +
//			"else {" +
				"if (style == null) {" + //$NON-NLS-1$
					"style = document.createElement('STYLE');" + //$NON-NLS-1$
					"style.type = 'text/css';" + //$NON-NLS-1$
				"}" + //$NON-NLS-1$
				"style.innerHTML = css;" + //$NON-NLS-1$
				"document.body.appendChild(style);" + //$NON-NLS-1$
//			"}" +
			"style.id = 'VPV-STYLESHEET';" +  //$NON-NLS-1$
			"})(" + selectionStyle + ")"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private void scrollToId(Long currentSelectionId) {
		if (currentSelectionId != null) {
			browser.execute(
					"(function(){" + //$NON-NLS-1$
							"var selectedElement = document.querySelector('[" + VpvDomBuilder.ATTR_VPV_ID + "=\"" + currentSelectionId + "\"]');" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							"selectedElement.scrollIntoView(true);" + //$NON-NLS-1$
					"})()"   //$NON-NLS-1$
			);
		}
	}

	private void updateSelectionAndScrollToIt(ISelection currentSelection) {
		if (currentSelection instanceof IStructuredSelection) {
			Node sourceNode = getNodeFromSelection((IStructuredSelection) currentSelection);
			Long currentSelectionId = getIdForSelection(sourceNode, visualModel);
			updateBrowserSelection(currentSelectionId);
			scrollToId(currentSelectionId);
		}
	}
	
	public Browser getBrowser() {
		return browser;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		if (sourceEditor != null) {
			sourceEditor.doSave(monitor);
		}
	}

	@Override
	public void doSaveAs() {
		if (sourceEditor != null) {
			sourceEditor.doSaveAs();
			setInput(sourceEditor.getEditorInput());
		}
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
	}

	@Override
	public boolean isDirty() {
			return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		if (sourceEditor != null) {
			return sourceEditor.isSaveAsAllowed();
		} else {
			return false;
		}
	}

	@Override
	public void setVisualMode(int type) {
		switch (type) {
		case VISUALSOURCE_MODE:
			/*
			 * https://jira.jboss.org/browse/JBIDE-6832
			 * Restore the state after switching from Preview, for example.
			 */
//			selectionBar.setVisible(selectionBar.getAlwaysVisibleOption());
//			setVerticalToolbarVisible(true);
			/*
			 * Fixes https://jira.jboss.org/jira/browse/JBIDE-3140
			 * author Denis Maliarevich.
			 */
			container.setMaximizedControl(null);
			if (sourceContent != null) {
				sourceContent.setVisible(true);
				if (sourceEditor != null) {
                    activeEditor = sourceEditor;
                }
			}
			if (visualContent != null)
				visualContent.setVisible(true);
			if (previewContent != null) {
				previewContent.setVisible(false);
			}
			break;

//		case VISUAL_MODE:
//			selectionBar.showBar(showSelectionBar);
//			if (sourceContent != null)
//				sourceContent.setVisible(false);
//			if (visualContent != null)
//				visualContent.setVisible(true);
//			if (previewContent != null) {
//				previewContent.setVisible(false);
//			}
//			break;

		case SOURCE_MODE:
//			selectionBar.setVisible(selectionBar.getAlwaysVisibleOption());
			if (sourceContent != null) {
				sourceContent.setVisible(true);
				if (sourceEditor != null) {
                    activeEditor = sourceEditor;
                }
				/*
				 * Fixes https://jira.jboss.org/jira/browse/JBIDE-3140
				 * author Denis Maliarevich.
				 */
				container.setMaximizedControl(sourceContent);
				
				//Added by Max Areshkau
				//was fixed bug(border which drawed by iflasher doesn't hide on MACOS when we swith
				// to souce view)
//				if(Platform.getOS().equals(Platform.OS_MACOSX)&&controller!=null) {
//					
//				visualEditor.getController().visualRefresh();
//				}
			}
			/*
			 * Fixes https://jira.jboss.org/jira/browse/JBIDE-3140
			 * author Denis Maliarevich.
			 */
//			if (visualContent != null)
//				visualContent.setVisible(false);
//			if (previewContent != null) {
//				previewContent.setVisible(false);
//			}
			break;

		case PREVIEW_MODE:
//			if (selectionBar != null) {
//				selectionBar.setVisible(false);
//			}
			/*
			 * Fixes https://jira.jboss.org/jira/browse/JBIDE-3140
			 * author Denis Maliarevich.
			 */
//			if (sourceContent != null) {
//				sourceContent.setVisible(false);
//			}
//
//			if (visualContent != null) {
//				visualContent.setVisible(false);
//			}

			if (previewContent != null) {
				previewContent.setVisible(true);
				container.setMaximizedControl(previewContent);
			}
			break;
		}
		container.layout();
		if (visualMode == SOURCE_MODE && type != SOURCE_MODE) {
			visualMode = type;
			refresh(browser);
		}
		visualMode = type;
	}

	@Override
	public IVisualController getController() {
		return null;
	}

	@Override
	public Object getPreviewWebBrowser() {
		return browser;
	}

	@Override
	public void createPreviewBrowser() {
		
	}

	@Override
	public Object getVisualEditor() {
		return null;
	}

	@Override
	public void createVisualEditor() {
//		System.out.println("CVE");
//		browser = new Browser(visualContent, SWT.WEBKIT);
//		browser.setUrl(ABOUT_BLANK);
//		browser.addProgressListener(new ProgressAdapter() {
//			@Override
//			public void completed(ProgressEvent event) {
//				ISelection currentSelection = getCurrentSelection();
//				updateSelectionAndScrollToIt(currentSelection);
//			}
//		});
//		inizializeSelectionListener();	
//		inizializeEditorListener(browser, modelHolderId);
//		
//		makeActions();
//		contributeToActionBars();
	}

	@Override
	public void maximizeSource() {
		if (container != null) {
			if (CustomSashForm.isSourceEditorFirst(WebUiPlugin.getDefault().getPreferenceStore()
					.getString(IVpePreferencesPage.VISUAL_SOURCE_EDITORS_SPLITTING))) {
				container.maxDown();
			} else {
				container.maxUp();
			}
			/*
			 * In JUnit for JBIDE-3127 on manual maximizing
			 * SashForm control listener isn't fired up
			 * do it here.
			 */
			updateVisualEditorVisibility();
		}
	}

	@Override
	public void maximizeVisual() {
		if (container != null) {
			if (CustomSashForm.isSourceEditorFirst(WebUiPlugin.getDefault().getPreferenceStore()
					.getString(IVpePreferencesPage.VISUAL_SOURCE_EDITORS_SPLITTING))) {
				container.maxUp();
			} else {
				container.maxDown();
			}
			/*
			 * In JUnit for JBIDE-3127 on manual maximizing
			 * SashForm control listener isn't fired up
			 * do it here.
			 */
			updateVisualEditorVisibility();
		}
	}

	protected void updateVisualEditorVisibility() {
		Point point = visualContent.getSize();
		if (point.x == 0 || point.y == 0) {
//			VpeController controller = getController();
//			if (controller != null)
//				controller.setVisualEditorVisible(false);
		} else {
//			VpeController controller = getController();
//			if (controller != null && !controller.isVisualEditorVisible()) {
//				controller.setVisualEditorVisible(true);
//				if (controller.getSelectionManager() != null) {
//					controller.getSelectionManager().refreshVisualSelection();
//				}
//				if (!controller.isSynced()) {
//					controller.visualRefresh();
//				}
//			}
			refresh(browser);
		}
	}
}
