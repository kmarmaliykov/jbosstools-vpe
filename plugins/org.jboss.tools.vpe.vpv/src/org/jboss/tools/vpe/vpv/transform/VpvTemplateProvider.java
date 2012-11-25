package org.jboss.tools.vpe.vpv.transform;

import org.jboss.tools.jsf.vpv.ajax4jsf.template.Ajax4JSFLogTemplate;
import org.jboss.tools.jsf.vpv.jsf.template.JsfInputTextAreaTemplate;
import org.jboss.tools.jsf.vpv.jsf.template.JsfInputTextTemplate;
import org.jboss.tools.vpe.vpv.template.VpeTemplate;
import org.jboss.tools.vpe.vpv.template.VpvDefaultTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class VpvTemplateProvider {
	/**
	 * Returns a template class for given sourceNode
	 */
	public VpeTemplate getTemplate(Document sourceDocument, Node sourceNode) {
		// TODO: simple test implementation
		if (sourceNode.getNodeType() == Node.ELEMENT_NODE) {
			if ("h:inputText".equals(sourceNode.getNodeName())) { //XXX
				return new JsfInputTextTemplate();
			} else if ("h:inputTextarea".equals(sourceNode.getNodeName())) {
				return new JsfInputTextAreaTemplate();	
			} else if ("a4j:log".equals(sourceNode.getNodeName())) {
				return new Ajax4JSFLogTemplate();
			}
		}
		
		return new VpvDefaultTemplate();
	}
}
