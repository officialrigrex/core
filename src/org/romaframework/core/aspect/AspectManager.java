/*
 * Copyright 2006 Luca Garulli (luca.garulli--at--assetdata.it)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.romaframework.core.aspect;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.romaframework.core.config.RomaApplicationContext;

/**
 * Manage Aspect implementations.
 * 
 * @author Luca Garulli (luca.garulli--at--assetdata.it)
 * 
 */
public class AspectManager {

	protected Collection<Aspect>	aspectCollection;
	protected Map<String, Aspect>	defaults	= new LinkedHashMap<String, Aspect>();

	protected AspectManager() {
	}

	/**
	 * Register the aspect already started. It's used by SelfRegistrantAspect.
	 * 
	 * @param iAspect
	 *          Aspect to register
	 */
	private void registerAspect(Aspect iAspect) {
		defaults.put(iAspect.aspectName(), iAspect);
		updateAspectCollection();
	}

	private void updateAspectCollection() {
		synchronized (defaults) {
			aspectCollection = Collections.unmodifiableCollection(defaults.values());
		}
	}

	public Collection<Aspect> getAspectCollection() {
		synchronized (defaults) {
			return aspectCollection;
		}
	}

	public Aspect getAspect(String name) {
		return defaults.get(name);
	}

	public Map<String, Aspect> getDefaults() {
		return defaults;
	}

	public void setDefaults(Map<String, Aspect> iDefaults) {
		this.defaults.putAll(iDefaults);
		updateAspectCollection();
	}

	public String getStatus() {
		return null;
	}

	public void registerAspectIfNotExist(Aspect aspect) {
		if (getAspect(aspect.aspectName()) == null) {
			registerAspect(aspect);
		}
	}

	public static Collection<AspectConfigurator> getConfigurators() {
		return RomaApplicationContext.getInstance().getComponentAspect().getComponentsOfClass(AspectConfigurator.class).values();
	}

}
