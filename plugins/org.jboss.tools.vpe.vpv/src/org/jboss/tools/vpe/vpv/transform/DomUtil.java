package org.jboss.tools.vpe.vpv.transform;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
	
	public static String nodeToString(Node node) throws TransformerException {
		Transformer transformer = getTransformer();
		StringWriter buffer = new StringWriter();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(new DOMSource(node), new StreamResult(buffer));
		return buffer.toString();
	}
	
	private static Transformer transformer;
	public static Transformer getTransformer() throws TransformerConfigurationException {
		if (transformer == null) {
			TransformerFactory transFactory = TransformerFactory.newInstance();
			transformer = transFactory.newTransformer();
		}
		
		return transformer;
	}
}
