/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.vpe.preview.core.transform;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Yahor Radtsevich (yradtsevich)
 * @author Ilya Buziuk (ibuziuk)
 */
public class VpvVisualModelHolderRegistry {
	private static int vpvViewCounter = 0;
	private final Map<Integer, VpvVisualModelHolder> visualModelHolderRegistry;
	
	public VpvVisualModelHolderRegistry() {
		visualModelHolderRegistry = new HashMap<Integer, VpvVisualModelHolder>();
	}

	public int registerHolder(VpvVisualModelHolder visualModelHolder) {
		visualModelHolderRegistry.put(vpvViewCounter, visualModelHolder);
		return vpvViewCounter++;
	}
	
	public void unregisterHolder(VpvVisualModelHolder visualModelHolder) {
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
	
	public VpvVisualModelHolder getHolderById(Integer id) {
		if (id == null) {
			return null;
		} else {
			return visualModelHolderRegistry.get(id);
		}
	}
}
