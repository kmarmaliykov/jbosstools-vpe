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
import org.jboss.tools.vpe.preview.editor.context.VpvPageContext;
import org.jboss.tools.vpe.resref.core.AbsoluteFolderReferenceList;
import org.w3c.dom.Node;

/**
 * @author Konstantin Marmalyukov (kmarmaliykov)
 */
@SuppressWarnings({ "deprecation" })
public class VpvEditorController extends VisualController implements INodeAdapter, IModelLifecycleListener,
		INodeSelectionListener, ITextSelectionListener, SelectionListener, ResourceReferenceListListener,
		ISelectionChangedListener, IVisualController {
	
	public static final String VPE_CATEGORY_ID = "org.jboss.tools.vpe.category"; //$NON-NLS-1$
	
	private StructuredTextEditor sourceEditor;
	private VpvEditorPart editPart;
	private VpvEditor visualEditor;
	private VpvPageContext pageContext;
//	private FormatControllerManager toolbarFormatControllerManager = null;
	private boolean visualEditorVisible = true;
	
	private static List<String> vpeCategoryCommands = null;

	private AbsoluteFolderReferenceList absoluteFolderReferenceListListener;
	
	public VpvEditorController(VpvEditorPart editPart) {
		this.editPart = editPart;
	}
	
	void init(StructuredTextEditor sourceEditor, VpvEditor visualEditor, BundleMap bundleMap) {
		this.sourceEditor = sourceEditor;
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

		ISelectionProvider provider = sourceEditor.getSelectionProvider();
		provider.addSelectionChangedListener(this);
		
		final StyledText textWidget = SelectionHelper.getSourceTextWidget(sourceEditor);
		if (textWidget != null) {
			textWidget.addSelectionListener(this);
		}

		absoluteFolderReferenceListListener = AbsoluteFolderReferenceList.getInstance();
		absoluteFolderReferenceListListener.addChangeListener(this);
		
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
//		if (editPart.getVisualMode() != VpvEditorPart.SOURCE_MODE) {
//			if (toolbarFormatControllerManager != null)
//				toolbarFormatControllerManager.selectionChanged();
//		}

		if (editPart.getVisualMode() != VpvEditorPart.SOURCE_MODE) {
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
		if (absoluteFolderReferenceListListener == source) {
			visualRefresh();
		}
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
					if(VPE_CATEGORY_ID.equals(command.getCategory().getId())){
						//collecting vpe category commands
						vpeCategoryCommands.add(command.getId());
					}
				} catch (NotDefinedException e) {
					Activator.getDefault().logError(e);
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
		if (editPart.getVisualMode() != VpvEditorPart.SOURCE_MODE) {
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

		if (absoluteFolderReferenceListListener != null) {
			absoluteFolderReferenceListListener.removeChangeListener(this);
		}
		
//		toolbarFormatControllerManager = null;
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
	
//	public void setToolbarFormatControllerManager(
//			FormatControllerManager formatControllerManager) {
//		toolbarFormatControllerManager = formatControllerManager;
//	}
}
