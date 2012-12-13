package org.jboss.tools.vpe.vpv.views;

/**
 * @author Yahor Radtsevich (yradtsevich)
 */

import static org.jboss.tools.vpe.vpv.server.HttpConstants.ABOUT_BLANK;
import static org.jboss.tools.vpe.vpv.server.HttpConstants.HTTP;
import static org.jboss.tools.vpe.vpv.server.HttpConstants.LOCALHOST;
import static org.jboss.tools.vpe.vpv.server.HttpConstants.PROJECT_NAME;
import static org.jboss.tools.vpe.vpv.server.HttpConstants.VIEW_ID;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.EditorReference;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.INodeAdapter;
import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.jboss.tools.vpe.vpv.Activator;
import org.jboss.tools.vpe.vpv.transform.DomUtil;
import org.jboss.tools.vpe.vpv.transform.VisualMutation;
import org.jboss.tools.vpe.vpv.transform.VpvDomBuilder;
import org.jboss.tools.vpe.vpv.transform.VpvVisualModel;
import org.jboss.tools.vpe.vpv.transform.VpvVisualModelHolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class VpvView extends ViewPart implements VpvVisualModelHolder {

	public static final String ID = "org.jboss.tools.vpe.vpv.views.VpvView"; //$NON-NLS-1$

	private Browser browser;
	private IAction refreshAction;
	private IAction openInDefaultBrowserAction;
	private IAction disableAutomaticRefreshAction;
	private boolean disableAutomaticRefresh = false;
	
	private Job currentJob;
	
	private VpvVisualModel visualModel;
	private int modelHolderId;

	private EditorListener editorListener;
	private SelectionListener selectionListener;
	
	private IEditorPart currentEditor;

	private IDocumentListener documentListener;
	
	
	public VpvView() {
		setModelHolderId(Activator.getDefault().getVisualModelHolderRegistry().registerHolder(this));
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
		browser.addProgressListener(new ProgressAdapter() {
			@Override
			public void completed(ProgressEvent event) {
				ISelection currentSelection = getCurrentSelection();
				updateSelectionAndScrollToIt(currentSelection);
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
		manager.add(disableAutomaticRefreshAction);
	}

	private void inizializeEditorListener(Browser browser, int modelHolderId ) {
		editorListener = new EditorListener();
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
	
	public void editorChanged(IEditorPart editor) {
		if (currentEditor == editor) {
			// do nothing
		} else if (editor == null) {
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
		if (this.currentEditor != null) {
			IDocument document = (IDocument) this.currentEditor.getAdapter(IDocument.class);
			if (document != null) {
				document.removeDocumentListener(getDocumentListener());
			}
		}
		
		this.currentEditor = currentEditor;
		
		if (this.currentEditor != null) {
			IDocument document = (IDocument) this.currentEditor.getAdapter(IDocument.class);
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
					if (!disableAutomaticRefresh) { 
						if (currentJob == null || currentJob.getState() != Job.WAITING) {
							if (currentJob != null && currentJob.getState() == Job.SLEEPING) {
								currentJob.cancel();
							}
							currentJob = createPreviewUpdateJob();
						}

						currentJob.schedule(500);
					
//					IDocument document = event.getDocument();
//					if (document != null) 	{
//						int startSelectionPosition = event.getOffset();
//						int endSelectionPosition = startSelectionPosition + event.getText().length();
//
//						Node firstSelectedNode = getNodeBySourcePosition(document, startSelectionPosition);
//						Node lastSelectedNode = getNodeBySourcePosition(document, endSelectionPosition);
//
//						processNodes(firstSelectedNode, lastSelectedNode);
//					}
					}
				}
				

				
//				private void processNodes(Node firstSelectedNode, Node lastSelectedNode) {
//					if ((firstSelectedNode == null) || (lastSelectedNode == null)) {;
//						sourceDomChanged(getRootNode(firstSelectedNode)); // rebuild the whole document
//					} else {
//						Node commonNode = getCommonNode(firstSelectedNode, lastSelectedNode);
//						if (commonNode != null){
//							sourceDomChanged(commonNode);
//						}
//					}
//				}

//				private Node getCommonNode(Node firstNode, Node secondNode) {
//					if (firstNode == secondNode) {
//						return firstNode;
//					} else {
//						Set<Node> firstNodeParents = getParentNodes(firstNode);	
//						firstNodeParents.add(firstNode); // firstSelectedNode could be parent node for lastSelectedNode
//						
//						Node commonNode = null;
//						Node secondNodeParent = secondNode;
//						while (secondNodeParent != null && commonNode == null) {
//							if (firstNodeParents.contains(secondNodeParent)) {
//								commonNode = secondNodeParent;
//							}
//							secondNodeParent = getParentNode(secondNodeParent);
//						}
//	
//						if (commonNode == null) {
//							commonNode = getRootNode(firstNode);
//						}
//						
//						return commonNode;
//					}
//				}

			};
		}

		return documentListener;
	}
	
	private void makeActions() {
		makeRefreshAction();
		makeOpenInDefaultBrowserAction();
		makeDisableAutomaticRefreshAction();

	}

	private void makeDisableAutomaticRefreshAction() {
		disableAutomaticRefreshAction = new Action(Messages.VpvView_DISABLE_AUTOMATIC_REFRESH, IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				if (disableAutomaticRefreshAction.isChecked()) {
					disableAutomaticRefresh = true;
				} else {
					disableAutomaticRefresh = false;
					refresh(browser);
				}
			}
		};

		disableAutomaticRefreshAction.setChecked(false);
		disableAutomaticRefreshAction.setImageDescriptor(Activator.getImageDescriptor("icons/automatic_refresh.gif"));
	}

	private void makeOpenInDefaultBrowserAction() {
		openInDefaultBrowserAction = new Action() {
			public void run(){
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
		openInDefaultBrowserAction.setImageDescriptor(Activator.getImageDescriptor("icons/open_in_default_browser.gif"));
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
	
	private void refresh(Browser browser){
		browser.setUrl(browser.getUrl());
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
			IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.getActiveEditor();
			formRequestToServer(activeEditor);
		}

	}
	
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
		if (currentEditor != null) {
			editorModel = (IDOMModel) currentEditor.getAdapter(IDOMModel.class);
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
	
	@SuppressWarnings("restriction")
	private Node getNodeBySourcePosition(IDocument document, int position) {
		IStructuredModel model = null;
		Node node = null;
		try {
			model = StructuredModelManager.getModelManager().getExistingModelForRead(document);
			if (model != null) {
				IndexedRegion indexedRegion = model.getIndexedRegion(position);
				if (indexedRegion instanceof Node) {
					node  = (Node) indexedRegion;
				}
			}
		} finally {
			if (model != null) {
				model.releaseFromRead();
			}
		}
		
		return node;
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
			selectionStyle = "";
		} else {
			selectionStyle = "'[" + VpvDomBuilder.ATTR_VPV_ID + "=\"" + currentSelectionId + "\"] {outline: 2px solid blue;}'";
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
			"})(" + selectionStyle + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
}
