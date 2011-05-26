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

package org.romaframework.core.schema;


/**
 * Represent a base element for an entity.
 * 
 * @author Luca Garulli (luca.garulli--at--assetdata.it)
 */
public abstract class SchemaElement extends SchemaFeatures implements Comparable<SchemaElement> {
	private static final long	serialVersionUID	= -4789886810661429988L;

	protected String					name;
	protected int							order;

	public final static int		DEF_ORDER					= -1;

	public SchemaElement(String iName, FeatureType featureType) {
		super(featureType);
		order = DEF_ORDER;
		name = iName;
	}

	public int compareTo(SchemaElement iField) {
		int otherOrder = iField.getOrder();

		if (otherOrder == order)
			return 0;

		if (otherOrder == -1)
			return 1;

		if (order == -1)
			return -1;

		return order > otherOrder ? 1 : -1;
	}

	public String getName() {
		return name;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getFullName() {
		return getName();
	}
}
