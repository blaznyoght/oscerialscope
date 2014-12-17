package org.blaznyoght.oscerialscope;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.blaznyoght.oscerialscope.ui.UIProvider;

public class OscerialScope {
	private static final Logger LOG = LogManager.getLogger(OscerialScope.class);
	
	private ServiceLoader<UIProvider> uiProviderLoader = ServiceLoader.load(UIProvider.class);
	private Map<String, UIProvider> uiProviders = new HashMap<String, UIProvider>();
	 
	private void initProviders()
	{
		// Discover and register the available commands
		uiProviders.clear();
		uiProviderLoader.reload();
		Iterator<UIProvider> uiProvidersIterator = uiProviderLoader.iterator();
		while (uiProvidersIterator.hasNext())
		{
			UIProvider provider = uiProvidersIterator.next();
			uiProviders.put(provider.getId(), provider);
		}
	}
	
	public static void main(String[] args) {
		OscerialScope oscerialScope = new OscerialScope();
		oscerialScope.initProviders();
		UIProvider provider = null;
		if ("Pivot".equals(System.getProperty("ui"))) {
			provider = oscerialScope.uiProviders.get("Pivot");
		}
		else if (oscerialScope.uiProviders.get("Swing") != null) {
			provider = oscerialScope.uiProviders.get("Swing");
		}
		else {
			UIProvider[] providersArray = (UIProvider[]) oscerialScope.uiProviders.values().toArray();
			if (providersArray != null && providersArray.length > 0) {
				provider = providersArray[0];
			}
		}
		if (provider != null) {
			provider.provideUI(args);
		}
		else {
			LOG.error("No suitable UI found");
		}
	}

}
