package org.jboss.tools.vpe.preview.core.util;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.ui.IEditorPart;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.jboss.tools.vpe.preview.core.Activator;
import org.jboss.tools.vpe.preview.core.transform.TransformUtil;
import org.jboss.tools.vpe.preview.core.transform.VpvVisualModel;
import org.w3c.dom.Node;

@SuppressWarnings("restriction")
public class BrowserUtil {
	public static void addNavigationFromVisual(final IEditorPart currentEditor, final Browser browser, final VpvVisualModel visualModel) {
		browser.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent event) {
				String stringToEvaluate = "return document.elementFromPoint(" + event.x + ", " + event.y + ").outerHTML;"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				String result = (String) browser.evaluate(stringToEvaluate);
				String selectedElementId = TransformUtil.getSelectedElementId(result, "(?<=data-vpvid=\").*?(?=\")"); //$NON-NLS-1$

				NavigationUtil.outlineSelectedElement(browser, Long.parseLong(selectedElementId));

				String fileExtension = EditorUtil.getFileExtensionFromEditor(currentEditor);

				if (SuitableFileExtensions.isHTML(fileExtension)) {
					try {
						Node visualNode = TransformUtil.getVisualNodeByVpvId(visualModel, selectedElementId);
						Node sourseNode = TransformUtil.getSourseNodeByVisualNode(visualModel, visualNode);

						if (sourseNode != null && sourseNode instanceof IDOMNode) {
							int startOffset = ((IDOMNode) sourseNode).getStartOffset();
							int endOffset = ((IDOMNode) sourseNode).getEndOffset();

							StructuredTextEditor editor = (StructuredTextEditor) currentEditor.getAdapter(StructuredTextEditor.class);
							editor.selectAndReveal(startOffset, endOffset - startOffset);

						}

					} catch (XPathExpressionException e) {
						Activator.logError(e);
					}
				}
			}

		});
	}
	
}
