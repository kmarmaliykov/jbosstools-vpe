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

import org.jboss.tools.vpe.vpv.mapping.AttributeData;
import org.jboss.tools.vpe.vpv.mapping.VpeElementData;
import org.jboss.tools.vpe.vpv.template.VpeCreationData;
import org.jboss.tools.vpe.vpv.template.util.HTML;
import org.jboss.tools.vpe.vpv.template.util.VisualDomUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class JsfInputTextAreaTemplate extends AbstractEditableJsfTemplate {

	public VpeCreationData create(Node sourceNode, Document visualDocument) {

		Element sourceElement = (Element) sourceNode;

		Element textArea = visualDocument
				.createElement(HTML.TAG_TEXTAREA);

		copyGeneralJsfAttributes(sourceElement, textArea);
		ComponentUtil.copyDisabled(sourceElement, textArea);

		copyAttribute(textArea, sourceElement, JSF.ATTR_DIR, HTML.ATTR_DIR);
		copyAttribute(textArea, sourceElement, JSF.ATTR_ROWS, HTML.ATTR_ROWS);
		copyAttribute(textArea, sourceElement, JSF.ATTR_COLS, HTML.ATTR_COLS);

		VpeElementData elementData = new VpeElementData();
		Node text = null;
		if (sourceElement.hasAttribute(JSF.ATTR_VALUE)) {

			Attr attr = sourceElement.getAttributeNode(JSF.ATTR_VALUE);
			text = visualDocument.createTextNode(sourceElement
					.getAttribute(JSF.ATTR_VALUE));
			elementData.addNodeData(new AttributeData(attr, textArea,
					true));

		} else {
			text = visualDocument.createTextNode(""); //$NON-NLS-1$
			elementData.addNodeData(new AttributeData(JSF.ATTR_VALUE,
					textArea, true));

		}
		textArea.appendChild(text);
		/*
		 * https://issues.jboss.org/browse/JBIDE-3225
		 * Components should render usual text inside
		 */
		VpeCreationData creationData = VisualDomUtil.createTemplateWithTextContainer(
				sourceElement, textArea, HTML.TAG_DIV, visualDocument);
		
		creationData.setElementData(elementData);

		return creationData;
	}
}
