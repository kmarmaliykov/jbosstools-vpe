package org.jboss.tools.vpe.editor;

import org.jboss.tools.jst.web.ui.internal.editor.editor.IVisualEditor;
import org.jboss.tools.vpe.editor.xpl.CustomSashForm;

public interface IVisualEditor2 extends IVisualEditor{
	public CustomSashForm getContainer();
	public void fillContainer(boolean useCurrentEditorSettings, String currentOrientation);
}
