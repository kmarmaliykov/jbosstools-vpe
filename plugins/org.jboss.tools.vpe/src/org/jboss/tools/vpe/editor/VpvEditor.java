package org.jboss.tools.vpe.editor;

import static org.jboss.tools.vpe.preview.core.server.HttpConstants.ABOUT_BLANK;
import static org.jboss.tools.vpe.preview.core.server.HttpConstants.HTTP;
import static org.jboss.tools.vpe.preview.core.server.HttpConstants.LOCALHOST;
import static org.jboss.tools.vpe.preview.core.server.HttpConstants.PROJECT_NAME;
import static org.jboss.tools.vpe.preview.core.server.HttpConstants.VIEW_ID;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.jboss.tools.jst.web.ui.WebUiPlugin;
import org.jboss.tools.jst.web.ui.internal.editor.preferences.IVpePreferencesPage;
import org.jboss.tools.vpe.VpePlugin;
import org.jboss.tools.vpe.editor.mozilla.MozillaEditor;
import org.jboss.tools.vpe.editor.mozilla.listener.EditorLoadWindowListener;
import org.jboss.tools.vpe.editor.preferences.VpeEditorPreferencesPage;
import org.jboss.tools.vpe.editor.preferences.VpeResourcesDialogFactory;
import org.jboss.tools.vpe.editor.toolbar.IVpeToolBarManager;
import org.jboss.tools.vpe.editor.toolbar.VpeToolBarManager;
import org.jboss.tools.vpe.editor.toolbar.format.FormatControllerManager;
import org.jboss.tools.vpe.editor.toolbar.format.TextFormattingToolBar;
import org.jboss.tools.vpe.editor.util.FileUtil;
import org.jboss.tools.vpe.messages.VpeUIMessages;
import org.jboss.tools.vpe.preview.core.transform.DomUtil;
import org.jboss.tools.vpe.preview.core.transform.TransformUtil;
import org.jboss.tools.vpe.preview.core.transform.VpvDomBuilder;
import org.jboss.tools.vpe.preview.core.transform.VpvVisualModel;
import org.jboss.tools.vpe.preview.core.transform.VpvVisualModelHolder;
import org.jboss.tools.vpe.preview.core.util.EditorUtil;
import org.jboss.tools.vpe.preview.core.util.NavigationUtil;
import org.jboss.tools.vpe.preview.core.util.SuitableFileExtensions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class VpvEditor extends EditorPart implements VpvVisualModelHolder, IReusableEditor{
	/**
	 * 
	 */
	protected static final File INIT_FILE = new File(VpePlugin.getDefault().getResourcePath("ve"), "init.html"); //$NON-NLS-1$ //$NON-NLS-2$
	public static final String CONTENT_AREA_ID = "__content__area__"; //$NON-NLS-1$
	
	/*
	 * Paths for tool bar icons
	 */
	public static final String ICON_PREFERENCE = "icons/preference.gif"; //$NON-NLS-1$
	public static final String ICON_PREFERENCE_DISABLED = "icons/preference_disabled.gif"; //$NON-NLS-1$
	public static final String ICON_REFRESH = "icons/refresh.gif"; //$NON-NLS-1$
	public static final String ICON_REFRESH_DISABLED = "icons/refresh_disabled.gif"; //$NON-NLS-1$
	public static final String ICON_PAGE_DESIGN_OPTIONS = "icons/point_to_css.gif"; //$NON-NLS-1$
	public static final String ICON_PAGE_DESIGN_OPTIONS_DISABLED = "icons/point_to_css_disabled.gif"; //$NON-NLS-1$
	public static final String ICON_ORIENTATION_SOURCE_LEFT = "icons/source_left.gif"; //$NON-NLS-1$
	public static final String ICON_ORIENTATION_SOURCE_TOP = "icons/source_top.gif"; //$NON-NLS-1$
	public static final String ICON_ORIENTATION_VISUAL_LEFT = "icons/visual_left.gif"; //$NON-NLS-1$
	public static final String ICON_ORIENTATION_VISUAI_TOP = "icons/visual_top.gif"; //$NON-NLS-1$
	public static final String ICON_ORIENTATION_SOURCE_LEFT_DISABLED = "icons/source_left_disabled.gif"; //$NON-NLS-1$
	public static final String ICON_SELECTION_BAR = "icons/selbar.gif"; //$NON-NLS-1$
	public static final String ICON_TEXT_FORMATTING = "icons/text-formatting.gif"; //$NON-NLS-1$
	public static final String ICON_SCROLL_LOCK= "icons/scroll_lock.gif"; //$NON-NLS-1$
	
	private static Map<String, String> layoutIcons;
	private static Map<String, String> layoutNames;
	private static List<String> layoutValues;
	private int currentOrientationIndex = 1;
	private Action openVPEPreferencesAction;
	private Action visualRefreshAction;
	private Action showResouceDialogAction;
	private Action rotateEditorsAction;
	private Action showSelectionBarAction;
	private Action showTextFormattingAction;
	private Action scrollLockAction;
	static {
		/*
		 * Values from <code>layoutValues</code> should correspond to the order
		 * when increasing the index of the array will cause 
		 * the source editor rotation 
		 */
	    layoutIcons = new HashMap<String, String>();
	    layoutIcons.put(IVpePreferencesPage.SPLITTING_HORIZ_LEFT_SOURCE_VALUE, ICON_ORIENTATION_SOURCE_LEFT);
	    layoutIcons.put(IVpePreferencesPage.SPLITTING_VERT_TOP_SOURCE_VALUE, ICON_ORIENTATION_SOURCE_TOP);
	    layoutIcons.put(IVpePreferencesPage.SPLITTING_HORIZ_LEFT_VISUAL_VALUE, ICON_ORIENTATION_VISUAL_LEFT);
	    layoutIcons.put(IVpePreferencesPage.SPLITTING_VERT_TOP_VISUAL_VALUE, ICON_ORIENTATION_VISUAI_TOP);
	    
	    layoutNames = new HashMap<String, String>();
	    layoutNames.put(IVpePreferencesPage.SPLITTING_HORIZ_LEFT_SOURCE_VALUE, VpeUIMessages.SPLITTING_HORIZ_LEFT_SOURCE_TOOLTIP);
	    layoutNames.put(IVpePreferencesPage.SPLITTING_VERT_TOP_SOURCE_VALUE, VpeUIMessages.SPLITTING_VERT_TOP_SOURCE_TOOLTIP);
	    layoutNames.put(IVpePreferencesPage.SPLITTING_HORIZ_LEFT_VISUAL_VALUE, VpeUIMessages.SPLITTING_HORIZ_LEFT_VISUAL_TOOLTIP);
	    layoutNames.put(IVpePreferencesPage.SPLITTING_VERT_TOP_VISUAL_VALUE, VpeUIMessages.SPLITTING_VERT_TOP_VISUAL_TOOLTIP);

	    layoutValues= new ArrayList<String>();
	    layoutValues.add(IVpePreferencesPage.SPLITTING_HORIZ_LEFT_SOURCE_VALUE);
	    layoutValues.add(IVpePreferencesPage.SPLITTING_VERT_TOP_SOURCE_VALUE);
	    layoutValues.add(IVpePreferencesPage.SPLITTING_HORIZ_LEFT_VISUAL_VALUE);
	    layoutValues.add(IVpePreferencesPage.SPLITTING_VERT_TOP_VISUAL_VALUE);

	}
	
	private FormatControllerManager formatControllerManager = new FormatControllerManager();
	private VPVController controller;
	private ToolBar verBar = null;
	private IPropertyChangeListener selectionBarCloseListener;
	
	private IVpeToolBarManager vpeToolBarManager;
	
	private EditorLoadWindowListener editorLoadWindowListener;
	
	private Browser browser;
	private VpvVisualModel visualModel;
	private int modelHolderId;
	private SelectionListener selectionListener;
	private IEditorPart sourceEditor;
	
	public VpvEditor(IEditorPart sourceEditor) {
		setModelHolderId(Activator.getDefault().getVisualModelHolderRegistry().registerHolder(this));
		this.sourceEditor = sourceEditor;
	}
	
	@Override
	public void doSave(IProgressMonitor arg0) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.setSite(site);
		super.setInput(input);
	}

	public void setInput(IEditorInput input) {
		boolean isVisualRefreshRequired = (getEditorInput() != null && getEditorInput() != input && controller != null);
		super.setInput(input);
		if(isVisualRefreshRequired) controller.visualRefresh();
	}
	
	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	public void setController(VPVController controller){
		this.controller = controller;
		formatControllerManager.setVpeController(controller);
		controller.setToolbarFormatControllerManager(formatControllerManager);
	}
	
	public ToolBar createVisualToolbar(Composite parent) {
		final ToolBarManager toolBarManager = new ToolBarManager(SWT.VERTICAL | SWT.FLAT);
		verBar = toolBarManager.createControl(parent);
		
		/*
		 * Create OPEN VPE PREFERENCES tool bar item
		 */
		openVPEPreferencesAction = new Action(VpeUIMessages.PREFERENCES,
				IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				VpeEditorPreferencesPage.openPreferenceDialog();
			}
		};
		openVPEPreferencesAction.setImageDescriptor(ImageDescriptor.createFromFile(MozillaEditor.class,
				ICON_PREFERENCE));
		openVPEPreferencesAction.setToolTipText(VpeUIMessages.PREFERENCES);
		toolBarManager.add(openVPEPreferencesAction);
		
		/*
		 * Create VPE VISUAL REFRESH tool bar item
		 */
		visualRefreshAction = new Action(VpeUIMessages.REFRESH,
				IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				if (controller != null) {
					controller.visualRefresh();
				}
			}
		};
		visualRefreshAction.setImageDescriptor(ImageDescriptor.createFromFile(MozillaEditor.class,
				ICON_REFRESH));
		visualRefreshAction.setToolTipText(VpeUIMessages.REFRESH);
		toolBarManager.add(visualRefreshAction);
		
		/*
		 * Create SHOW RESOURCE DIALOG tool bar item
		 * 
		 * https://jira.jboss.org/jira/browse/JBIDE-3966
		 * Disabling Page Design Options for external files. 
		 */
		IEditorInput input = getEditorInput();
		IFile file = null;
		if (input instanceof IFileEditorInput) {
			file = ((IFileEditorInput) input).getFile();
		} else if (input instanceof ILocationProvider) {
			ILocationProvider provider = (ILocationProvider) input;
			IPath path = provider.getPath(input);
			if (path != null) {
			    file = FileUtil.getFile(input, path.lastSegment());
			}
		}
		boolean fileExistsInWorkspace = ((file != null) && (file.exists()));
		showResouceDialogAction = new Action(VpeUIMessages.PAGE_DESIGN_OPTIONS,
				IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				VpeResourcesDialogFactory.openVpeResourcesDialog(getEditorInput());
			}
		};
		showResouceDialogAction.setImageDescriptor(ImageDescriptor.createFromFile(MozillaEditor.class,
				fileExistsInWorkspace ? ICON_PAGE_DESIGN_OPTIONS : ICON_PAGE_DESIGN_OPTIONS_DISABLED));
		if (!fileExistsInWorkspace) {
			showResouceDialogAction.setEnabled(false);
		}
		showResouceDialogAction.setToolTipText(VpeUIMessages.PAGE_DESIGN_OPTIONS);
		toolBarManager.add(showResouceDialogAction);
		
		
		/*
		 * Create ROTATE EDITORS tool bar item
		 * 
		 * https://jira.jboss.org/jira/browse/JBIDE-4152
		 * Compute initial icon state and add it to the tool bar.
		 */
		String newOrientation = WebUiPlugin
		.getDefault().getPreferenceStore().getString(
				IVpePreferencesPage.VISUAL_SOURCE_EDITORS_SPLITTING);
		currentOrientationIndex = layoutValues.indexOf(newOrientation);
		rotateEditorsAction = new Action(
				VpeUIMessages.VISUAL_SOURCE_EDITORS_SPLITTING,
				IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				/*
				 * Rotate editors orientation clockwise.
				 */
		    	currentOrientationIndex++;
				if (currentOrientationIndex >= layoutValues.size()) {
					currentOrientationIndex = currentOrientationIndex % layoutValues.size();
				}
				String newOrientation = layoutValues.get(currentOrientationIndex);
				/*
				 * Update icon and tooltip
				 */
				this.setImageDescriptor(ImageDescriptor.createFromFile(
						MozillaEditor.class, layoutIcons.get(newOrientation)));
				
				this.setToolTipText(layoutNames.get(newOrientation));
				/*
				 * Call <code>filContainer()</code> from VpeEditorPart
				 * to redraw CustomSashForm with new layout.
				 */
				((VpvEditor2)getController().getPageContext().getEditPart()).fillContainer(true, newOrientation);
				WebUiPlugin.getDefault().getPreferenceStore().
					setValue(IVpePreferencesPage.VISUAL_SOURCE_EDITORS_SPLITTING, newOrientation);
			}
		};
		rotateEditorsAction.setImageDescriptor(ImageDescriptor.createFromFile(MozillaEditor.class,
				layoutIcons.get(newOrientation)));
		rotateEditorsAction.setToolTipText(layoutNames.get(newOrientation));
		toolBarManager.add(rotateEditorsAction);
	
		/*
		 * Create SHOW TEXT FORMATTING tool bar item
		 */
		showTextFormattingAction = new Action(
				VpeUIMessages.SHOW_TEXT_FORMATTING, IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				/*
				 * Update Text Formatting Bar 
				 */
				vpeToolBarManager.setToolbarVisibility(this.isChecked());
				WebUiPlugin.getDefault().getPreferenceStore().
				setValue(IVpePreferencesPage.SHOW_TEXT_FORMATTING, this.isChecked());
			}
		};
		showTextFormattingAction.setImageDescriptor(ImageDescriptor.createFromFile(MozillaEditor.class,
				ICON_TEXT_FORMATTING));
		showTextFormattingAction.setToolTipText(VpeUIMessages.SHOW_TEXT_FORMATTING);
		toolBarManager.add(showTextFormattingAction);
		
		/*
		 * https://issues.jboss.org/browse/JBIDE-11302
		 * Create SYNCHRONIZE_SCROLLING_BETWEEN_SOURCE_VISUAL_PANES tool bar item
		 */
		scrollLockAction = new Action(
				VpeUIMessages.SYNCHRONIZE_SCROLLING_BETWEEN_SOURCE_VISUAL_PANES,
				IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				/*
				 * Change the enabled state, listeners in VpeController will do the rest
				 */
				WebUiPlugin.getDefault().getPreferenceStore().setValue(
						IVpePreferencesPage.SYNCHRONIZE_SCROLLING_BETWEEN_SOURCE_VISUAL_PANES,
						this.isChecked());
			}
		};
		scrollLockAction.setImageDescriptor(ImageDescriptor.createFromFile(
				MozillaEditor.class, ICON_SCROLL_LOCK));
		scrollLockAction.setToolTipText(VpeUIMessages.SYNCHRONIZE_SCROLLING_BETWEEN_SOURCE_VISUAL_PANES);
		toolBarManager.add(scrollLockAction);

		/*
		 * Create SHOW SELECTION BAR tool bar item
		 */
		showSelectionBarAction = new Action(VpeUIMessages.SHOW_SELECTION_BAR,
				IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				/*
				 * Update Selection Bar 
				 */
				((VpvEditor2)controller.getPageContext().getEditPart()).updateSelectionBar(this.isChecked());
				WebUiPlugin.getDefault().getPreferenceStore().
					setValue(IVpePreferencesPage.SHOW_SELECTION_TAG_BAR, this.isChecked());
			}
		};
		
		selectionBarCloseListener = new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				/*
				 * Change icon state after sel bar was closed
				 */
				if (IVpePreferencesPage.SHOW_SELECTION_TAG_BAR.equalsIgnoreCase(event.getProperty())) {
					boolean newValue = (Boolean) event.getNewValue();
					if (showSelectionBarAction.isChecked() != newValue) {
						showSelectionBarAction.setChecked(newValue);
					}
				}
			}
		};
		WebUiPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(selectionBarCloseListener);
		
		showSelectionBarAction.setImageDescriptor(ImageDescriptor.createFromFile(MozillaEditor.class,
				ICON_SELECTION_BAR));
		showSelectionBarAction.setToolTipText(VpeUIMessages.SHOW_SELECTION_BAR);
		toolBarManager.add(showSelectionBarAction);
		
		updateToolbarItemsAccordingToPreferences();
		toolBarManager.update(true);

		parent.addDisposeListener(new DisposeListener() {
			
			public void widgetDisposed(DisposeEvent e) {
				toolBarManager.dispose();
				toolBarManager.removeAll();
				openVPEPreferencesAction = null;
				visualRefreshAction = null;
				showResouceDialogAction = null;
				rotateEditorsAction = null;;
				showSelectionBarAction = null;
				showTextFormattingAction = null;
			}
		});
		return verBar;
	}
	
	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(2,false);
		layout.marginHeight = 0;
		layout.marginWidth = 2;
		layout.verticalSpacing = 2;
		layout.horizontalSpacing = 2;
		layout.marginBottom = 0;
		parent.setLayout(layout);

		// Editors and Toolbar composite 
		Composite cmpEdTl = new Composite(parent, SWT.NONE);
		GridLayout layoutEdTl = new GridLayout(1, false);
		layoutEdTl.verticalSpacing = 0;
		layoutEdTl.marginHeight = 0;
		layoutEdTl.marginBottom = 3;
		layoutEdTl.marginWidth = 0;
		cmpEdTl.setLayout(layoutEdTl);
		cmpEdTl.setLayoutData(new GridData(GridData.FILL_BOTH));

		/*
		 * https://jira.jboss.org/jira/browse/JBIDE-4429
		 * Toolbar was moved to VpeEditorPart.
		 *  'verBar' should be created in createVisualToolbar(..) in VpeEditorPart
		 *  and only after that MozillaEditor should be created itself. 
		 */
		if (null != verBar) {
			// Use vpeToolBarManager to create a horizontal toolbar.
			vpeToolBarManager = new VpeToolBarManager();
			if (vpeToolBarManager != null) {
				vpeToolBarManager.createToolBarComposite(cmpEdTl);
				vpeToolBarManager.addToolBar(new TextFormattingToolBar(formatControllerManager));
			}
		}

		//Create a composite to the Editor
		final Composite cmpEd = new Composite (cmpEdTl, SWT.NATIVE);
		GridLayout layoutEd = new GridLayout(1, false);
		layoutEd.marginBottom = 0;
		layoutEd.marginHeight = 1;
		layoutEd.marginWidth = 0;
		layoutEd.marginRight = 0;
		layoutEd.marginLeft = 1;
		layoutEd.verticalSpacing = 0;
		layoutEd.horizontalSpacing = 0;
		cmpEd.setLayout(layoutEd);
		cmpEd.setLayoutData(new GridData(GridData.FILL_BOTH));

		//TODO Add a paintListener to cmpEd and give him a border top and left only
		Color buttonDarker = parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
		cmpEd.setBackground(buttonDarker);

		browser = new Browser(cmpEd, SWT.NONE);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		browser.addProgressListener(new ProgressAdapter() {
			@Override
			public void completed(ProgressEvent event) {
				ISelection currentSelection = getCurrentSelection();
				updateSelectionAndScrollToIt(currentSelection);
				if (editorLoadWindowListener != null) {
					editorLoadWindowListener.load();
				}
			}
		});
		inizializeSelectionListener();
	}

	private void inizializeSelectionListener() {
		selectionListener = new SelectionListener();
		getSite().getPage().addSelectionListener(selectionListener);	
	}
	
	private ISelection getCurrentSelection() {
		Activator activator = Activator.getDefault();
		IWorkbench workbench = activator.getWorkbench();
		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
		ISelectionService selectionService = workbenchWindow.getSelectionService();
		ISelection selection = selectionService.getSelection();
		return selection;
	}
	
	private void updateSelectionAndScrollToIt(ISelection currentSelection) {
		if (currentSelection instanceof IStructuredSelection) {
			Node sourceNode = getNodeFromSelection((IStructuredSelection) currentSelection);
			Long currentSelectionId = getIdForSelection(sourceNode, visualModel);
			updateBrowserSelection(currentSelectionId);
			scrollToId(currentSelectionId);
		}
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
	@Override
	public void setFocus() {
		if (browser != null) {
			browser.setFocus();
		}
	}
	
	public void dispose() {
		WebUiPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(selectionBarCloseListener);
		
		if (vpeToolBarManager != null) {
			vpeToolBarManager.dispose();
			vpeToolBarManager = null;
		}
		
//		removeDomEventListeners();
		if(controller != null) {
			((VPVController)controller).dispose();
			controller = null;
		}
		if (browser != null) {
			browser.dispose();
			browser = null;
		}

		formatControllerManager.setVpeController(null);
		formatControllerManager=null;
		super.dispose();
	}

	public void updateToolbarItemsAccordingToPreferences() {
		String prefsOrientation = WebUiPlugin
		.getDefault().getPreferenceStore().getString(
				IVpePreferencesPage.VISUAL_SOURCE_EDITORS_SPLITTING);
		int prefsOrientationIndex = layoutValues.indexOf(prefsOrientation);
		
		boolean prefsShowTextFormatting = WebUiPlugin.getDefault().getPreferenceStore()
				.getBoolean(IVpePreferencesPage.SHOW_TEXT_FORMATTING);
		boolean prefsShowSelectionBar = WebUiPlugin.getDefault().getPreferenceStore()
				.getBoolean(IVpePreferencesPage.SHOW_SELECTION_TAG_BAR);
		boolean scrollLockEditors = WebUiPlugin.getDefault().getPreferenceStore()
				.getBoolean(IVpePreferencesPage.SYNCHRONIZE_SCROLLING_BETWEEN_SOURCE_VISUAL_PANES);
		
		if (showSelectionBarAction != null) {
			showSelectionBarAction.setChecked(prefsShowSelectionBar);
		}
		if (showTextFormattingAction != null) {
			showTextFormattingAction.setChecked(prefsShowTextFormatting);
			if (vpeToolBarManager != null) {
				// JBIDE-14756 Selection/reset of 'Show Text Formatting Bar' in VPE preferences does not Show/Hide 'Show Text Formatting Bar' in editor
				vpeToolBarManager.setToolbarVisibility(prefsShowTextFormatting);
			}
		}
		if (scrollLockAction != null) {
			scrollLockAction.setChecked(scrollLockEditors);
		}
		if (rotateEditorsAction != null) {
			currentOrientationIndex = prefsOrientationIndex;
			rotateEditorsAction.setImageDescriptor(ImageDescriptor.createFromFile(
					MozillaEditor.class, layoutIcons.get(prefsOrientation)));
			rotateEditorsAction.setToolTipText(layoutNames.get(prefsOrientation));
		}
	}
	
	public void reload() {
		formRequestToServer(sourceEditor);
	}
	
	private void formRequestToServer(IEditorPart editor) {
		IFile ifile = getFileOpenedInEditor(editor);
		if (ifile != null && SuitableFileExtensions.contains(ifile.getFileExtension().toString())) {
			String url = formUrl(ifile);
			browser.setUrl(url, null, new String[] {"Cache-Control: no-cache"}); //$NON-NLS-1$
		} else {
			browser.setUrl(ABOUT_BLANK);
		}
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
	
	/**
	 * @return the controller
	 */
	public VPVController getController() {
		return controller;
	}
	
	@Override
	public void setVisualModel(VpvVisualModel visualModel) {
		this.visualModel = visualModel;
	}
	
	public void setModelHolderId(int modelHolderId) {
		this.modelHolderId = modelHolderId;
	}
	
	public Browser getBrowser() {
		return browser;
	}
	
	public void setEditorLoadWindowListener(EditorLoadWindowListener listener) {
		editorLoadWindowListener = listener;
	}

	private class SelectionListener implements ISelectionListener {

		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (selection instanceof IStructuredSelection && isInCurrentEditor((IStructuredSelection) selection)) {
				updateSelectionAndScrollToIt(selection);
			}
		}
	}
}
