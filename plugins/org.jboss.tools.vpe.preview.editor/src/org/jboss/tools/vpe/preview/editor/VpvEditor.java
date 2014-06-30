/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.vpe.preview.editor;

import static org.jboss.tools.vpe.preview.core.server.HttpConstants.ABOUT_BLANK;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.progress.UIJob;
import org.jboss.tools.jst.web.ui.WebUiPlugin;
import org.jboss.tools.jst.web.ui.internal.editor.preferences.IVpePreferencesPage;
import org.jboss.tools.vpe.VpePlugin;
import org.jboss.tools.vpe.editor.mozilla.MozillaEditor;
import org.jboss.tools.vpe.editor.mozilla.listener.EditorLoadWindowListener;
import org.jboss.tools.vpe.editor.preferences.VpeEditorPreferencesPage;
import org.jboss.tools.vpe.editor.toolbar.IVpeToolBarManager;
import org.jboss.tools.vpe.editor.toolbar.VpeToolBarManager;
import org.jboss.tools.vpe.editor.toolbar.format.FormatControllerManager;
import org.jboss.tools.vpe.editor.toolbar.format.TextFormattingToolBar;
import org.jboss.tools.vpe.messages.VpeUIMessages;
import org.jboss.tools.vpe.preview.core.transform.VpvVisualModel;
import org.jboss.tools.vpe.preview.core.transform.VpvVisualModelHolder;
import org.jboss.tools.vpe.preview.core.util.ActionBarUtil;
import org.jboss.tools.vpe.preview.core.util.EditorUtil;
import org.jboss.tools.vpe.preview.core.util.NavigationUtil;
import org.jboss.tools.vpe.preview.core.util.SuitableFileExtensions;

