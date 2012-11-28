package org.jboss.tools.vpe.vpv;

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jboss.tools.vpe.vpv.server.VpvServer;
import org.jboss.tools.vpe.vpv.transform.ResourceAcceptor;
import org.jboss.tools.vpe.vpv.transform.VpvController;
import org.jboss.tools.vpe.vpv.transform.VpvDomBuilder;
import org.jboss.tools.vpe.vpv.transform.VpvTemplateProvider;
import org.jboss.tools.vpe.vpv.transform.VpvVisualModelHolderRegistry;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.vpe.vpv"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private VpvServer server;

	private VpvVisualModelHolderRegistry visualModelHolderRegistry;
	
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
		VpvDomBuilder domBuilder = new VpvDomBuilder(templateProvider);
		visualModelHolderRegistry = new VpvVisualModelHolderRegistry();
		VpvController vpvController = new VpvController(domBuilder, visualModelHolderRegistry);
//		vpvController.getResource("jspTest", "/WebContent/pages/components/body.jsp", null, new ResourceAcceptor() {
//			@Override
//			public void acceptText(String text, String mimeType) {
//				System.out.println("mimeType: " + mimeType);
//				System.out.println(text);
//			}
//			
//			@Override
//			public void acceptFile(File file, String mimeType) {
//				System.out.println("mimeType: " + mimeType);
//				System.out.println(file.getAbsolutePath());
//			}
//		});
		server = new VpvServer(vpvController);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		server.stop();
		server = null;
		visualModelHolderRegistry = null;
		
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
	
	public VpvServer getServer() {
		return server;
	}
	
	public static void logError(Throwable e) {
		getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage(), e));
	}
	
	public static void logInfo(String info) {
		getDefault().getLog().log(new Status(IStatus.INFO, PLUGIN_ID, info));
	}
}
