/*******************************************************************************
 * Copyright (c) 2007-2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.vpe.preview.core.template;

import org.jboss.tools.vpe.preview.core.template.VpeCreationData;
import org.jboss.tools.vpe.preview.core.template.VpeTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * VPV template which should be used if a template is not found.
 * 
 * @author Yahor Radtsevich (yradtsevich)
 */
public class VpvDefaultTemplate implements VpeTemplate {

	@Override
	public VpeCreationData create(Node sourceNode, Document visualDocument) {
		Node visualNode = null;
		
		short sourceNodeType = sourceNode.getNodeType();
		if (sourceNodeType == Node.ELEMENT_NODE) {
			Element visualElement = visualDocument.createElement(sourceNode.getNodeName());
			NamedNodeMap sourceNodeAttributes = sourceNode.getAttributes();
			visualNode = visualElement;
			for (int i = 0; i < sourceNodeAttributes.getLength(); i++) {
				Node sourceNodeAttaribute = sourceNodeAttributes.item(i);
				visualElement.setAttribute(sourceNodeAttaribute.getNodeName(), sourceNodeAttaribute.getNodeValue());
			}
		} else if (sourceNodeType == Node.TEXT_NODE) {
			Text visualText = visualDocument.createTextNode(sourceNode.getTextContent());
			visualNode = visualText; 
		}
		
		return new VpeCreationData(visualNode);
	}

}