/**
 * @author Konstantin Marmalyukov (kmarmaliykov)
 */

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
	private Action rotateEditorsAction;
	private Action showSelectionBarAction;
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
	
	private final ToolBarManager toolBarManager = new ToolBarManager(SWT.VERTICAL | SWT.FLAT);
	private FormatControllerManager formatControllerManager = new FormatControllerManager();
	private VpvEditorController controller;
	private ToolBar verBar = null;
	private IPropertyChangeListener selectionBarCloseListener;
	
	private IVpeToolBarManager vpeToolBarManager;
	
	private EditorLoadWindowListener editorLoadWindowListener;
	
	private Browser browser;
	private VpvVisualModel visualModel;
	private int modelHolderId;
	private SelectionListener selectionListener;
	private IEditorPart sourceEditor;
	private Job currentJob;
	
	private ActionBarUtil actionBarUtil;
	protected BrowserErrorWrapper errorWrapper = new BrowserErrorWrapper();
	public VpvEditor(IEditorPart sourceEditor) {
		setModelHolderId(Activator.getDefault().getVisualModelHolderRegistry().registerHolder(this));
		this.sourceEditor = sourceEditor;
		IDocument document = (IDocument) this.sourceEditor.getAdapter(IDocument.class);
		document.addDocumentListener(new IDocumentListener() {

				@Override
				public void documentAboutToBeChanged(DocumentEvent event) {
				}

				@Override
				public void documentChanged(DocumentEvent event) {
					if (actionBarUtil.isAutomaticRefreshEnabled() && controller.isVisualEditorVisible()) {
						updatePreview();
					}
				}

			});
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

	public void setController(VpvEditorController controller){
		this.controller = controller;
		formatControllerManager.setVpeController(controller);
		controller.setToolbarFormatControllerManager(formatControllerManager);
	}
	
	public ToolBar createVisualToolbar(Composite parent) {
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
				((VpvEditorPart)getController().getPageContext().getEditPart()).fillContainer(true, newOrientation);
				WebUiPlugin.getDefault().getPreferenceStore().
					setValue(IVpePreferencesPage.VISUAL_SOURCE_EDITORS_SPLITTING, newOrientation);
			}
		};
		rotateEditorsAction.setImageDescriptor(ImageDescriptor.createFromFile(MozillaEditor.class,
				layoutIcons.get(newOrientation)));
		rotateEditorsAction.setToolTipText(layoutNames.get(newOrientation));
		toolBarManager.add(rotateEditorsAction);
		
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
				((VpvEditorPart)controller.getPageContext().getEditPart()).updateSelectionBar(this.isChecked());
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
				rotateEditorsAction = null;;
				showSelectionBarAction = null;
			}
		});
		return verBar;
	}
	
	public void addPreviewToolbarItems() {
		actionBarUtil = new ActionBarUtil(browser);
		actionBarUtil.fillLocalToolBar(toolBarManager);
		toolBarManager.update(true);
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
//		if (null != verBar) {
//			// Use vpeToolBarManager to create a horizontal toolbar.
//			vpeToolBarManager = new VpeToolBarManager();
//			if (vpeToolBarManager != null) {
//				vpeToolBarManager.createToolBarComposite(cmpEdTl);
//				vpeToolBarManager.addToolBar(new TextFormattingToolBar(formatControllerManager));
//			}
//		}

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

		try {
			browser = new Browser(cmpEd, SWT.NONE);
			browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			browser.addLocationListener(new LocationAdapter() {
				@Override
				public void changed(LocationEvent event) {
					NavigationUtil.disableAlert(browser);
					NavigationUtil.disableLinks(browser);
					NavigationUtil.disableInputs(browser);
					
					ISelection currentSelection = getCurrentSelection();
					NavigationUtil.updateSelectionAndScrollToIt(currentSelection, browser, visualModel);
				}
			});
			
			browser.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseUp(MouseEvent event) {
					NavigationUtil.navigateToVisual(sourceEditor, browser, visualModel, event.x, event.y);
				}
				
			});
			
			if (editorLoadWindowListener != null) {
				editorLoadWindowListener.load();
			}
			
			inizializeSelectionListener();
		} catch (Throwable t) {
			//cannot create browser. show error message then
			
			/*
			 * Disable VPE toolbar
			 */
			if (verBar != null) {
				verBar.setEnabled(false);
			}
			errorWrapper.showError(cmpEd, t);
		}

	}
	
	private void inizializeSelectionListener() {
		selectionListener = new SelectionListener();
		getSite().getPage().addPostSelectionListener(selectionListener);	
	}
	
	private ISelection getCurrentSelection() {
		Activator activator = Activator.getDefault();
		IWorkbench workbench = activator.getWorkbench();
		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
		ISelectionService selectionService = workbenchWindow.getSelectionService();
		ISelection selection = selectionService.getSelection();
		return selection;
	}
	
	@Override
	public void setFocus() {
		if (browser != null) {
			browser.setFocus();
		}
	}
	
	public void dispose() {
		WebUiPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(selectionBarCloseListener);
		if (selectionListener != null) {
			getSite().getPage().removeSelectionListener(selectionListener);
		}
		Activator.getDefault().getVisualModelHolderRegistry().unregisterHolder(this);
		
		if (vpeToolBarManager != null) {
			vpeToolBarManager.dispose();
			vpeToolBarManager = null;
		}
		
		if(controller != null) {
			controller.dispose();
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
		
		boolean prefsShowSelectionBar = WebUiPlugin.getDefault().getPreferenceStore()
				.getBoolean(IVpePreferencesPage.SHOW_SELECTION_TAG_BAR);
		
		if (showSelectionBarAction != null) {
			showSelectionBarAction.setChecked(prefsShowSelectionBar);
		}
		if (rotateEditorsAction != null) {
			currentOrientationIndex = prefsOrientationIndex;
			rotateEditorsAction.setImageDescriptor(ImageDescriptor.createFromFile(
					MozillaEditor.class, layoutIcons.get(prefsOrientation)));
			rotateEditorsAction.setToolTipText(layoutNames.get(prefsOrientation));
		}
	}
	
	public void reload() {
		if (browser != null)
			formRequestToServer();
//		browser.setUrl(browser.getUrl());
	}
	
	private void formRequestToServer() {
		IFile ifile = EditorUtil.getFileOpenedInEditor(sourceEditor);
		if (ifile != null && SuitableFileExtensions.contains(ifile.getFileExtension().toString())) {
			String url;
			try {
				url = EditorUtil.formUrl(ifile, modelHolderId, "" + Activator.getDefault().getServer().getPort()); //$NON-NLS-1$
				browser.setUrl(url);
			} catch (UnsupportedEncodingException e) {
				Activator.logError(e);
			}
		} else {
			browser.setUrl(ABOUT_BLANK);
		}
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
	
	private Job createPreviewUpdateJob() {
		Job job = new UIJob("Preview Update") { //$NON-NLS-1$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (!browser.isDisposed()) {
					browser.setUrl(browser.getUrl());
				}
				return Status.OK_STATUS;
			}
		};
		return job;
	}
	
	/**
	 * @return the controller
	 */
	public VpvEditorController getController() {
		return controller;
	}
	
	@Override
	public void setVisualModel(VpvVisualModel visualModel) {
		this.visualModel = visualModel;
	}
	
	public void setModelHolderId(int modelHolderId) {
		this.modelHolderId = modelHolderId;
	}
	
	public void setBrowser(Browser browser) {
		this.browser = browser;
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
			if (selection instanceof IStructuredSelection && EditorUtil.isInCurrentEditor((IStructuredSelection) selection, sourceEditor)) {
				NavigationUtil.updateSelectionAndScrollToIt(selection, browser, visualModel);
			}
		}
	}
}
