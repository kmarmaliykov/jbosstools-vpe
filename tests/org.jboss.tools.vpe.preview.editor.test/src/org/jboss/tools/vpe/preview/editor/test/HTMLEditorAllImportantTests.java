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
package org.jboss.tools.vpe.preview.editor.test;

import org.jboss.tools.vpe.preview.editor.test.editor.OpenEditorTest;
import org.jboss.tools.vpe.preview.editor.test.editor.PreviewReloadTest;
import org.jboss.tools.vpe.preview.editor.test.editor.RefrestOptionsTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * 
 * @author Konstantin Marmalyukov (kmarmaliykov)
 *
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
OpenEditorTest.class,
RefrestOptionsTest.class,
PreviewReloadTest.class
//ResourcesTest.class
})
public class HTMLEditorAllImportantTests {
}
