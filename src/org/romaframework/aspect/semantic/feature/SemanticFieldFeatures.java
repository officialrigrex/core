/*
 * Copyright 2008 Luigi Dell'Aquila (luigi.dellaquila@assetdata.it)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.romaframework.aspect.semantic.feature;

import org.romaframework.aspect.semantic.SemanticAspect;
import org.romaframework.core.schema.Feature;
import org.romaframework.core.schema.FeatureType;

/**
 * IMPORTANT: PLEASE DO NOT RELY ON THIS RESOURCE, IT IS UNDER DEFINITION AND HEAVY DEVELOPMENT
 * 
 * @author Luigi Dell'Aquila (luigi.dellaquila@assetdata.it)
 * 
 */
public class SemanticFieldFeatures {

	public static final Feature<String>		PREDICATE	= new Feature<String>(SemanticAspect.ASPECT_NAME, "predicate", FeatureType.FIELD, String.class);
	public static final Feature<String[]>	RESOURCES	= new Feature<String[]>(SemanticAspect.ASPECT_NAME, "resources", FeatureType.FIELD, String[].class);
}
