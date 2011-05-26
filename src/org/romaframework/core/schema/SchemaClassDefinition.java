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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.romaframework.aspect.authentication.UserObjectPermissionListener;
import org.romaframework.core.Roma;
import org.romaframework.core.Utility;

public abstract class SchemaClassDefinition extends SchemaFeatures {

	private static final long						serialVersionUID	= 2060007769938743965L;
	protected Map<String, SchemaField>	fields;
	protected Map<String, SchemaAction>	actions;
	protected Map<String, SchemaEvent>	events;

	protected List<SchemaField>					orderedFields;
	protected List<SchemaAction>				orderedActions;

	private static Log									log								= LogFactory.getLog(SchemaClassDefinition.class);

	public SchemaClassDefinition() {
		super(FeatureType.CLASS);
		fields = new HashMap<String, SchemaField>();
		actions = new HashMap<String, SchemaAction>();
		events = new HashMap<String, SchemaEvent>();

		orderedFields = new ArrayList<SchemaField>();
		orderedActions = new ArrayList<SchemaAction>();
	}

	public abstract String getName();

	public abstract SchemaClass getSchemaClass();

	public SchemaField getField(String iFieldName) {
		String fieldName;
		int sepPos = iFieldName.indexOf(Utility.PACKAGE_SEPARATOR);
		if (sepPos == -1)
			fieldName = iFieldName;
		else
			fieldName = iFieldName.substring(0, sepPos);

		SchemaField field = fields.get(fieldName);

		if (sepPos > -1 && field != null) {
			if (field.getType() == null)
				return null;
			field = field.getType().getField(iFieldName.substring(sepPos + 1));
		}

		return field;
	}

	public Iterator<SchemaField> getFieldIterator() {
		return orderedFields.iterator();
	}

	/**
	 * Search an action by checking the name and parameter type. It works very similar to the JVM.
	 */
	public SchemaAction getAction(String iActionName, Object[] iActionParameters) {
		for (SchemaAction a : orderedActions) {
			if (a.getName().equals(iActionName)) {
				if (iActionParameters == null || iActionParameters.length == 0 && a.getParameterNumber() == 0)
					// NO PARAMETERS
					return a;

				if (iActionParameters.length == a.getParameterNumber()) {
					SchemaParameter par;
					Iterator<SchemaParameter> itPar = a.getParameterIterator();
					int i;
					for (i = 0; i < iActionParameters.length; ++i) {
						if (!itPar.hasNext())
							break;

						par = itPar.next();

						if (!par.getType().isAssignableFrom(Roma.schema().getSchemaClass(iActionParameters[i].getClass())))
							break;
					}

					if (i == iActionParameters.length)
						// ALL PARAMETER MATCHED: RETURN THIS ONE
						return a;
				}
			}
		}

		return null;
	}

	public SchemaAction getAction(String iActionName) {
		SchemaAction action = null;
		int sepPos = iActionName.indexOf(Utility.PACKAGE_SEPARATOR);
		if (sepPos == -1) {
			action = actions.get(iActionName);
		} else {
			// FOLLOW THE PATH UNTIL LAST FIELD AND THEN GET THE ACTION
			SchemaField field = getField(iActionName.substring(0, sepPos));
			if (field != null) {
				action = field.getType().getAction(iActionName.substring(sepPos + 1));
			}
		}
		return action;
	}

	public Iterator<SchemaAction> getActionIterator() {
		return orderedActions.iterator();
	}

	public SchemaEvent getEvent(String iEventName) {
		// TODO: Manage recursively
		return events.get(iEventName);
	}

	public Iterator<SchemaEvent> getEventIterator() {
		return events.values().iterator();
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		SchemaClassDefinition cloned = (SchemaClassDefinition) super.clone();

		cloned.fields = new HashMap<String, SchemaField>();
		cloned.actions = new HashMap<String, SchemaAction>();
		cloned.events = new HashMap<String, SchemaEvent>();

		cloned.orderedFields = new ArrayList<SchemaField>();
		cloned.orderedActions = new ArrayList<SchemaAction>();

		cloned.copyDefinition(this);
		return cloned;
	}

	/**
	 * Copy the definition from class schema.
	 * 
	 * @param iSource
	 */
	public abstract void copyDefinition(SchemaClassDefinition iSource);

