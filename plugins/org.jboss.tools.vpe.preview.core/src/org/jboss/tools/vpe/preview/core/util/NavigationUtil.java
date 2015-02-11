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
package org.jboss.tools.vpe.preview.core.util;

import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.browser.Browser;
import org.eclipse.ui.IEditorPart;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.jboss.tools.vpe.preview.core.Activator;
import org.jboss.tools.vpe.preview.core.transform.DomUtil;
import org.jboss.tools.vpe.preview.core.transform.TransformUtil;
import org.jboss.tools.vpe.preview.core.transform.VpvDomBuilder;
import org.jboss.tools.vpe.preview.core.transform.VpvVisualModel;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Ilya Buziuk (ibuziuk)
 */
public final class NavigationUtil {
	private static final String VPV_SELECTION_STYLE_ID = "VPV_STYLESHEET_ID";  //$NON-NLS-1$
	
	private NavigationUtil() {
	}
		
	public static void disableLinks(Browser browser) {
		if (browser != null && !browser.isDisposed()) {
			// IE can't handle 'javascript: void(0);' - JBIDE-18091 StackOverflow error
			String disableHrefScript = (PlatformUtil.isWindows()) ? "'#'" : "'javascript: void(0);'";  //$NON-NLS-1$ //$NON-NLS-2$ 	
			String disablerScript = "var anchors = document.getElementsByTagName('a');" + //$NON-NLS-1$
			                        "for (var i = 0; i < anchors.length; i++) {" + //$NON-NLS-1$
			                            // need to disable all links except local links with anchor(which is used for one-page-app)
			                            "if (anchors[i].href.indexOf('#') == -1 || anchors[i].href.indexOf('://localhost:') == -1) {" + //$NON-NLS-1$
			                                "anchors[i].href = " + disableHrefScript + ";" + //$NON-NLS-1$ //$NON-NLS-2$
			                                "anchors[i].target = '';" + //$NON-NLS-1$
			                            "}" +  //$NON-NLS-1$
			                        "};"; //$NON-NLS-1$			
			browser.execute("(setTimeout(function() { " +  //$NON-NLS-1$
								disablerScript +	
					  		"}, 10))();"); //$NON-NLS-1$
			disableDynamic(browser, disablerScript);
		}		
	}
	
