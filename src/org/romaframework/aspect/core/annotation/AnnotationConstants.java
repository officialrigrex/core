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

package org.romaframework.aspect.core.annotation;

/**
 * Stores the common constants for annotations. They actually match with boolean values.
 * These annotations are strictly needed in Roma annotations.* 
 *
 */

public enum AnnotationConstants {

	UNSETTED(null), TRUE(Boolean.TRUE), FALSE(Boolean.FALSE);

	private Boolean	value;

	private AnnotationConstants(Boolean value) {
		this.value = value;
	}

	public Boolean getValue() {
		return value;
	}

	public static final String	DEF_VALUE	= "$DEFAULT_VALUE";
}