	public void setField(String iFieldName, SchemaField iField) {
		if (fields.containsKey(iFieldName)) {
			// REPLACE IT
			for (int i = 0; i < orderedFields.size(); ++i) {
				if (orderedFields.get(i).getName() != null && orderedFields.get(i).getName().equals(iFieldName)) {
					orderedFields.remove(i);
					break;
				}
			}
		}

		orderedFields.add(iField);
		fields.put(iFieldName, iField);
	}

	public void setAction(String iActionName, SchemaAction iAction) {
		if (actions.containsKey(iActionName)) {
			// REPLACE IT
			for (int i = 0; i < orderedActions.size(); ++i) {
				if (orderedActions.get(i).getName() != null && orderedActions.get(i).getName().equals(iActionName)) {
					orderedActions.remove(i);
					break;
				}
			}
		}

		orderedActions.add(iAction);
		actions.put(iActionName, iAction);
	}

	public void setEvent(String iEventName, SchemaEvent iEvent) {
		events.put(iEventName, iEvent);
	}

	public void setOrderFields(List<SchemaField> iFields) {
		orderedFields = iFields;
	}

	public void setOrderActions(List<SchemaAction> iActions) {
		orderedActions = iActions;
	}

	public Map<String, SchemaField> getFields() {
		return fields;
	}

	public Map<String, SchemaAction> getActions() {
		return actions;
	}

	/**
	 * COPY ALL EVENTS FROM PARENT ENTITY
	 * 
	 * @param iSource
	 * @param listeners
	 * @throws CloneNotSupportedException
	 */
	protected void cloneEvents(SchemaClassDefinition iSource, List<UserObjectPermissionListener> listeners) throws CloneNotSupportedException {

		boolean allowed;
		SchemaEvent event;
		SchemaEvent sourceSchemaEvent;
		for (Iterator<SchemaEvent> it = iSource.getEventIterator(); it.hasNext();) {
			sourceSchemaEvent = it.next();

			// CHECK IF ALLOWED
			if (listeners != null) {
				synchronized (listeners) {
					allowed = true;
					for (UserObjectPermissionListener listener : listeners) {
						if (!listener.allowEvent(sourceSchemaEvent)) {
							// NOT ALLOWED
							allowed = false;
							break;
						}
					}
				}

				if (!allowed)
					continue;
			}

			event = (SchemaEvent) sourceSchemaEvent.clone();
			event.entity = this;
			setEvent(event.getName(), event);
		}
	}

	/**
	 * COPY ALL ACTIONS FROM PARENT ENTITY
	 * 
	 * @param iSource
	 * @param listeners
	 * @throws CloneNotSupportedException
	 */
	protected void cloneActions(SchemaClassDefinition iSource, List<UserObjectPermissionListener> listeners) throws CloneNotSupportedException {

		boolean allowed;
		SchemaAction action;
		SchemaAction sourceSchemaAction;
		for (Iterator<SchemaAction> it = iSource.getActionIterator(); it.hasNext();) {
			sourceSchemaAction = it.next();

			// CHECK IF ALLOWED
			if (listeners != null) {
				synchronized (listeners) {
					allowed = true;
					for (UserObjectPermissionListener listener : listeners) {
						if (!listener.allowAction(sourceSchemaAction)) {
							// NOT ALLOWED
							allowed = false;
							break;
						}
					}
				}

				if (!allowed)
					continue;
			}

			action = (SchemaAction) sourceSchemaAction.clone();
			action.entity = this;
			setAction(action.getName(), action);
		}
	}

	/**
	 * COPY ALL FIELDS FROM PARENT ENTITY
	 * 
	 * @param iSource
	 * @param listeners
	 * @throws CloneNotSupportedException
	 */
	protected void cloneFields(SchemaClassDefinition iSource, List<UserObjectPermissionListener> listeners) throws CloneNotSupportedException {

		boolean allowed;
		SchemaField field;
		SchemaField sourceSchemaField;
		for (Iterator<SchemaField> it = iSource.getFieldIterator(); it.hasNext();) {
			sourceSchemaField = it.next();

			// CHECK IF ALLOWED
			if (listeners != null) {
				synchronized (listeners) {
					allowed = true;
					for (UserObjectPermissionListener listener : listeners) {
						if (!listener.allowField(sourceSchemaField)) {
							// NOT ALLOWED
							allowed = false;
							break;
						}
					}
				}

				if (!allowed)
					continue;
			}

			field = (SchemaField) sourceSchemaField.clone();
			field.entity = this;
			setField(field.getName(), field);
		}
	}
}
