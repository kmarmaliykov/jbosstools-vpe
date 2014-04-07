package org.jboss.tools.vpe.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.wst.sse.core.internal.model.ModelLifecycleEvent;
import org.eclipse.wst.sse.core.internal.provisional.IModelLifecycleListener;
import org.eclipse.wst.sse.core.internal.provisional.INodeAdapter;
import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.sse.ui.internal.view.events.INodeSelectionListener;
import org.eclipse.wst.sse.ui.internal.view.events.ITextSelectionListener;
import org.eclipse.wst.sse.ui.internal.view.events.NodeSelectionChangedEvent;
import org.eclipse.wst.sse.ui.internal.view.events.TextSelectionChangedEvent;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.jboss.tools.common.model.XModel;
import org.jboss.tools.common.model.project.IModelNature;
import org.jboss.tools.common.model.util.EclipseResourceUtil;
import org.jboss.tools.common.resref.core.ResourceReferenceListListener;
import org.jboss.tools.jst.web.model.helpers.WebAppHelper;
import org.jboss.tools.jst.web.project.WebProject;
import org.jboss.tools.jst.web.ui.internal.editor.bundle.BundleMap;
import org.jboss.tools.jst.web.ui.internal.editor.editor.IJSPTextEditor;
import org.jboss.tools.jst.web.ui.internal.editor.editor.IVisualController;
import org.jboss.tools.jst.web.ui.internal.editor.selection.SelectionHelper;
import org.jboss.tools.vpe.VpeDebug;
import org.jboss.tools.vpe.VpePlugin;
import org.jboss.tools.vpe.editor.context.VpvPageContext;
import org.jboss.tools.vpe.editor.toolbar.format.FormatControllerManager;
import org.jboss.tools.vpe.handlers.VisualPartAbstractHandler;
import org.w3c.dom.Node;

