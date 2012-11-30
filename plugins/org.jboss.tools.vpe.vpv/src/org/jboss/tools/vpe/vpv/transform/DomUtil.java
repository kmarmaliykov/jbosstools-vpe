package org.jboss.tools.vpe.vpv.transform;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;

public class DomUtil {
	public static Node getParentNode(Node node) {
		if (node == null) {
			return null;
		} else if (node instanceof Attr) {
			return ((Attr) node).getOwnerElement();
		} else {
			return node.getParentNode();
		}
	}
}
