package org.jboss.tools.vpe.vpv.transform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jboss.tools.vpe.vpv.mapping.NodeData;
import org.jboss.tools.vpe.vpv.mapping.VpeElementData;
import org.jboss.tools.vpe.vpv.template.VpeChildrenInfo;
import org.jboss.tools.vpe.vpv.template.VpeCreationData;
import org.jboss.tools.vpe.vpv.template.VpeTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VpvDomBuilder {
	public static final String ATTR_VPV_ID = "data-vpvid";
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
	
	public VisualMutation rebuildSubtree(VpvVisualModel visualModel, Document sourceDocument, Node sourceParent) {
		/*  mappedParent = sourceParent;
		 * 	if mappedParent not contained in sourceVisualMap {
		 * 		mappedParent = find an ascendant which is contained
		 * 	}
		 * 
		 * 10. remove mappedParent and its descendants from the sourceVisualMapping
		 * 20. remove mappedVisualParent and its descendants from the visualDocument
		 * 30. add newVisualNode to the place of mappedVisualParent
		 */
		Map<Node, Node> sourceVisualMapping = visualModel.getSourceVisualMapping();
		
		Node mappedSourceParent = sourceParent;
		Node oldMappedVisualParent = null;
		while (true) {
			if (mappedSourceParent != null) { 
				oldMappedVisualParent = sourceVisualMapping.get(mappedSourceParent);
				if (oldMappedVisualParent instanceof Element 
						&& ((Element) oldMappedVisualParent).hasAttribute(ATTR_VPV_ID)) {
					break;
				}
			} else {
				break;
			}
				
			mappedSourceParent = DomUtil.getParentNode(mappedSourceParent);
		}		
		
		removeSubtreeFromMapping(mappedSourceParent, sourceVisualMapping);
		
		Node newMappedVisualParent = convertNode(sourceDocument, mappedSourceParent, 
				visualModel.getVisualDocument(), sourceVisualMapping);
		
		long oldParentId = getNodeMarkerId(oldMappedVisualParent);
		long newParentId = -1; 
		if (newMappedVisualParent != null) {
			DomUtil.getParentNode(oldMappedVisualParent).replaceChild(newMappedVisualParent, oldMappedVisualParent);
			newParentId = markSubtree(newMappedVisualParent);
		} else {
			DomUtil.getParentNode(oldMappedVisualParent).removeChild(oldMappedVisualParent);
		}

		return new VisualMutation(oldParentId, newMappedVisualParent);
	}
	
	private long getNodeMarkerId(Node oldMappedVisualParent) {
		if (oldMappedVisualParent instanceof Element) {
			String stringMarkerId = ((Element) oldMappedVisualParent).getAttribute(ATTR_VPV_ID);
			if (stringMarkerId != null) {
				try {
					return Long.parseLong(stringMarkerId);
				} catch (NumberFormatException e) { // do not throw exception if cannot parse
				}
			}
		}
		return -1;
	}

	private void removeSubtreeFromMapping(Node sourceParent,
			Map<Node, Node> sourceVisualMapping) {
		sourceVisualMapping.remove(sourceParent);
		
		if (sourceParent.getNodeType() == Node.ELEMENT_NODE) {
			NamedNodeMap attributes = sourceParent.getAttributes();
			for (int i = 0; i < attributes.getLength(); i++) {
				Node attribute = attributes.item(i);
				sourceVisualMapping.remove(attribute);
			}
		}
		
		NodeList children = sourceParent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			removeSubtreeFromMapping(child, sourceVisualMapping);
		}
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
