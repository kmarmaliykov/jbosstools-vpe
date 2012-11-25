package org.jboss.tools.vpe.vpv.transform;

public class VisualMutation {
	private long oldParentId;
	private long newParentId;
	
	public VisualMutation(long oldParentId, long newParentId) {
		super();
		this.oldParentId = oldParentId;
		this.newParentId = newParentId;
	}
	public long getOldParentId() {
		return oldParentId;
	}
	public long getNewParentId() {
		return newParentId;
	}
}
