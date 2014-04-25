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

import org.w3c.dom.Node;

/**
 * @author Yahor Radtsevich (yradtsevich)
 * @author Ilya Buziuk (ibuziuk)
 */
public class VisualMutation {
	private long oldParentId;
	private Node newParentNode;
	
	public VisualMutation(long oldParentId, Node newParentNode) {
		super();
		this.oldParentId = oldParentId;
		this.newParentNode = newParentNode;
	}
	public long getOldParentId() {
		return oldParentId;
	}
	public Node getNewParentNode() {
		return newParentNode;
	}
}
