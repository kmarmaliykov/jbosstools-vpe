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

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * @author Yahor Radtsevich (yradtsevich)
 * @author Ilya Buziuk (ibuziuk)
 */
public class DomUtil {
	public static Node getParentNode(Node node) {
		if (node == null) {
			return null;
		} else if (node instanceof Attr) {
			return ((Attr) node).getOwnerElement();
		} else {
			return node.getParentNode();
		}
	}

	public static String nodeToString(Node node) throws TransformerException {
		Transformer transformer = getTransformer();
		StringWriter buffer = new StringWriter();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); //$NON-NLS-1$
		transformer.transform(new DOMSource(node), new StreamResult(buffer));
		return buffer.toString();
	}

	private static Transformer transformer;

	public static Transformer getTransformer() throws TransformerConfigurationException {
		if (transformer == null) {
			TransformerFactory transFactory = TransformerFactory.newInstance();
			transformer = transFactory.newTransformer();
		}

		return transformer;
	}

}