public class VPVController extends VisualController implements INodeAdapter, IModelLifecycleListener,
		INodeSelectionListener, ITextSelectionListener, SelectionListener, ResourceReferenceListListener,
		ISelectionChangedListener, IVisualController {
	
	
	private StructuredTextEditor sourceEditor;
	private VpvEditor2 editPart;
	private VpvEditor visualEditor;
	private VpvPageContext pageContext;
	private FormatControllerManager toolbarFormatControllerManager = null;
	private boolean visualEditorVisible = true;
	
	private static List<String> vpeCategoryCommands = null;
	
	public VPVController(VpvEditor2 editPart) {
		this.editPart = editPart;
	}
	
	void init(StructuredTextEditor sourceEditor, VpvEditor visualEditor, BundleMap bundleMap) {
		this.sourceEditor = sourceEditor;
		//this.bundleMap = bundleMap;
		this.visualEditor = visualEditor;
		visualEditor.setController(this);
		bundleMap.init(sourceEditor.getEditorInput());
		pageContext = new VpvPageContext(bundleMap, editPart);
		IDOMModel sourceModel = (IDOMModel) getModel();
		if (sourceModel == null) {
			return;
		}
		sourceModel.addModelLifecycleListener(this);

		IEditorInput editorInput = pageContext.getEditPart().getEditorInput();
		// commented by Maksim Areshkau, as fix for
		// https://jira.jboss.org/jira/browse/JBIDE-4534
		if (editorInput instanceof IFileEditorInput) {
			XModel xm = null;
			IProject project = ((IFileEditorInput) editorInput).getFile()
					.getProject();
			IModelNature mn = EclipseResourceUtil.getModelNature(project);
			if (mn != null) {
				xm = mn.getModel();
			}
			if (xm != null) {
				WebProject.getInstance(xm).getTaglibMapping().revalidate(
						WebAppHelper.getWebApp(xm));
			}
		}

		// Fix for JBIDE-5105, JBIDE-5161
		//visualEditor.getEditor();

		// glory
		ISelectionProvider provider = sourceEditor.getSelectionProvider();
		// Max Areshkau JBIDE-1105 If selection event received after selection
		// in
		// visual part we lost focus of selection, so we should process
		// selection event
		// in time of selection
		// if (provider instanceof IPostSelectionProvider)
		// ((IPostSelectionProvider)
		// provider).addPostSelectionChangedListener(this);
		// else
		provider.addSelectionChangedListener(this);

		// ViewerSelectionManager selectionManager =
		// sourceEditor.getViewerSelectionManager();
		// selectionManager.addNodeSelectionListener(this);
		// selectionManager.addTextSelectionListener(this);
		final StyledText textWidget = SelectionHelper.getSourceTextWidget(sourceEditor);
		if (textWidget != null) {
			textWidget.addSelectionListener(this);
		}
		/*
		 * https://issues.jboss.org/browse/JBIDE-8701
		 * Add Source ScrollBar Listener
		 */
//		scrollCoordinator = new ScrollCoordinator(sourceEditor, visualEditor, domMapping); 
//		if ((visualEditor != null) && (sourceEditor != null)) {
//			sourceEditorVerticalScrollBar = textWidget.getVerticalBar();
//			if (sourceEditorVerticalScrollBar != null) {
//				if (visualEditor.getXulRunnerEditor() != null) {
//					nsIWebBrowser webBrowser = visualEditor.getXulRunnerEditor().getWebBrowser();
//					if (webBrowser != null) {
//						/*
//						 * Initialize mozilla browser content window
//						 */
//						final nsIDOMWindow domWindow = webBrowser.getContentDOMWindow();
//						final nsIDOMWindowInternal windowInternal = org.jboss.tools.vpe.xulrunner.util.XPCOM
//								.queryInterface(domWindow, nsIDOMWindowInternal.class);
//						/*
//						 * Adding source listener
//						 */
//						sourceScrollSelectionListener = new SelectionListener() {
//							@Override
//							public void widgetSelected(SelectionEvent e) {
//								/*
//								 * Check that scrolling is reasonable:
//								 * when scrollMaxYVisual > 0
//								 */
//								int scrollMaxYVisual = windowInternal.getScrollMaxY();
//								if (WebUiPlugin.getDefault().getPreferenceStore().getBoolean(
//										IVpePreferencesPage.SYNCHRONIZE_SCROLLING_BETWEEN_SOURCE_VISUAL_PANES)
//										&& !visualScrollEventFlag && !selectionManager.isUpdateSelectionEventPerformed()
//										&& editPart.getVisualMode() == VpeEditorPart.VISUALSOURCE_MODE
//										&& scrollMaxYVisual > 0) { // ignore internal visual scroll event
//									sourceScrollEventFlag = true;
//									int posY = scrollCoordinator.computeVisualPositionFromSource();
//									/*
//									 * Scroll only when there is a new position
//									 */
//									if (posY != -1) {
//										if (posY > scrollMaxYVisual) {
//											posY = scrollMaxYVisual;
//										}
//										domWindow.scrollTo(windowInternal.getPageXOffset(),posY);
//									}
//								} else {
//									visualScrollEventFlag = false;
//									selectionManager.setUpdateSelectionEventFlag(false);
//								}
//							}
//							@Override
//							public void widgetDefaultSelected(SelectionEvent e) { }
//						};
//						sourceEditorVerticalScrollBar.addSelectionListener(sourceScrollSelectionListener);
//					}
//				}
//			}
//		} // End of fix JBIDE-8701


		// pageContext.fireTaglibsChanged();

		// yradtsevich: we have to refresh VPE selection on init 
		// (fix of JBIDE-4037)
		sourceSelectionChanged();
		refreshCommands();
	}
	
	@Override
	public IStructuredModel getModel() {
		return sourceEditor.getModel();
	}

	@Override
	public void drop(Node node, Node parentNode, int offset) {
		//no DnD in VPV
	}

	@Override
	public VpvPageContext getPageContext() {
		return pageContext;
	}

	@Override
	public void postLongOperation() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preLongOperation() {
		//do nothing
	}

	@Override
	public void refreshExternalLinks() {
		//external links in VPV is not supported for now
		
	}

	@Override
	public void visualRefresh() {
		visualEditor.reload();
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		if (!isVisualEditorVisible()) {
			// selection event doesn't changes a content
			// synced = false;
			return;
		}
		/*
		 * Enable/disable "Externalize strings" toolbar icon 
		 * after selection has been changed
		 */
//		visualEditor.updateExternalizeStringsToolbarIconState(
//				sourceEditor.getSelectionProvider().getSelection());
		/*
		 * Update Text Formatting Toolbar state
		 */
		if (editPart.getVisualMode() != VpeEditorPart.SOURCE_MODE) {
			if (toolbarFormatControllerManager != null)
				toolbarFormatControllerManager.selectionChanged();
		}

		if (editPart.getVisualMode() != VpeEditorPart.SOURCE_MODE) {
			if (VpeDebug.PRINT_SOURCE_SELECTION_EVENT) {
				System.out.println(">>>>>>>>>>>>>> selectionChanged  " + event.getSource()); //$NON-NLS-1$
			}
			sourceSelectionChanged();
		}
	}

	@Override
	public IPath getPath() {
		if (editPart != null) {
			IEditorInput input = editPart.getEditorInput();
			if (input instanceof IFileEditorInput) {
				return ((IFileEditorInput) input).getFile().getFullPath();
			}
		}
		return null;
	}

	@Override
	public void changed(Object source) {
		// is used for external references which is not implemented now
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {
		//do nothing
	}

	@Override
	public void widgetSelected(SelectionEvent arg0) {
		//is called when something is selected in visual part.
		//TODO: integrate this functionality like it is done for view
	}

	@Override
	public void textSelectionChanged(TextSelectionChangedEvent arg0) {
		//is called when something is selected in visual part.
		//TODO: integrate this functionality like it is done for view
	}

	@Override
	public void nodeSelectionChanged(NodeSelectionChangedEvent arg0) {
		//do nothing
	}

	@Override
	public void processPostModelEvent(ModelLifecycleEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processPreModelEvent(ModelLifecycleEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	// INodeAdapter implementation
	@Override
	public boolean isAdapterForType(Object type) {
		return this == type;
	}

	@Override
	public void notifyChanged(INodeNotifier arg0, int arg1, Object arg2,
			Object arg3, Object arg4, int arg5) {
		// TODO Auto-generated method stub
		
	}
	
	public void refreshCommands(){
		ICommandService commandService = (ICommandService) 
				editPart.getSite().getService(ICommandService.class);
		/*
		 * https://issues.jboss.org/browse/JBIDE-12290
		 * In eclipse 4.2 commandService could be null.
		 */
		if (commandService != null) {
			for (String commandId : getVpeCategoryCommands()) {
				commandService.refreshElements(commandId, null);
			} 
		}
	}
	
	private List<String> getVpeCategoryCommands() {
		/*
		 * initialize  VPE Commands List 
		 * if its has not been initialized yet
		 */
		if (vpeCategoryCommands == null) {
			ICommandService commandService = (ICommandService) 
					editPart.getSite().getService(ICommandService.class);
			vpeCategoryCommands = new ArrayList<String>();
			Command [] definedCommands = commandService.getDefinedCommands();
			for (Command command : definedCommands) {
				try {
					if(VisualPartAbstractHandler.VPE_CATEGORY_ID.equals(command.getCategory().getId())){
						//collecting vpe category commands
						vpeCategoryCommands.add(command.getId());
					}
				} catch (NotDefinedException e) {
					VpePlugin.reportProblem(e);
				}
			}
		}
		return vpeCategoryCommands;
	}	
	
	public void sourceSelectionChanged() {
//		// we should processed if we have correct view in visual editor,
//		// otherwise we shouldn't process this event
//		if (getChangeEvents().size() > 0) {
//			return;
//		}
//		if (selectionManager != null && !visualSelectionIsAlreadyInProgress) {
//			selectionManager.refreshVisualSelection();
//		}
	}

	public void sourceSelectionToVisualSelection() {
		if (editPart.getVisualMode() != VpeEditorPart.SOURCE_MODE) {
			sourceSelectionChanged();
		}
	}
	
	public void dispose() {
//		if (job != null) {
//			job.cancel();
//			job = null;
//		}
//
//		if (uiJob != null) {
//			uiJob.cancel();
//			getChangeEvents().clear();
//			uiJob = null;
//		}
//
//		if (visualRefreshJob != null) {
//			visualRefreshJob.cancel();
//			visualRefreshJob = null;
//		}
//
//		if (optionsListener != null) {
//			XModelObject optionsObject = ModelUtilities.getPreferenceModel()
//					.getByPath(VpePreference.EDITOR_PATH);
//			optionsObject.getModel().removeModelTreeListener(optionsListener);
//			optionsListener.dispose();
//			optionsListener = null;
//		}
		IDOMModel sourceModel = (IDOMModel) getModel();
		if (sourceModel != null) {
			sourceModel.removeModelLifecycleListener(this);
		}

//		VpeTemplateManager.getInstance().removeTemplateListener(this);
//
//		if (visualBuilder != null) {
//			visualBuilder.dispose();
//			visualBuilder = null;
//		}
//		sourceBuilder = null;
		if (sourceEditor != null) {
			// glory
			ISelectionProvider provider = sourceEditor.getSelectionProvider();
			provider.removeSelectionChangedListener(this);
			StyledText textWidget = SelectionHelper
					.getSourceTextWidget(sourceEditor);
			if (textWidget != null) {
				textWidget.removeSelectionListener(this);
			}
			((IJSPTextEditor) sourceEditor).setVPEController(null);

		}
//		if (dropWindow != null) {
//			dropWindow.setEditor(null);
//		}
		if (visualEditor != null) {
//			unregisterEventTargets();
//			if (visualSelectionController != null) {
//				// visualSelectionController.Release();
//				visualSelectionController = null;
//			}
			visualEditor = null;
		}

//		if (cssReferenceListListener != null) {
//			cssReferenceListListener.removeChangeListener(this);
//		}
//		if (taglibReferenceListListener != null) {
//			taglibReferenceListListener.removeChangeListener(this);
//		}
//		if (absoluteFolderReferenceListListener != null) {
//			absoluteFolderReferenceListListener.removeChangeListener(this);
//		}
//		if (elReferenceListListener != null) {
//			elReferenceListListener.removeChangeListener(this);
//		}
//		if (relativeFolderReferenceListListener != null) {
//			relativeFolderReferenceListListener.removeChangeListener(this);
//		}
		toolbarFormatControllerManager = null;
	}
	
	public boolean isVisualEditorVisible() {
		return visualEditorVisible;
	}
	
	public void setVisualEditorVisible(boolean visualEditorVisible) {
		this.visualEditorVisible = visualEditorVisible;
	}
	
	public StructuredTextEditor getSourceEditor() {
		return sourceEditor;
	}
	
	public void setToolbarFormatControllerManager(
			FormatControllerManager formatControllerManager) {
		toolbarFormatControllerManager = formatControllerManager;
	}
}
