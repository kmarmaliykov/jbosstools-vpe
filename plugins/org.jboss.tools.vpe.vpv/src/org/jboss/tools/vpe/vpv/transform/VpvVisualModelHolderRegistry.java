package org.jboss.tools.vpe.vpv.transform;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class VpvVisualModelHolderRegistry {
	private static int vpvViewCounter = 0;
	private Map<Integer, VpvVisualModelHolder> visualModelHolderRegistry;
	
	
	
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
