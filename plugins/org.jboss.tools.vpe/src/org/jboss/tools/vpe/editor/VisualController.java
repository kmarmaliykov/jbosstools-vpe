package org.jboss.tools.vpe.editor;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.wst.sse.core.internal.model.ModelLifecycleEvent;
import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.sse.ui.internal.view.events.NodeSelectionChangedEvent;
import org.eclipse.wst.sse.ui.internal.view.events.TextSelectionChangedEvent;
import org.jboss.tools.jst.web.ui.internal.editor.editor.IVisualContext;
import org.jboss.tools.jst.web.ui.internal.editor.editor.IVisualController;
import org.jboss.tools.vpe.editor.context.AbstractPageContext;
import org.jboss.tools.vpe.editor.context.VpvPageContext;
import org.jboss.tools.vpe.editor.toolbar.format.FormatControllerManager;
import org.w3c.dom.Node;

public abstract class VisualController implements IVisualController{

	public abstract IStructuredModel getModel();
	
	public abstract void drop(Node node, Node parentNode, int offset);

	public abstract AbstractPageContext getPageContext();

	public abstract void postLongOperation();
	
	public abstract void preLongOperation();

	public abstract void refreshExternalLinks();
	
	public abstract void visualRefresh();
	
	public abstract void selectionChanged(SelectionChangedEvent event);
	
	public abstract IPath getPath();
	
	public abstract void changed(Object source);
	
	public abstract void widgetDefaultSelected(SelectionEvent arg0);

	public abstract void widgetSelected(SelectionEvent arg0);

	public abstract void textSelectionChanged(TextSelectionChangedEvent arg0);

	public abstract void nodeSelectionChanged(NodeSelectionChangedEvent arg0);

	public abstract void processPostModelEvent(ModelLifecycleEvent arg0);

	public abstract void processPreModelEvent(ModelLifecycleEvent arg0);
	
	public abstract boolean isAdapterForType(Object type);

	public abstract void notifyChanged(INodeNotifier arg0, int arg1, Object arg2,
			Object arg3, Object arg4, int arg5);
	
	public abstract void refreshCommands();
	
	public abstract void sourceSelectionChanged();

	public abstract void sourceSelectionToVisualSelection();
	
	public abstract void dispose();
	
	public abstract void setVisualEditorVisible(boolean visualEditorVisible);
	
	public abstract StructuredTextEditor getSourceEditor();
	
	public abstract void setToolbarFormatControllerManager(
			FormatControllerManager formatControllerManager);

}
