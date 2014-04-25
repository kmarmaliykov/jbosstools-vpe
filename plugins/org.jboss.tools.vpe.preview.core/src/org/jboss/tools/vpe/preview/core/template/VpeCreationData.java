/*******************************************************************************
 * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.vpe.preview.core.template;

import java.util.ArrayList;
import java.util.List;

import org.jboss.tools.vpe.preview.core.mapping.VpeElementData;
import org.w3c.dom.Node;

/**
 * @author Yahor Radtsevich (yradtsevich)
 */
public class VpeCreationData {
	private Node visualNode;
	private List<VpeChildrenInfo> childrenInfoList;
	private List<Node> illegalChildren;

	/**
	 * @deprecated - You must use elementData. If VpeElementData has not
	 *             necessary functionality you must extend its
	 */
	private Object data;
	private VpeElementData elementData;

	public VpeCreationData(Node node) {
		this.visualNode = node;
	}
	
	public VpeCreationData(Node visualNode, boolean initializeChildren) {
		this.visualNode = visualNode;
		if (initializeChildren)
			this.childrenInfoList = new ArrayList<VpeChildrenInfo>();
	} 

	public Node getVisualNode() {
		return visualNode;
	}

	public void addChildrenInfo(VpeChildrenInfo info) {
		if (childrenInfoList == null) {
			childrenInfoList = new ArrayList<VpeChildrenInfo>();
		}
		childrenInfoList.add(info);
	}

	public void setChildrenInfoList(List<VpeChildrenInfo> childrenInfoList) {
		this.childrenInfoList = childrenInfoList;
	}

	public List<VpeChildrenInfo> getChildrenInfoList() {
		return childrenInfoList;
	}

	public void addIllegalChild(Node child) {
		if (illegalChildren == null) {
			illegalChildren = new ArrayList<Node>();
		}
		illegalChildren.add(child);
	}

	public List<Node> getIllegalChildren() {
		return illegalChildren;
	}

	/**
	 * @deprecated - You must use elementData. If VpeElementData has not
	 *             necessary functionality you must extend its
	 * @param data
	 */
	public void setData(Object data) {
		this.data = data;
	}

	/**
	 * @deprecated - You must use elementData. If VpeElementData has not
	 *             necessary functionality you must extend its
	 * @return
	 */
	public Object getData() {
		return data;
	}

	/**
	 * get element data
	 * 
	 * @return
	 */
	public VpeElementData getElementData() {
		return elementData;
	}

	/**
	 * set element data
	 * 
	 * @param elementData
	 */
	public void setElementData(VpeElementData elementData) {
		this.elementData = elementData;
	}

}
