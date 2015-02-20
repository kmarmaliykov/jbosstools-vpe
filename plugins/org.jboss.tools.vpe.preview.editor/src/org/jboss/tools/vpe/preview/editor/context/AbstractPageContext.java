package org.jboss.tools.vpe.preview.editor.context;

import org.jboss.tools.jst.web.ui.internal.editor.bundle.BundleMap;
import org.jboss.tools.jst.web.ui.internal.editor.editor.IVisualContext;
import org.jboss.tools.vpe.preview.editor.IVisualEditor2;

public abstract class AbstractPageContext implements IVisualContext {
	public static final String CUSTOM_ELEMENTS_ATTRS = "customElementsAttributes"; //$NON-NLS-1$
	public static final String CURRENT_VISUAL_NODE = "currentVisualNode"; //$NON-NLS-1$
	public static final String RES_REFERENCES = "resourceReferences"; //$NON-NLS-1$
	public static final String EL_EXPR_SERVICE = "elExprService"; //$NON-NLS-1$
	
	protected BundleMap bundle;
	
	@Override
	public void refreshBundleValues() {
		// TODO Auto-generated method stub

	}	

	public abstract IVisualEditor2 getEditPart();
	
	public BundleMap getBundle() {
		return bundle;
	}
}
