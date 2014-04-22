package org.jboss.tools.vpe.editor;

/**
 * @author Yahor Radtsevich (yradtsevich)
 */

import org.eclipse.compare.Splitter;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IStatusField;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.jboss.tools.common.model.ui.editor.IModelObjectEditorInput;
import org.jboss.tools.jst.web.ui.WebUiPlugin;
import org.jboss.tools.jst.web.ui.internal.editor.bundle.BundleMap;
import org.jboss.tools.jst.web.ui.internal.editor.editor.IVisualController;
import org.jboss.tools.jst.web.ui.internal.editor.editor.IVisualEditor;
import org.jboss.tools.jst.web.ui.internal.editor.jspeditor.JSPMultiPageEditor;
import org.jboss.tools.jst.web.ui.internal.editor.jspeditor.StorageRevisionEditorInputAdapter;
import org.jboss.tools.jst.web.ui.internal.editor.preferences.IVpePreferencesPage;
import org.jboss.tools.jst.web.ui.internal.editor.selection.bar.SelectionBar;
import org.jboss.tools.vpe.IVpeHelpContextIds;
import org.jboss.tools.vpe.VpePlugin;
import org.jboss.tools.vpe.editor.mozilla.listener.EditorLoadWindowListener;
import org.jboss.tools.vpe.editor.toolbar.IVpeToolBarManager;
import org.jboss.tools.vpe.editor.toolbar.format.FormatControllerManager;
import org.jboss.tools.vpe.editor.util.VpeStyleUtil;
import org.jboss.tools.vpe.editor.xpl.CustomSashForm;
import org.jboss.tools.vpe.editor.xpl.CustomSashForm.ICustomSashFormListener;
import org.jboss.tools.vpe.editor.xpl.EditorSettings;
import org.jboss.tools.vpe.editor.xpl.SashSetting;
import org.jboss.tools.vpe.xulrunner.browser.XulRunnerBrowser;

public class VpvEditor2 extends EditorPart implements IVisualEditor2 {

	public static final String ID = "org.jboss.tools.vpe.vpv.views.VpvView"; //$NON-NLS-1$
	private Job currentJob;

	protected EditorSettings editorSettings;
//	private EditorListener editorListener;
//	private SelectionListener selectionListener;
	private SelectionBar selectionBar; // should be get from o.j.t.jst.jsp plugin
	private StructuredTextEditor sourceEditor;
	private int visualMode;
	private CustomSashForm container;
	private EditorPart multiPageEditor;
	private BundleMap bundleMap;
	//private IHandler sourceMaxmin,visualMaxmin, jumping;	
	private Composite cmpEd;
	private Composite cmpEdTl;
	private ControlListener controlListener;
	/*
	 * VPE visual components.
	 */
	private Composite sourceContent = null;
	private Composite visualContent = null;
	private Composite previewContent = null;
	private Splitter verticalToolbarSplitter = null;
	private Composite verticalToolbarEmpty = null;
	private ToolBar toolBar = null;
	
	IEditorPart activeEditor;
	
	private VpvEditor visualEditor;
	
	public VpvEditor2(EditorPart multiPageEditor, StructuredTextEditor textEditor, int visualMode, BundleMap bundleMap) {
		this.sourceEditor = textEditor;
		this.visualMode = visualMode;
		this.multiPageEditor = multiPageEditor;
		this.bundleMap = bundleMap;
	}
	
	public void addRulerContextMenuListener(IMenuListener listener) {
		if (sourceEditor != null)
			sourceEditor.addRulerContextMenuListener(listener);
	}

	public boolean isEditorInputReadOnly() {
		if (sourceEditor != null)
			return sourceEditor.isEditorInputReadOnly();
		else {
			return false;
		}
	}

	public void removeRulerContextMenuListener(IMenuListener listener) {
		if (sourceEditor != null)
			sourceEditor.removeRulerContextMenuListener(listener);
	}
	
	public void setStatusField(IStatusField field, String category) {
			sourceEditor.setStatusField(field, category);
	}
	
	public void doSave(IProgressMonitor monitor) {
		if (sourceEditor != null) {
			sourceEditor.doSave(monitor);
		}
	}

