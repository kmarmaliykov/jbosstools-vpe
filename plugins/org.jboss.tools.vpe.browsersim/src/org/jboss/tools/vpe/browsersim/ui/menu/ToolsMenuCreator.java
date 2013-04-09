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
package org.jboss.tools.vpe.browsersim.ui.menu;

import java.util.List;
import java.util.UUID;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.jboss.tools.vpe.browsersim.model.Device;
import org.jboss.tools.vpe.browsersim.model.preferences.SpecificPreferences;
import org.jboss.tools.vpe.browsersim.ui.BrowserSim;
import org.jboss.tools.vpe.browsersim.ui.Messages;
import org.jboss.tools.vpe.browsersim.ui.debug.firebug.FireBugLiteLoader;
import org.jboss.tools.vpe.browsersim.ui.skin.BrowserSimSkin;

/**
 * @author Yahor Radtsevich (yradtsevich)
 * @author Konstantin Marmalyukov (kmarmaliykov)
 */

public class ToolsMenuCreator {
	
	public static void addFireBugLiteItem(Menu menu, final BrowserSimSkin skin) {
		MenuItem fireBugLite = new MenuItem(menu, SWT.PUSH);
		fireBugLite.setText(Messages.BrowserSim_FIREBUG_LITE);
		fireBugLite.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FireBugLiteLoader.startFireBugOpening(skin.getBrowser());
			}
		});
	}
	
	public static void addWeinreItem(Menu menu, final BrowserSimSkin skin, final String weinreScriptUrl,
			final String weinreClientUrl) {
		MenuItem weinre = new MenuItem(menu, SWT.PUSH);
		weinre.setText(Messages.BrowserSim_WEINRE);
		weinre.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String url = (String) skin.getBrowser().evaluate("if(window.WeinreServerURL && window.WeinreServerId) {return window.WeinreServerURL + 'client/#' + window.WeinreServerId} else {return null}");
				if (url == null) {
					String id = UUID.randomUUID().toString();
					skin.getBrowser().execute("var head = document.head;"
									+		"var script = document.createElement('script');"
									+		"head.appendChild(script);"
									+		"script.src='" + weinreScriptUrl + "#" + id + "'");

					url = weinreClientUrl + "#" + id;
				}

				Display display = skin.getBrowser().getDisplay();
				Shell shell = new Shell(display);
				shell.setLayout(new FillLayout());
				shell.setText("Weinre Inspector");
				final Browser browser;
				try {
					browser = new Browser(shell, SWT.WEBKIT);
				} catch (SWTError e2) {
					System.out.println("Could not instantiate Browser: " + e2.getMessage());
					display.dispose();
					return;
				}
				shell.open();
				browser.setUrl(url);
			}
		});
	}
	
	public static void addScreenshotMenuItem(Menu menu, final BrowserSimSkin skin, final String path) {
		MenuItem screenshot = new MenuItem(menu, SWT.CASCADE);
		screenshot.setText(Messages.Screenshots_Screenshot);

		Menu subMenu = ScreenshotMenuCreator.createScreenshotsMenu(menu, Display.getDefault(), skin.getShell(), path);
		screenshot.setMenu(subMenu);
	}
	
	public static void addSyncronizedWindowItem(Menu menu, final BrowserSimSkin skin,
			final List<Device> devices, final Boolean useSkins, final int orientationAngle, final String homeUrl) {
		MenuItem syncWindow = new MenuItem(menu, SWT.CASCADE);
		syncWindow.setText("Open Syncronized Window");
		Menu subMenu = new Menu(menu);
		syncWindow.setMenu(subMenu);

		for (final Device device : devices) {
			MenuItem deviceMenuItem = new MenuItem(subMenu, SWT.RADIO);
			deviceMenuItem.setText(device.getName());
			deviceMenuItem.setData(device);

			deviceMenuItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					MenuItem menuItem = (MenuItem) e.widget;
					if (menuItem.getSelection()) {
						int selectedDeviceIndex = devices.indexOf(menuItem.getData());
						if (selectedDeviceIndex < 0) {
							selectedDeviceIndex = 0;
						}
						SpecificPreferences sp = new SpecificPreferences(selectedDeviceIndex, useSkins,
								orientationAngle, null);

						BrowserSim browserSim1 = new BrowserSim(homeUrl);
						browserSim1.open(sp, skin.getBrowser().getUrl());
					}
				};
			});
		}
	}
}