	public static void disableInputs(Browser browser) {
		if (browser != null && !browser.isDisposed()) {
			String disablerScript = "var inputs = document.getElementsByTagName('INPUT');" + //$NON-NLS-1$
	        	      				"for (var i = 0; i < inputs.length; i++) {" + //$NON-NLS-1$
	        	      					"inputs[i].blur();" + //$NON-NLS-1$ Disabling autofocus
	        	      					"inputs[i].disabled = true;" + //$NON-NLS-1$
	        	      				"}"; //$NON-NLS-1$
			
			String disableInputs = "function() {" + //$NON-NLS-1$
					                  disablerScript +
		                           "}"; //$NON-NLS-1$
			if (PlatformUtil.isWindows()) {
				browser.execute("(" + disableInputs + ")();"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				int timeout = 30;
				if (PlatformUtil.isLinux()) {
					timeout = 150; // timeout increased for old xulrunner 
				}
				browser.execute("(setTimeout(" + disableInputs + ", "+ timeout +"))();"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			}
			
			disableDynamic(browser, disablerScript);
		}
	}

	public static void outlineSelectedElement(Browser browser, Long currentSelectionId) {
		if (browser != null && !browser.isDisposed()) {
			String styleAttributeSelector;
			if (currentSelectionId == null) {
				styleAttributeSelector = ""; //$NON-NLS-1$
			} else {
				styleAttributeSelector = "'[" + VpvDomBuilder.ATTR_VPV_ID + "=\"" + currentSelectionId + "\"] {outline: 1px solid blue; border: 1px solid blue;  z-index: 2147483638;}'"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			
			String outlineJsFunction = "function() {" + //$NON-NLS-1$
					                       "var style=document.getElementById('" + VPV_SELECTION_STYLE_ID + "');" + //$NON-NLS-1$ //$NON-NLS-2$
					                       "if (!style) {" + //$NON-NLS-1$
					                            "style = document.createElement('STYLE');" + //$NON-NLS-1$
					                            "style.type = 'text/css';" + //$NON-NLS-1$
					                       "}" + //$NON-NLS-1$
					                       "style.id = '" + VPV_SELECTION_STYLE_ID + "';" + //$NON-NLS-1$ //$NON-NLS-2$
					                       "style.innerHTML = " + styleAttributeSelector + ";" + //$NON-NLS-1$ //$NON-NLS-2$
					                       "var head = document.head || document.getElementsByTagName('head')[0] ;"+ //$NON-NLS-1$
					                       "head.appendChild(style);" + //$NON-NLS-1$
					                   "}"; //$NON-NLS-1$
			
			if (PlatformUtil.isWindows()) {
				browser.execute("(" + outlineJsFunction + ")();"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				// JBIDE-17155 Visual Preview: no selection after element changed on Mac Os and Linux
				browser.execute("(setTimeout(" + outlineJsFunction + ", 10))();"); //$NON-NLS-1$//$NON-NLS-2$
			}
		}
	}

	public static void disableJsPopUps(Browser browser) {
		browser.execute ("window.alert = function() {};" + //$NON-NLS-1$
				"window.confirm = function() {};" + //$NON-NLS-1$
				"window.prompt = function() {};" + //$NON-NLS-1$
				"window.open = function() {};"); //$NON-NLS-1$
	}
	
	public static String removeAnchor(String url) {
		int index = url.lastIndexOf('#');
		if (index != -1) {
		    return url.substring(0, index);
		} 
		return url;
	}
	
	/** Modify current URL to make Visual Editor/HTML Preview work correct
	 * 
	 * @see JBIDE-18043
	 * @see JBIDE-18302
	 * 
	 * @param browser
	 * @return updated URL
	 */
	public static String fixUrl(Browser browser) {
		String url = browser.getUrl();
		if (PlatformUtil.isWindows()) {
			return NavigationUtil.removeAnchor(url);
		} else if (PlatformUtil.isMacOS()) {
			return (String) browser.evaluate("return window.location.href"); //$NON-NLS-1$
		}
		return url;
	}
	
	public static void navigateToVisual(IEditorPart currentEditor, Browser browser, VpvVisualModel visualModel, int x, int y) {
		String stringToEvaluate = ""; //$NON-NLS-1$
		if (PlatformUtil.isLinux()) {
			/* outerHTML is not available with XulRunner we shipping, so <code>result</code> variable will be null
			 * because we make it default browser on Linux this workaround is used
			 * @see JBIDE-17454
			 */
			stringToEvaluate = "var selected = document.elementFromPoint(" + x + ", " + y + ");"+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"var temp = document.createElement('div');"+ //$NON-NLS-1$
			"temp.appendChild(selected.cloneNode(true));"+ //$NON-NLS-1$
			"return temp.innerHTML;"; //$NON-NLS-1$
		} else {
			stringToEvaluate = "return document.elementFromPoint(" + x + ", " + y + ").outerHTML;"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		String result = (String) browser.evaluate(stringToEvaluate);
		if (result != null) {
			String selectedElementId = TransformUtil.getSelectedElementId(result, "(?<=data-vpvid=\").*?(?=\")"); //$NON-NLS-1$
	
			Long id = (selectedElementId != null && !selectedElementId.isEmpty()) //avoiding NumberFormatException
					  ? Long.parseLong(selectedElementId)
					  : null;
			NavigationUtil.outlineSelectedElement(browser, id);
	
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
	}
	
	public static void updateSelectionAndScrollToIt(ISelection currentSelection, Browser browser, VpvVisualModel visualModel) {
		if (currentSelection instanceof IStructuredSelection) {
			Node sourceNode = EditorUtil.getNodeFromSelection((IStructuredSelection) currentSelection);
			Long currentSelectionId = getIdForSelection(sourceNode, visualModel);
			NavigationUtil.scrollToId(browser, currentSelectionId);
			NavigationUtil.outlineSelectedElement(browser, currentSelectionId);
		}
	}
	
	private static void scrollToId(Browser browser, Long currentSelectionId) {
		if (browser != null && !browser.isDisposed()) {
			if (currentSelectionId != null) {
				browser.execute("(setTimeout(function() {" + //$NON-NLS-1$								
									"var selectedElement = document.querySelector('[" + VpvDomBuilder.ATTR_VPV_ID + "=\"" + currentSelectionId + "\"]');" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
									"selectedElement.scrollIntoView(true);" + //$NON-NLS-1$
								"}, 300))();");   //$NON-NLS-1$;
			}
		}
	}
	
	public static Long getIdForSelection(Node selectedSourceNode, VpvVisualModel visualModel) {
		Long id = null;
		if (selectedSourceNode != null && visualModel != null) {
			Map<Node, Node> sourceVisuaMapping = visualModel.getSourceVisualMapping();

			Node visualNode = null;
			Node sourceNode = selectedSourceNode;
			do {
				visualNode = sourceVisuaMapping.get(sourceNode);
				sourceNode = DomUtil.getParentNode(sourceNode);
			} while (visualNode == null && sourceNode != null);

			if (!(visualNode instanceof Element)) { // text node, comment, etc
				visualNode = DomUtil.getParentNode(visualNode); // should be element now or null
			}

			String idString = null;
			if (visualNode instanceof Element) {
				Element elementNode = (Element) visualNode;
				idString = elementNode.getAttribute(VpvDomBuilder.ATTR_VPV_ID);
			}

			if (idString != null && !idString.isEmpty()) {
				try {
					id = Long.parseLong(idString);
				} catch (NumberFormatException e) {
					Activator.logError(e);
				}
			}
		}
		return id;
	}

	private static void disableDynamic(Browser browser, String disablerScript) {
		String script = 
				"var observeDOM = (function(){" + //$NON-NLS-1$
			        "var MutationObserver = window.MutationObserver || window.WebKitMutationObserver," + //$NON-NLS-1$
			            "eventListenerSupported = window.addEventListener;" +  //$NON-NLS-1$

			        "return function(obj, callback){" + //$NON-NLS-1$
			            "if( MutationObserver ){" + //$NON-NLS-1$
			                // define a new observer
			                "var obs = new MutationObserver(function(mutations, observer){" + //$NON-NLS-1$
			                    "if( mutations[0].addedNodes.length || mutations[0].removedNodes.length )" + //$NON-NLS-1$
			                        "callback();" + //$NON-NLS-1$
			                "});" + //$NON-NLS-1$
			                // have the observer observe for changes in children
			                "obs.observe( obj, { childList:true, subtree:true });" + //$NON-NLS-1$
			            "}" + //$NON-NLS-1$
			            "else if( eventListenerSupported ){" + //$NON-NLS-1$
			                "obj.addEventListener('DOMNodeInserted', callback, false);" + //$NON-NLS-1$
			                "obj.addEventListener('DOMNodeRemoved', callback, false);" + //$NON-NLS-1$
			            "}" + //$NON-NLS-1$
			        "}" + //$NON-NLS-1$
			    "})();" + //$NON-NLS-1$

			    // Observe a specific DOM element:
			    "observeDOM( document.body ,function(){" +  //$NON-NLS-1$
			    	disablerScript + 
			    "});"; //$NON-NLS-1$
		browser.execute(script);
	}
}
