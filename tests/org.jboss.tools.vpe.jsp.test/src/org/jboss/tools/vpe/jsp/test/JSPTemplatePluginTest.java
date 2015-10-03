/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.vpe.jsp.test;

import static org.junit.Assert.assertNotNull;

import org.jboss.tools.vpe.jsp.JspTemplatePluign;
import org.junit.Test;

public class JSPTemplatePluginTest {

	@Test
	public void testJspTemplatePluginActivator() {
		assertNotNull(JspTemplatePluign.getDefault());
	}

}
