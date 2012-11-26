package org.jboss.tools.vpe.vpv;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jboss.tools.vpe.vpv.server.VpvServer;
import org.jboss.tools.vpe.vpv.transform.VpvController;
import org.jboss.tools.vpe.vpv.transform.VpvDomBuilder;
import org.jboss.tools.vpe.vpv.transform.VpvTemplateProvider;
import org.jboss.tools.vpe.vpv.transform.VpvVisualModelHolder;
import org.jboss.tools.vpe.vpv.views.VpvView;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jboss.tools.vpe.vpv"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private Map<Integer, VpvVisualModelHolder> visualModelHolderRegistry;
	private static int vpvViewCounter = 0;

	private VpvServer vpvServer;
	
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
		VpvController vpvController = new VpvController(domBuilder);
		vpvServer = new VpvServer(vpvController);
		visualModelHolderRegistry = new HashMap<Integer, VpvVisualModelHolder>();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		visualModelHolderRegistry = null;
		vpvServer.stop();
		vpvServer = null;
		
		plugin = null;
		super.stop(context);
	}
	
	public int registerVisualModelHolder(VpvVisualModelHolder visualModelHolder) {
		visualModelHolderRegistry.put(vpvViewCounter, visualModelHolder);
		return vpvViewCounter++;
	}
	
	public void unregisterVisualModelHolder(VpvVisualModelHolder visualModelHolder) {
		Integer key = null;
		for (Entry<Integer, VpvVisualModelHolder> entry : visualModelHolderRegistry.entrySet()) {
			if (entry.getValue() == visualModelHolder) {
				key = entry.getKey();
			}
		}
		
		if (key != null) {
			visualModelHolderRegistry.remove(key);
		}
	}
	
	public VpvVisualModelHolder getVisualModelHolderById(Integer id) {
		if (id == null) {
			return null;
		} else {
			return visualModelHolderRegistry.get(id);
		}
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
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
	
	public static void logError(Throwable e) {
		getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, e.getMessage(), e));
	}
}
