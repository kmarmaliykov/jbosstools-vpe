/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.vpe.preview.core.transform;

import org.jboss.tools.vpe.preview.core.template.VpeTemplate;
import org.jboss.tools.vpe.preview.core.template.VpvDefaultTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author Yahor Radtsevich (yradtsevich)
 */
public class VpvTemplateProvider {
	/**
	 * Returns a template class for given sourceNode
	 */
	public VpeTemplate getTemplate(Document sourceDocument, Node sourceNode) {
		// TODO: simple test implementation
//		if (sourceNode.getNodeType() == Node.ELEMENT_NODE) {
//			if ("h:inputText".equals(sourceNode.getNodeName())) { //XXX //$NON-NLS-1$
//				return new JsfInputTextTemplate();
//			} else if ("h:inputTextarea".equals(sourceNode.getNodeName())) { //$NON-NLS-1$
//				return new JsfInputTextAreaTemplate();	
//			} else if ("a4j:log".equals(sourceNode.getNodeName())) { //$NON-NLS-1$
//				return new Ajax4JSFLogTemplate();
//			}
//		}
		
		return new VpvDefaultTemplate();
	}
}
