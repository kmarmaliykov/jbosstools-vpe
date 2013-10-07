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
package org.jboss.tools.vpe.cordovasim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.tools.vpe.browsersim.BrowserSimArgs;

public class CordovaSimArgs {
	private static final Integer DEFAULT_PORT = 0;// any free port
	
	private static String rootFolder;
	private static String startPage;
	private static Integer port;
	public static boolean standalone;

	public static void parseArgs(String[] args) {
		List<String> params = new ArrayList<String>(Arrays.asList(args));
		standalone = !params.contains(BrowserSimArgs.NOT_STANDALONE);
		if (!standalone) {
			params.remove(BrowserSimArgs.NOT_STANDALONE);
		}
		
		int portParameterIndex = params.indexOf("-port");
		if (portParameterIndex >= 0) {
			params.remove(portParameterIndex);
			port = Integer.parseInt(params.remove(portParameterIndex));
		} else {
			port = DEFAULT_PORT;
		}
		
		if (params.size() > 0) {
			startPage = params.remove(params.size() - 1); // the last parameter
		} else {
			startPage = "";
		}
		
		if (params.size() > 0) {
			rootFolder = params.remove(params.size() - 1); // the parameter before the last one
		} else {
			rootFolder = ".";
		}
	}

	public static String getRootFolder() {
		return rootFolder;
	}

	public static String getStartPage() {
		return startPage;
	}

	public static Integer getPort() {
		return port;
	}
	
	public static void setPort(Integer port) {
		CordovaSimArgs.port = port;
	}

	public static boolean isStandalone() {
		return standalone;
	}
}
