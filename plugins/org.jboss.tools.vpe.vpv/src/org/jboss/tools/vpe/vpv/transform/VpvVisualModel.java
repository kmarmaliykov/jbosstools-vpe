package org.jboss.tools.vpe.vpv.transform;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

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
