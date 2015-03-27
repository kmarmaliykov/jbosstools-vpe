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

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * @author Konstantin Marmalyukov (kmarmaliykov)
 *
 */
public class PreviewEditorTestPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.vpe.preview.editor.test"; //$NON-NLS-1$
	
	public static final String VPE_TEST_EXTENTION_POINT_ID = "org.jboss.tools.vpe.ui.test"; //$NON-NLS-1$
	// The shared instance
	private static PreviewEditorTestPlugin plugin;
	
	/**
	 * The constructor
	 */
	public PreviewEditorTestPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static PreviewEditorTestPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns all extensions of {@value #VPE_TEST_EXTENTION_POINT_ID}
	 */
	public IExtension[] getVpeTestExtensions() {
		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = extensionRegistry
				.getExtensionPoint(VPE_TEST_EXTENTION_POINT_ID);
		IExtension[] extensions = extensionPoint.getExtensions();
		return extensions;
	}
}
