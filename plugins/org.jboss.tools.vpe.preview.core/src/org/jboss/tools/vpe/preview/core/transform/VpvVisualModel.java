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

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author Yahor Radtsevich (yradtsevich)
 * @author Ilya Buziuk (ibuziuk)
 */
public class VpvVisualModel {
	private Map<Node, Node> sourceVisualMapping;
	private Document visualDocument;

	public VpvVisualModel(Document visualDocument,
			Map<Node, Node> sourceVisualMapping) {
		super();
		this.visualDocument = visualDocument;
		this.sourceVisualMapping = sourceVisualMapping;
	}
	
	public Document getVisualDocument() {
		return visualDocument;
	}
	
	public Map<Node, Node> getSourceVisualMapping() {
		return sourceVisualMapping;
	}
}
