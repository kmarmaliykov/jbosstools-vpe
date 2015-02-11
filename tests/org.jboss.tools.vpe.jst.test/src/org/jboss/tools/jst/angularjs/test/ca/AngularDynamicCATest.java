/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.jst.angularjs.test.ca;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.jboss.tools.jst.jsp.test.ca.ContentAssistantTestCase;

/**
 * @author Alexey Kazakov
 */
public class AngularDynamicCATest extends ContentAssistantTestCase {

	@Override
	public void setUp() throws Exception {
		project = ResourcesPlugin.getWorkspace().getRoot().getProject("AngularPhonecat");
	}

	@Override
	public void tearDown() throws Exception {
		closeEditor();
		super.tearDown();
	}

	public void testControllers(){
		checkProposals("first - {{}}", "phone", "phones", "this");
		checkProposals("second - {{ph}}", true, "second - {{phone}}");
	}

	public void testControllerProperties(){
		checkProposals("third - {{phone.}}", "name", "snippet", "test");
		checkProposals("fourth - {{phone.s}}", true, "fourth - {{phone.snippet}}");
	}

	private ICompletionProposal[] checkProposals(String substring, String... proposals) {
		return checkProposals(substring, false, proposals);
	}

	private ICompletionProposal[] checkProposals(String substring, boolean exactly, String... proposals) {
		return checkProposals("index.html", substring, substring.length()-2, proposals, exactly, false);
	}
}