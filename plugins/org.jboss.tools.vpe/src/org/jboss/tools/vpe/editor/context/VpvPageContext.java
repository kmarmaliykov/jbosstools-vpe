package org.jboss.tools.vpe.editor.context;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.jboss.tools.common.resref.core.ResourceReference;
import org.jboss.tools.jst.web.tld.TaglibData;
import org.jboss.tools.jst.web.ui.internal.editor.bundle.BundleMap;
import org.jboss.tools.jst.web.ui.internal.editor.editor.IVisualContext;
import org.jboss.tools.jst.web.ui.internal.editor.preferences.VpePreference;
import org.jboss.tools.vpe.editor.VpvEditor2;
import org.jboss.tools.vpe.resref.core.AbsoluteFolderReferenceList;
import org.jboss.tools.vpe.resref.core.RelativeFolderReferenceList;
import org.jboss.tools.vpe.resref.core.TaglibReferenceList;

public class VpvPageContext extends AbstractPageContext implements IVisualContext {
	private VpvEditor2 editPart;
	
	public VpvPageContext(BundleMap bundle, VpvEditor2 editPart) {
		this.bundle = bundle;
		this.editPart = editPart;
	}

	public static boolean isAbsolutePosition() {
		if ("yes".equals(VpePreference.USE_ABSOLUTE_POSITION.getValue())) { //$NON-NLS-1$
			return true;
		}
		return false;
	}

	public void dispose() {
		bundle.dispose();
		bundle.clearAll();
		editPart = null;
	}

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

	public ResourceReference getRuntimeRelativeFolder(IFile file) {
		ResourceReference[] list = RelativeFolderReferenceList.getInstance().getAllResources(file);
		if (list.length > 0) {
			return list[list.length - 1];
		}
		return null;
	}

	public ResourceReference getRuntimeAbsoluteFolder(IFile file) {
		ResourceReference[] list = AbsoluteFolderReferenceList.getInstance().getAllResources(file);
		if (list.length > 0) {
			return list[list.length - 1];
		}
		return null;
	}

	/**
	 * Processes display events to prevent eclipse froze
	 */
	public static void processDisplayEvents() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		if (display != null) {
			while (display.readAndDispatch()) {
			}
		}
	}

	@Override
	public void refreshBundleValues() {
		//Do nothing here
	}

	@Override
	public VpvEditor2 getEditPart() {
		return editPart;
	}

}
