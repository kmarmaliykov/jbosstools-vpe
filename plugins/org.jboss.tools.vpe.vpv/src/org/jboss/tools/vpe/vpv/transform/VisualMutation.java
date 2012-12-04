package org.jboss.tools.vpe.vpv.transform;

import org.w3c.dom.Node;

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
