/*******************************************************************************
 * Copyright (c) 2007-2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.vpe.browsersim.ui.skin;

/**
 * @author Yahor Radtsevich (yradtsevich)
 */

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.jboss.tools.vpe.browsersim.model.FitToScreen;
import org.jboss.tools.vpe.browsersim.model.preferences.CommonPreferences;
import org.jboss.tools.vpe.browsersim.model.preferences.SpecificPreferences;
import org.jboss.tools.vpe.browsersim.ui.SizeWarningDialog;
import org.jboss.tools.vpe.browsersim.util.BrowserSimUtil;

public class ResizableSkinSizeAdvisorImpl implements ResizableSkinSizeAdvisor{
	private CommonPreferences commonPreferences;
	private SpecificPreferences specificPreferences;
	private Shell shell;
	
	private LocationAdapter zoomAdapter;
	
	public ResizableSkinSizeAdvisorImpl(CommonPreferences cp, SpecificPreferences sp, Shell shell) {
		super();
		this.commonPreferences = cp;
		this.specificPreferences = sp;
		this.shell = shell;
	}

	@Override
	public Point checkWindowSize(int orientation, Point prefferedSize, Point prefferedShellSize, final Browser browser) {
		Rectangle clientArea = BrowserSimUtil.getMonitorClientArea(shell.getMonitor());
		if (zoomAdapter != null) {
			browser.removeLocationListener(zoomAdapter);
		}
		
		FitToScreen fitToScreen = null;
		if (commonPreferences.getFitToScreen() == FitToScreen.PROMPT) {
			if (prefferedShellSize.x > clientArea.width || prefferedShellSize.y > clientArea.height) { 
				String deviceName = commonPreferences.getDevices().get(specificPreferences.getSelectedDeviceIndex()).getName();
				
				SizeWarningDialog dialog = new SizeWarningDialog(shell, new Point(clientArea.width, clientArea.height),
						prefferedShellSize, deviceName,
						orientation == SpecificPreferences.ORIENTATION_PORTRAIT || orientation == SpecificPreferences.ORIENTATION_PORTRAIT_INVERTED);
				dialog.open();

				fitToScreen = dialog.getFitToScreen();
				if (dialog.getRememberDecision()) {
					commonPreferences.setFitToScreen(fitToScreen);
				}
			}
		} else {
			fitToScreen = commonPreferences.getFitToScreen();
		}

		Point size = new Point(prefferedShellSize.x, prefferedShellSize.y);
		double bsZoom = Math.min((double)clientArea.width/prefferedShellSize.x, (double)clientArea.height/prefferedShellSize.y);
		
		if (FitToScreen.ALWAYS_FIT.equals(fitToScreen) && bsZoom < 1) {
			size.x = (int) (prefferedShellSize.x * bsZoom);
			size.y = (int) (prefferedShellSize.y * bsZoom);			
			
			double pageZoom = 1;
			String pageZoomValue = ((String) browser.evaluate("return document.documentElement.style.zoom")).trim();
			if (!pageZoomValue.isEmpty()) {
				if (pageZoomValue.endsWith("%")) {
					pageZoomValue = pageZoomValue.replace("%", "");
					pageZoom = Double.parseDouble(pageZoomValue) / 100;
				} 
				pageZoom = Double.parseDouble(pageZoomValue);
			}
			
			final double zoom = pageZoom * bsZoom;
			setPageZoom(browser, zoom);
			zoomAdapter = new LocationAdapter() {
				@Override
				public void changed(LocationEvent event) {
					browser.execute(
				    	      "(function(){" +
				    	       "if (!document.getElementById('browserSimZoom')) {" +
				    	        "var f = function(){" +
				    	         "document.removeEventListener('DOMNodeInserted', f);" +
				    	         "el=document.createElement('style');" +
				    	         "el.id = 'browserSimZoom';" +
				    	         "el.innerText='html{zoom:" + zoom + "}';" +
				    	         "document.documentElement.appendChild(el);" +
				    	        "};" +
				    	        "document.addEventListener('DOMNodeInserted', f);" +
				    	       "}" +
				    	      "})()");
				}
			};
			browser.addLocationListener(zoomAdapter);
		} else {
			removePageZoom(browser);
		}

		return size;
	}
	
	private void setPageZoom(Browser browser, double zoom) {
		browser.execute(
	    	      "(function(){" +
	    	       "if (!document.getElementById('browserSimZoom')) {" +
	    	         "document.removeEventListener('DOMNodeInserted', f);" +
	    	         "el=document.createElement('style');" +
	    	         "el.id = 'browserSimZoom';" +
	    	         "document.documentElement.appendChild(el);" +
	    	       "}" +
	    	       "el.innerText='html{zoom:" + zoom + "}';" +
	    	      "})()");
	}
	
	private void removePageZoom(Browser browser) {
		browser.execute(
	    	      "(function(){" +
	    	       "if (document.getElementById('browserSimZoom')) {" +
	    	    		"var zoomElement = document.getElementById('browserSimZoom');" +
	    	        	"zoomElement.parentNode.removeChild(zoomElement);" +
	    	       "}" +
	    	      "})()");
	}
}
