package org.jboss.tools.vpe.vpv.template;

import org.jboss.tools.vpe.vpv.template.VpeCreationData;
import org.jboss.tools.vpe.vpv.template.VpeTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * VPV template which should be used if a template is not found.
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
