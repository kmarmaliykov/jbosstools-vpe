/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.vpe.preview.core.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author Ilya Buziuk (ibuziuk)
 */
public final class TransformUtil {

	private TransformUtil() {
	}

	public static Node getVisualNodeByVpvId(VpvVisualModel visualModel, String selectedElementId)
			throws XPathExpressionException {
		Document visualDocument = visualModel.getVisualDocument();

		XPath xPath = XPathFactory.newInstance().newXPath();
		String expression = "//*[@" + VpvDomBuilder.ATTR_VPV_ID + "='" + selectedElementId + "']"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		Node node = (Node) xPath.compile(expression).evaluate(visualDocument, XPathConstants.NODE);

		return node;
	}

	public static Node getSourseNodeByVisualNode(VpvVisualModel visualModel, Node visualNode) {
		Map<Node, Node> sourceVisualMapping = visualModel.getSourceVisualMapping();
		for (Entry<Node, Node> entry : sourceVisualMapping.entrySet()) {
			if (entry.getValue().equals(visualNode)) {
				return entry.getKey();
			}
		}
		return null;
	}

	public static List<String> getAllMatches(String text, String regex) {
		List<String> matches = new ArrayList<String>();
		Matcher m = Pattern.compile("(?=(" + regex + "))").matcher(text); //$NON-NLS-1$ //$NON-NLS-2$
		while (m.find()) {
			matches.add(m.group(1));
		}
		Collections.sort(matches);
		return matches;
	}

	public static String getSelectedElementId(String text, String regex) {
		List<String> allMatches = getAllMatches(text, regex);
		if (allMatches != null && !allMatches.isEmpty()) {
			return allMatches.get(allMatches.size() - 1); // The very last is the biggest one
		}
		return null;
	}

}
