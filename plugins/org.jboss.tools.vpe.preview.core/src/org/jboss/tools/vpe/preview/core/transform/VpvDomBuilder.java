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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jboss.tools.vpe.preview.core.mapping.NodeData;
import org.jboss.tools.vpe.preview.core.mapping.VpeElementData;
import org.jboss.tools.vpe.preview.core.template.VpeChildrenInfo;
import org.jboss.tools.vpe.preview.core.template.VpeCreationData;
import org.jboss.tools.vpe.preview.core.template.VpeTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Yahor Radtsevich (yradtsevich)
 * @author Ilya Buziuk (ibuziuk)
 */
public class VpvDomBuilder {
	public static final String ATTR_VPV_ID = "data-vpvid"; //$NON-NLS-1$
	private static long markerId = 0;
	private VpvTemplateProvider templateProvider;

	public VpvDomBuilder(VpvTemplateProvider templateProvider) {
		this.templateProvider = templateProvider;
	}

	public VpvVisualModel buildVisualModel(Document sourceDocument) throws ParserConfigurationException {
		Document visualDocument = createDocument();
		Map<Node, Node> sourceVisualMapping = new HashMap<Node, Node>();
		Element documentElement = sourceDocument.getDocumentElement();
		Node visualRoot = convertNode(sourceDocument, documentElement, visualDocument, sourceVisualMapping);

		if (visualRoot != null) {
			markSubtree(visualRoot);
			visualDocument.appendChild(visualRoot);
		}

		VpvVisualModel visualModel = new VpvVisualModel(visualDocument, sourceVisualMapping);
		return visualModel;
	}
	
	private long markSubtree(Node visualParent) {
		if (visualParent.getNodeType() == Node.ELEMENT_NODE) {
			Element visualParentElement = (Element) visualParent;
			NodeList children = visualParentElement.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				markSubtree(children.item(i));
			}
			
			// The outermost element will have the greatest id.
			// Also this means if a subelement was modified, child element is will be greater.
			long markerId = getNextMarkerId();
			visualParentElement.setAttribute(ATTR_VPV_ID, Long.toString( markerId ));
			
			return markerId;
		}
		
		return -1;
	}

	/**
	 * Converts a sourceNote to its visual representation
	 */
	private Node convertNode(Document sourceDocument, Node sourceNode, Document visualDocument,
			Map<Node, Node> sourceVisualMapping) {
		VpeTemplate vpeTemplate = templateProvider.getTemplate(sourceDocument, sourceNode);
		VpeCreationData creationData = vpeTemplate.create(sourceNode, visualDocument);

		Node visualNode = creationData.getVisualNode();
		if (visualNode != null) {
			sourceVisualMapping.put(sourceNode, visualNode);
		}
		
		VpeElementData elementData = creationData.getElementData();
		if (elementData != null) {
			List<NodeData> nodesData = elementData.getNodesData();
			if (nodesData != null) {
				for (NodeData nodeData : nodesData) {
					if (nodeData.getSourceNode() != null && nodeData.getVisualNode() != null) {
						sourceVisualMapping.put(nodeData.getSourceNode(), nodeData.getVisualNode());
					}
				}
			}
		}
		
		List<VpeChildrenInfo> childrenInfos = creationData.getChildrenInfoList();
		if (childrenInfos != null) {
			for (VpeChildrenInfo childrenInfo : childrenInfos) {
				List<Node> sourceChildren = childrenInfo.getSourceChildren();
				Element visualParent = childrenInfo.getVisualParent();
				for (Node sourceChild : sourceChildren) {
					Node visualChild 
							= convertNode(sourceDocument, sourceChild, visualDocument, sourceVisualMapping);
					if (visualChild != null) {
						visualParent.appendChild(visualChild);
					}
				}
			}
		} else {
			NodeList sourceChildren = sourceNode.getChildNodes();
			for (int i = 0; i < sourceChildren.getLength(); i++) {
				Node sourceChild = sourceChildren.item(i);
				Node visualChild 
						= convertNode(sourceDocument, sourceChild, visualDocument, sourceVisualMapping);
				if (visualChild != null) {
					visualNode.appendChild(visualChild);
				}
			}
		}
		
		
		
		return visualNode;
	}

	private Document createDocument() throws ParserConfigurationException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.newDocument();
		return document;
	}
	
	private static long getNextMarkerId() {
		return markerId++;
	}
}
