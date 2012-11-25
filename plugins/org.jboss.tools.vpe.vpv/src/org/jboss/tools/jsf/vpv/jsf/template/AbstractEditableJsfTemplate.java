/******************************************************************************* 
 * Copyright (c) 2007 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.jsf.vpv.jsf.template;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.tools.vpe.vpv.template.VpeTemplate;
import org.jboss.tools.vpe.vpv.template.util.HTML;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * general class for jsf templates.
 * 
 * @author Sergey Dzmitrovich
 */
public abstract class AbstractEditableJsfTemplate implements VpeTemplate {
	
    // general jsf attributes
	/**
	 * Contains JSF attributes and appropriate HTML attributes 
	 * content of that does not have to be modified in templates.
	 */
    static final private Map<String, String> attributes 
    		= new HashMap<String, String>();
	static {
		attributes.put(JSF.ATTR_STYLE, HTML.ATTR_STYLE);
		attributes.put(JSF.ATTR_STYLE_CLASS, HTML.ATTR_CLASS);
	}

	/**
	 * Renames and copies most general JSF attributes from the
	 * {@code sourceElement} to the {@code visualElement}.
	 * 
	 * @param sourceElement the source element
	 * @param visualElement the visual element
	 * @see AbstractEditableJsfTemplate#attributes attributes
	 */
	protected void copyGeneralJsfAttributes(Element sourceElement,
			Element visualElement) {
		
		Set<Map.Entry<String, String>> jsfAttrEntries = attributes.entrySet();
		
		for (Map.Entry<String, String> attrEntry : jsfAttrEntries) {
			copyAttribute(visualElement, sourceElement, attrEntry.getKey(),
					attrEntry.getValue());
		}

	}

	/**
	 * copy attribute.
	 * 
	 * @param sourceElement the source element
	 * @param targetAtttributeName the target atttribute name
	 * @param sourceAttributeName the source attribute name
	 * @param visualElement the visual element
	 */
	protected void copyAttribute(Element visualElement,
			Element sourceElement, String sourceAttributeName,
			String targetAtttributeName) {

		if (sourceElement.hasAttribute(sourceAttributeName))
			visualElement.setAttribute(targetAtttributeName, sourceElement
					.getAttribute(sourceAttributeName));

	}

	/**
	 * Gets the output attribute node.
	 * This method may be overridden in subclasses.
	 * 
	 * @param element the element
	 * 
	 * @return the output attribute node
	 */
	public Attr getOutputAttributeNode(Element element) {
		if (element.hasAttribute(JSF.ATTR_VALUE)) {
			return element.getAttributeNode(JSF.ATTR_VALUE);
		} else if (element.hasAttribute(JSF.ATTR_BINDING)) {
			return element.getAttributeNode(JSF.ATTR_BINDING);
		}
		return null;
	}
}
