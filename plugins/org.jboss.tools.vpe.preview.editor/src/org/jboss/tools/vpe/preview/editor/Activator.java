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
package org.jboss.tools.vpe.preview.editor;

import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jboss.tools.common.log.BaseUIPlugin;
import org.jboss.tools.usage.event.UsageEventType;
import org.jboss.tools.usage.event.UsageReporter;
import org.jboss.tools.vpe.preview.core.server.VpvServer;
import org.jboss.tools.vpe.preview.core.transform.VpvController;
import org.jboss.tools.vpe.preview.core.transform.VpvDomBuilder;
import org.jboss.tools.vpe.preview.core.transform.VpvTemplateProvider;
import org.jboss.tools.vpe.preview.core.transform.VpvVisualModelHolderRegistry;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Yahor Radtsevich (yradtsevich)
 * @author Ilya Buziuk (ibuziuk)
 */
public class Activator extends BaseUIPlugin {
	
	private static final String EDITOR_EVENT_ACTION = "editor"; //$NON-NLS-1$
	private static final String SOURCE_EVENT_LABEL = "source"; //$NON-NLS-1$
	private static final String VPE_EVENT_LABEL = "visual-vpe"; //$NON-NLS-1$
	private static final String VPV_EVENT_LABEL = "visual-vpv"; //$NON-NLS-1$
	
	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.vpe.preview.editor"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private VpvServer server;

	private VpvVisualModelHolderRegistry visualModelHolderRegistry;

	private VpvDomBuilder domBuilder;

	private UsageEventType editorEventType;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		VpvTemplateProvider templateProvider = new VpvTemplateProvider();
		domBuilder = new VpvDomBuilder(templateProvider);
		visualModelHolderRegistry = new VpvVisualModelHolderRegistry();
		VpvController vpvController = new VpvController(domBuilder, visualModelHolderRegistry);
		server = new VpvServer(vpvController);
		
		editorEventType = new UsageEventType(this, EDITOR_EVENT_ACTION, Messages.UsageEventTypeEditorLabelDescription, UsageEventType.HOW_MANY_TIMES_VALUE_DESCRIPTION);
		UsageReporter.getInstance().registerEvent(editorEventType);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		server.stop();
		server = null;
		visualModelHolderRegistry = null;
		domBuilder = null;
		
		plugin = null;
		super.stop(context);
	}
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	public VpvVisualModelHolderRegistry getVisualModelHolderRegistry() {
		return visualModelHolderRegistry;
	}

	public VpvDomBuilder getDomBuilder() {
		return domBuilder;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	public static URL getFileUrl(String path) {
		return plugin.getBundle().getEntry(path);
	}
	
	public VpvServer getServer() {
		return server;
	}
	
	public void countSourceTabEvent() {
		UsageReporter.getInstance().countEvent(editorEventType.event(SOURCE_EVENT_LABEL));
	}

	public void countVpeTabEvent() {
		UsageReporter.getInstance().countEvent(editorEventType.event(VPE_EVENT_LABEL));
	}

	public void countVpvTabEvent() {
		UsageReporter.getInstance().countEvent(editorEventType.event(VPV_EVENT_LABEL));
	}
}