	public void doSaveAs() {
		if (sourceEditor != null) {
			sourceEditor.doSaveAs();
			setInput(sourceEditor.getEditorInput());
		}
	}	
	
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		if (editorSettings == null) {
			editorSettings = EditorSettings.getEditorSetting(this);
		} else if (input instanceof FileEditorInput) {
			editorSettings.setInput((FileEditorInput) input);
		}

	}
	
	@Override
	public void setInput(IEditorInput input) {
		super.setInput(input);
	}
	
	public boolean isDirty() {
		if (sourceEditor != null) {
			return sourceEditor.isDirty();
		} else {
			return false;
		}
	}

	public boolean isSaveAsAllowed() {
		if (sourceEditor != null) {
			return sourceEditor.isSaveAsAllowed();
		} else {
			return false;
		}
	}
	
	public void setVisualMode(int type) {
		switch (type) {
		case VISUALSOURCE_MODE:
			/*
			 * https://jira.jboss.org/browse/JBIDE-6832
			 * Restore the state after switching from Preview, for example.
			 */
//			selectionBar.setVisible(selectionBar.getAlwaysVisibleOption());
//			setVerticalToolbarVisible(true);
			setVerticalToolbarVisible(WebUiPlugin.getDefault().getPreferenceStore()
					.getBoolean(IVpePreferencesPage.SHOW_VISUAL_TOOLBAR));
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
			setVerticalToolbarVisible(false);
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
			setVerticalToolbarVisible(false);
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

			if (visualContent != null) {
				visualContent.setVisible(true);
                if (visualEditor != null) {
                    activeEditor = visualEditor;
                }
				container.setMaximizedControl(visualContent);
			}
			break;
		}
		if(visualEditor!=null&&visualEditor.getController()!=null){
			visualEditor.getController().refreshCommands();
		}
		container.layout();
		if (visualMode == SOURCE_MODE && type != SOURCE_MODE) {
			visualMode = type;
			if (visualEditor != null && visualEditor.getController() != null) {
				visualEditor.getController().visualRefresh();
				if(type!=PREVIEW_MODE) {
				visualEditor.getController().sourceSelectionChanged();
				}
			}
		}
		visualMode = type;
	}

	public int getVisualMode() {
		return visualMode;
	}
	
	/**
	 * Sets the visibility of the vertical toolbar for visual editor part.
	 * 
	 * @param visible if visible
	 */
	public void setVerticalToolbarVisible(boolean visible) {
		if ((null == verticalToolbarSplitter) || (null == verticalToolbarEmpty)
				|| (null == toolBar)) {
			return;
		}
		if (visible) {
			verticalToolbarSplitter.setVisible(toolBar, true);
			verticalToolbarSplitter.setVisible(verticalToolbarEmpty, false);
		} else {
			verticalToolbarSplitter.setVisible(toolBar, false);
			verticalToolbarSplitter.setVisible(verticalToolbarEmpty, true);
		}
		verticalToolbarSplitter.getParent().layout(true, true);
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
		cmpEdTl =  parent;
		GridLayout layoutEdTl = new GridLayout(2, false);
		layoutEdTl.verticalSpacing = 0;
		layoutEdTl.marginHeight = 0;
		layoutEdTl.marginBottom = 3;
		layoutEdTl.marginWidth = 0;
		cmpEdTl.setLayout(layoutEdTl);
		cmpEdTl.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		/*
		 * https://jira.jboss.org/jira/browse/JBIDE-4429
		 *  Composite for the left vertical toolbar
		 */
		verticalToolbarSplitter = new Splitter(cmpEdTl, SWT.NONE);
		GridLayout layout = new GridLayout(1,false);
		layout.marginHeight = 2;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;		
		layout.horizontalSpacing = 0;		
		verticalToolbarSplitter.setLayout(layout);
		verticalToolbarSplitter.setLayoutData(new GridData(SWT.CENTER, SWT.TOP | SWT.FILL, false, true, 1, 2));
		
		/*
		 * The empty vertical toolbar component
		 */
		verticalToolbarEmpty = new Composite(verticalToolbarSplitter, SWT.NONE) {
			public Point computeSize(int wHint, int hHint, boolean changed) {
				Point point = super.computeSize(wHint, hHint, changed);
				point.x = 1;
				return point;
			}
		};
		verticalToolbarEmpty.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		verticalToolbarEmpty.setVisible(true);

		/*
		 * The Visual Page Editor itself
		 */
		cmpEd = new Composite(cmpEdTl, SWT.BORDER);
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
		if (editorSettings != null) {
		    editorSettings.addSetting(new SashSetting(container));
		}		
		
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
				
//		// Create a preview content
//		previewContent = new Composite(container, SWT.NONE);
//		//previewContent.setLayout(new FillLayout());
//		previewContent.setLayout(new GridLayout());
		
		if (sourceEditor == null)
			sourceEditor = new StructuredTextEditor() {
				public void safelySanityCheckState(IEditorInput input) {
					super.safelySanityCheckState(input);
				}
			};
		container.setSashBorders(new boolean[] { true, true, true });

		controlListener = new ControlListener() {
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
			sourceEditor.addPropertyListener(new IPropertyListener() {
				public void propertyChanged(Object source, int propId) {
					if (propId == IWorkbenchPartConstants.PROP_TITLE) {
						VpvEditor2.this.setPartName(sourceEditor.getTitle());
					}
					VpvEditor2.this.firePropertyChange(propId);
				}
			});
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

			visualContent.addListener(SWT.Activate, new Listener() {
				public void handleEvent(Event event) {
					if (event.type == SWT.Activate) {
						if (visualEditor != null
								&& activeEditor != visualEditor) {
							activeEditor = visualEditor;
							setFocus();
						}
					}
				}
			});
			
			//TODO: ��� ��� �� �����
//			IWorkbenchWindow window = getSite().getWorkbenchWindow();
//			window.getPartService().addPartListener(activationListener);
//			window.getShell().addShellListener(activationListener);
			
		} catch (CoreException e) {
			VpePlugin.reportProblem(e);
		}
		// setVisualMode(visualMode);
		// ///////////////////////////////////////
		// ///// Add preference listener

		// ///////////////////////////////////////
		
		
		//editorChanged(activeEditor);

//		inizializeEditorListener(browser, modelHolderId);
//		
//		setCurrentEditor(activeEditor);
		if (editorSettings != null)
			editorSettings.apply();
		
		cmpEd.layout();
		
//		sourceMaxmin = new AbstractHandler() {
//			public Object execute(ExecutionEvent event)
//					throws ExecutionException {
//				if (getVisualMode() == IVisualEditor.VISUALSOURCE_MODE) {
//					Point p = visualContent.getSize();
//					if (p.x == 0 || p.y == 0) {
//						container.upClicked();
//					} else {
//						container.maxDown();
//					}
//				}
//				return null;
//			}
//		};
//		visualMaxmin = new AbstractHandler() {
//			public Object execute(ExecutionEvent event)
//					throws ExecutionException {
//				if (getVisualMode() == IVisualEditor.VISUALSOURCE_MODE) {
//					Point p = sourceContent.getSize();
//					if (p.x == 0 || p.y == 0) {
//						container.downClicked();
//					} else {
//						container.maxUp();
//					}
//				}
//				return null;
//			}
//		};
//		jumping = new AbstractHandler() {
//			public Object execute(ExecutionEvent event)
//					throws ExecutionException {
//				if (getVisualMode() == IVisualEditor.VISUALSOURCE_MODE) {
//					StructuredTextEditor editor = getSourceEditor();
//					if (editor == null)
//						return null;
//					StructuredTextViewer viewer = editor.getTextViewer();
//					if (viewer == null)
//						return null;
//					StyledText widget = viewer.getTextWidget();
//					if (widget == null || widget.isDisposed())
//						return null;
//					if (widget.isFocusControl()) {
//						if (visualEditor != null
//								&& activeEditor != visualEditor) {
//							activeEditor = visualEditor;
//							setFocus();
//							//visualContent.setFocus();
//						}
//					} else {
//						if (activeEditor != sourceEditor) {
//							activeEditor = sourceEditor;
//							setFocus();
//						}
//					}
//
//				}
//				return null;
//			}
//		};
		
		container.addCustomSashFormListener(new ICustomSashFormListener() {
			public void dividerMoved(int firstControlWeight, int secondControlWeight) {
				WebUiPlugin.getDefault().getPreferenceStore().
				setValue(IVpePreferencesPage.VISUAL_SOURCE_EDITORS_WEIGHTS, secondControlWeight);
			}
		});
		
		
	}

	/**
	 * Re-fills the VPE container according to new settings.
	 * 
	 * @param useCurrentEditorSettings
	 *            if <code>true</code> VPE will hold its current state 
	 *            otherwise values from preference page will be used,
	 * @param currentOrientation
	 *            current source-visual editors splitting value
	 */
	public void fillContainer(boolean useCurrentEditorSettings, String currentOrientation) {
		/*
		 * https://jira.jboss.org/jira/browse/JBIDE-4152
		 * 
		 * To re-layout editors new sash form will be created. Source, visual
		 * and preview content will stay the same, cause there are no changes to
		 * the model.
		 * 
		 * Content should be added to a new container.
		 */
		String splitting;
		if (useCurrentEditorSettings) {
			splitting = currentOrientation;
		} else {
			splitting = WebUiPlugin.getDefault().getPreferenceStore()
					.getString(IVpePreferencesPage.VISUAL_SOURCE_EDITORS_SPLITTING);
		}
		CustomSashForm newContainer = new CustomSashForm(cmpEd,	CustomSashForm.getSplittingDirection(splitting));

		/*
		 * Reset editor's settings.
		 */
		if (editorSettings != null) {
			editorSettings.clearOldSettings();
			editorSettings.addSetting(new SashSetting(newContainer));
		}
		newContainer
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		if (CustomSashForm.isSourceEditorFirst(splitting)) {
			sourceContent.setParent(newContainer);
			visualContent.setParent(newContainer);
		} else {
			visualContent.setParent(newContainer);
			sourceContent.setParent(newContainer);
		}
		// previewContent.setParent(newContainer);

		/*
		 * https://jira.jboss.org/jira/browse/JBIDE-4513 New container should
		 * have all properties from the old container set.
		 */
		if (null != container.getMaximizedControl()) {
			newContainer.setMaximizedControl(container.getMaximizedControl());
		}
		/*
		 * Dispose the old container: it'll be excluded from parent composite's
		 * layout.
		 */
		if (null != container) {
			container.dispose();
		}

		/*
		 * Reset the container.
		 */
		container = newContainer;

		/*
		 * Set up new sash weights
		 */
		int defaultWeight = WebUiPlugin.getDefault().getPreferenceStore()
				.getInt(IVpePreferencesPage.VISUAL_SOURCE_EDITORS_WEIGHTS);
		int[] weights = container.getWeights();
		if (useCurrentEditorSettings) {
			newContainer.setWeights(weights);
		} else {
			if (defaultWeight == 0) {
				if (CustomSashForm.isSourceEditorFirst(splitting)) {
					container.maxDown();
				} else {
					container.maxUp();
				}
			} else if (defaultWeight == 1000) {
				if (CustomSashForm.isSourceEditorFirst(splitting)) {
					container.maxUp();
				} else {
					container.maxDown();
				}
			} else {
				if (CustomSashForm.isSourceEditorFirst(splitting)) {
					weights[0] = 1000 - defaultWeight;
					weights[1] = defaultWeight;
				} else {
					weights[0] = defaultWeight;
					weights[1] = 1000 - defaultWeight;
				}
				if ((weights != null) && !container.isDisposed()) {
					container.setWeights(weights);
				}
			}
		}

		container.setSashBorders(new boolean[] { true, true, true });

		/*
		 * Reinit listeners on the new container.
		 */
		if (controlListener != null) {
			if (cmpEdTl != null && !cmpEdTl.isDisposed()) {
				cmpEdTl.getParent().removeControlListener(controlListener);
			}
		}
		controlListener = new ControlListener() {
			public void controlMoved(ControlEvent event) {
			}

			public void controlResized(ControlEvent event) {
				container.layout();
			}
		};

		if (cmpEdTl != null && !cmpEdTl.isDisposed()) {
			cmpEdTl.getParent().addControlListener(controlListener);
		}
		/*
		 * Layout the parent container for CustomSashForm, Selection Bar.
		 */
		cmpEdTl.layout(true, true);
	}
	
	@Override
	public void createVisualEditor() {
		visualEditor = new VpvEditor(sourceEditor);
		try {
			visualEditor.init(getEditorSite(), getEditorInput());
		} catch (PartInitException e) {
			VpePlugin.reportProblem(e);
		}

		visualEditor.setEditorLoadWindowListener(new EditorLoadWindowListener() {
			public void load() {
				visualEditor.setEditorLoadWindowListener(null);
				VPVController vpeController = new VPVController(VpvEditor2.this);
				vpeController.init(sourceEditor, visualEditor, bundleMap);
			}
		});
		
		toolBar = visualEditor.createVisualToolbar(verticalToolbarSplitter);
		visualEditor.createPartControl(visualContent);
		if (multiPageEditor instanceof JSPMultiPageEditor) {
			JSPMultiPageEditor jspMultiPageEditor = (JSPMultiPageEditor) multiPageEditor;
			/*
			 * https://issues.jboss.org/browse/JBIDE-11302
			 */
			selectionBar = jspMultiPageEditor.getSelectionBar();
			/*
			 * https://issues.jboss.org/browse/JBIDE-10711
			 */
			if (!XulRunnerBrowser.isCurrentPlatformOfficiallySupported()) {
				/*
				 * Set the flag in JSPMultiPageEditor
				 */
				jspMultiPageEditor.setXulRunnerBrowserIsNotSupported(true);
			}
		}
	}
	

	@Override
	public void createPreviewBrowser() {
		
	}
	
	@Override
	public void setFocus() {
		if (activeEditor != null) {
			activeEditor.setFocus();
		}
	}
	
	@Override
	public void dispose() {
//		deactivateServices();
//		sourceActivation = null;
//		sourceMaxmin = null;
//		visualActivation = null;
//		visualMaxmin = null;
//		jumpingActivation = null;
//		jumping = null;
		if (verticalToolbarEmpty != null) {
			if (!verticalToolbarEmpty.isDisposed()) {
				verticalToolbarEmpty.dispose();
			}
			verticalToolbarEmpty = null;
		}
//		if (optionsObject != null) {
//			optionsObject.getModel().removeModelTreeListener(listener);
//			listener=null;
//			optionsObject = null;
//		}
		if (editorSettings != null) {
			editorSettings.dispose();
			editorSettings = null;
		}
//		if (activationListener != null) {
//			IWorkbenchWindow window = getSite().getWorkbenchWindow();
//			window.getPartService().removePartListener(activationListener);
//			Shell shell = window.getShell();
//			if (shell != null && !shell.isDisposed())
//				shell.removeShellListener(activationListener);
//			activationListener = null;
//		}
		// editor will disposed as part of multipart editor
		if (sourceEditor != null) {
			sourceEditor.dispose();
			sourceEditor = null;
		}

		if (visualEditor != null) {
			visualEditor.dispose();
			visualEditor = null;
		}

//		if (previewWebBrowser != null) {
//			previewWebBrowser.dispose();
//			previewWebBrowser=null;
//		}
		if (previewContent != null) {
			previewContent.dispose();
			previewContent = null;
		}
		
		if (selectionBar != null) {
			selectionBar.dispose();
			selectionBar = null;
		}
		activeEditor = null;
		multiPageEditor = null;
		super.dispose();
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
			visualEditor.reload();
		}
	}
	
	
	
	public void updateSelectionBar(boolean isSelectionBarVisible) {
		if (selectionBar != null) {
			selectionBar.setVisible(isSelectionBarVisible);
		} else {
			VpePlugin.getDefault().logError("VPE Selection Bar is not initialized."); //$NON-NLS-1$
		}
	}

	@Override
	public IVisualController getController() {
		return visualEditor.getController();
	}

	@Override
	public Object getPreviewWebBrowser() {
		return visualEditor.getBrowser();
	}

	@Override
	public Object getVisualEditor() {
		return visualEditor;
	}
	
	/*
	 * Updates current VpeEditorPart after 
	 * OK/Apply button on "Visual Page Editor" preference page
	 * has been pressed.
	 */
	public void updatePartAccordingToPreferences() {
		/*
		 * Update MozillaEditor's toolbar items
		 */
		if (visualEditor != null) {
			visualEditor.updateToolbarItemsAccordingToPreferences();
		}
		/*
		 * When switching from Source view to Visual/Source controller could be null.
		 */
		if (getController() != null) {
			boolean prefsShowVPEToolBar = WebUiPlugin.getDefault().getPreferenceStore()
					.getBoolean(IVpePreferencesPage.SHOW_VISUAL_TOOLBAR);
			setVerticalToolbarVisible(prefsShowVPEToolBar);

			fillContainer(false, null);
		}
	}

	@Override
	public CustomSashForm getContainer() {
		return container;
	}
	
}
