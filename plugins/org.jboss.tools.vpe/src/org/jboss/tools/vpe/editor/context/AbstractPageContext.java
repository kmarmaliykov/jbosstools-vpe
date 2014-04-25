package org.jboss.tools.vpe.editor.context;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.jboss.tools.common.resref.core.ResourceReference;
import org.jboss.tools.jst.web.tld.TaglibData;
import org.jboss.tools.jst.web.ui.internal.editor.bundle.BundleMap;
import org.jboss.tools.jst.web.ui.internal.editor.editor.IVisualContext;
import org.jboss.tools.vpe.editor.IVisualEditor2;
import org.jboss.tools.vpe.resref.core.TaglibReferenceList;

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

	@Override
	public List<TaglibData> getIncludeTaglibs() {
		if (getEditPart() == null) {
			return new ArrayList<TaglibData>();
		}
		IEditorInput input = getEditPart().getEditorInput();
		IFile file = null;
		if (input instanceof IFileEditorInput) {
			file = ((IFileEditorInput) input).getFile();
		}
		ResourceReference[] resourceReferences = new ResourceReference[0];
		if (file != null) {
			resourceReferences = TaglibReferenceList.getInstance().getAllResources(file);
		}
		// added by Max Areshkau Fix for JBIDE-2065
		List<TaglibData> taglibData = new ArrayList<TaglibData>();
		for (ResourceReference resourceReference : resourceReferences) {
			taglibData.add(new TaglibData(0, resourceReference.getLocation(), resourceReference.getProperties()));
		}
		return taglibData;
	}

	public abstract IVisualEditor2 getEditPart();
	
	public BundleMap getBundle() {
		return bundle;
	}
}
