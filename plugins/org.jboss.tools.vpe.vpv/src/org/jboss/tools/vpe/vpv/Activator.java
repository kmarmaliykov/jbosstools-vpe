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

	private Map<Integer, VpvView> vpvViewRegistry;
	private static int vpvViewCounter = 0;

	private VpvController vpvController;

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
		
		vpvController = new VpvController();
		vpvServer = new VpvServer(vpvController);
		vpvViewRegistry = new HashMap<Integer, VpvView>();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		vpvViewRegistry = null;
		vpvServer.stop();
		vpvServer = null;
		vpvController = null;
		
		plugin = null;
		super.stop(context);
	}
	
	public int registerVpvView(VpvView vpvView) {
		vpvViewRegistry.put(vpvViewCounter, vpvView);
		return vpvViewCounter++;
	}
	
	public void unregisterVpvView(VpvView vpvView) {
		Integer key = null;
		for (Entry<Integer, VpvView> entry : vpvViewRegistry.entrySet()) {
			if (entry.getValue() == vpvView) {
				key = entry.getKey();
			}
		}
		
		if (key != null) {
			vpvViewRegistry.remove(key);
		}
	}
	
	public VpvView getVpvViewById(Integer id) {
		if (id == null) {
			return null;
		} else {
			return vpvViewRegistry.get(id);
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
