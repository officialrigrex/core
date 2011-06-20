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

package org.romaframework.aspect.i18n;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.romaframework.aspect.i18n.feature.I18nFieldFeatures;
import org.romaframework.aspect.session.SessionAspect;
import org.romaframework.core.GlobalConstants;
import org.romaframework.core.Roma;
import org.romaframework.core.Utility;
import org.romaframework.core.flow.Controller;
import org.romaframework.core.flow.SchemaFieldListener;
import org.romaframework.core.module.SelfRegistrantConfigurableModule;
import org.romaframework.core.schema.SchemaAction;
import org.romaframework.core.schema.SchemaClass;
import org.romaframework.core.schema.SchemaClassDefinition;
import org.romaframework.core.schema.SchemaEvent;
import org.romaframework.core.schema.SchemaField;
import org.romaframework.core.schema.SchemaObject;
import org.romaframework.core.util.parser.ObjectVariableResolver;

/**
 * I18N Aspect abstract implementation handle the aspect configuration. Extend this if you want to write your own Aspect
 * implementation.
 * 
 * @author Luca Garulli (luca.garulli--at--assetdata.it)
 */
public abstract class I18NAspectAbstract extends SelfRegistrantConfigurableModule<String> implements I18NAspect, SchemaFieldListener {

	private static final String	DATE_TIME_FORMAT_VAR	= "Application.DateTimeFormat";
	private static final String	TIME_FORMAT_VAR				= "Application.TimeFormat";
	private static final String	DATE_FORMAT_VAR				= "Application.DateFormat";
	private static final String	NUMBER_FORMAT_VAR			= "Application.NumberFormat";
	public static final String	LABEL_NAME						= "label";

	public I18NAspectAbstract() {
		Controller.getInstance().registerListener(SchemaFieldListener.class, this);
	}

	public void beginConfigClass(SchemaClassDefinition iClass) {
	}

	public void endConfigClass(SchemaClassDefinition iClass) {
	}

	public void configField(SchemaField iField) {

		setFieldDefaults(iField);
	}

	private void setFieldDefaults(SchemaField field) {
	}

	public Object onAfterFieldRead(Object iContent, SchemaField iField, Object iCurrentValue) {
		if (iContent == null || iCurrentValue == null) {
			return null;
		}

		String key = (String) iField.getFeature(I18nFieldFeatures.KEY);

		if (key == null) {
			return iCurrentValue;
		}

		StringBuilder keyToSearch = new StringBuilder(VARNAME_PREFIX);

		int pos = key.indexOf(I18nFieldFeatures.CONTENT_VAR);
		if (pos == -1) {
			keyToSearch.append(key);
		} else {
			// REPLACE CONTENT_VAR WITH REAL VALUE
			keyToSearch.append(key.substring(0, pos));
			keyToSearch.append(iCurrentValue.toString());
			keyToSearch.append(key.substring(pos + I18nFieldFeatures.CONTENT_VAR.length()));
		}

		Object ret = resolveString(Roma.schema().getSchemaClass(iContent), keyToSearch.toString());
		if (ret != null) {
			return ret;
		} else {
			return iCurrentValue;
		}
	}

	/**
	 * Find the label walking on entity inheritance tree.
	 * 
	 * @param resource
	 * @param iObject
	 * @param iElementName
	 * @return
	 */
	public String getLabel(SchemaObject iObject, String iElementName, String iElementLabel) {
		if (iElementLabel != null) {
			// CUSTOM LABEL FOUND: RETURN
			return resolveString(iObject, iElementLabel);
		}

		StringBuilder nameToSearch = new StringBuilder();
		String label = null;
		SchemaClass clazz = iObject.getSchemaClass();

		while (clazz != null) {
			// COMPOSE THE NAME TO SEARCH
			nameToSearch.setLength(0);
			nameToSearch.append(clazz.getSchemaClass().getName());
			if (iElementName != null) {
				nameToSearch.append(CONTEXT_SEPARATOR).append(iElementName);
			}
			nameToSearch.append(CONTEXT_SEPARATOR).append(LABEL_NAME);

			label = getString(nameToSearch.toString());
			if (label != null) {
				break;
			}

			clazz = clazz.getSchemaClass().getSuperClass();
		}

		if (label == null) {
			// AUTO COMPOSE LABEL IF NOT EXISTS
			label = Utility.getClearName(iElementName != null ? iElementName : iObject.getSchemaClass().getName());
		}

		return label;
	}

	public Locale getLocale() {
		Locale loc = null;
		SessionAspect sess = Roma.aspect(SessionAspect.ASPECT_NAME);
		if (sess != null)
			loc = sess.getActiveLocale();

		if (loc == null)
			loc = Locale.getDefault();
		return loc;
	}

	public String getString(String iText) {
		return getString(iText, getLocale());
	}

	/**
	 * Resolve a string using the I18N. If the string starts with $ prefix, then search that name in the resource bundle configured,
	 * otherwise returns the same string passed in input.
	 * 
	 * @param iObjectClass
	 *          The class of object for tree search
	 * @param iText
	 *          The string to analyze and search
	 * @return The I18N string if it starts with $ prefix
	 */
	public String resolveString(SchemaClassDefinition iObjectClass, String iText, Object... iArgs) {
		if (!iText.startsWith(VARNAME_PREFIX)) {
			return iText;
		}

		String result = null;

		String varName = iText.substring(VARNAME_PREFIX.length());

		StringBuilder buffer = new StringBuilder();
		SchemaClass currentClass = iObjectClass != null ? iObjectClass.getSchemaClass() : null;

		// SEARCH BROWSING ALL CLASS TREE UNTIL OBJECT CLASS
		while (currentClass != null) {
			buffer.setLength(0);
			buffer.append(VARNAME_PREFIX);
			buffer.append(currentClass.getName());
			buffer.append(CONTEXT_SEPARATOR);
			buffer.append(varName);

			result = resolveString(buffer.toString(), iArgs);
			if (result != null) {
				return result;
			}

			currentClass = currentClass.getSuperClass();
		}

		// NOT FOUND, TRY WITH DEFAULT
		result = resolveString(VARNAME_PREFIX + GlobalConstants.ROOT_CLASS + Utility.PACKAGE_SEPARATOR + varName, iArgs);

		return result;
	}

	/**
	 * Resolve a string using the I18N. If the string starts with $ prefix, then search that name in the resource bundle configured,
	 * otherwise returns the same string passed in input.
	 * 
	 * @param iText
	 *          The string to analyze and search
	 * @return The I18N string if it starts with $ prefix, otherwise iLabel
	 */
	public String resolveString(String iText, Object... iArgs) {
		if (iText != null && iText.startsWith(VARNAME_PREFIX)) {
			// VARNAME_PREFIX FOUND: RESOLVE THE LABEL WITH I18N SETTINGS
			iText = getString(iText.substring(VARNAME_PREFIX.length()));
			if (iText != null && iArgs != null && iArgs.length > 0)
				iText = new ObjectVariableResolver(iText).resolveVariables(iArgs);
		}
		return iText;
	}

	public Object onBeforeFieldRead(Object content, SchemaField field, Object currentValue) {
		return IGNORED;
	}

	public Object onBeforeFieldWrite(Object content, SchemaField field, Object currentValue) {
		return currentValue;
	}

	public Object onAfterFieldWrite(Object content, SchemaField field, Object currentValue) {
		return currentValue;
	}

	public void configAction(SchemaAction action) {
	}

	public void configClass(SchemaClassDefinition class1) {
	}

	public void configEvent(SchemaEvent event) {
	}

	public Object getUnderlyingComponent() {
		return null;
	}

	public DateFormat getDateTimeFormat() {
		return getDateTimeFormat(getLocale());
	}

	public DateFormat getDateTimeFormat(Locale iLocale) {
		if (iLocale == null) {
			iLocale = getLocale();
		}
		String format = getString(DATE_TIME_FORMAT_VAR, iLocale);
		if (format == null)
			format = "dd/MM/yyyy HH:mm:ss";
		return new SimpleDateFormat(format);
	}

	public DateFormat getDateFormat() {
		return getDateFormat(getLocale());
	}

	public DateFormat getDateFormat(Locale iLocale) {
		if (iLocale == null) {
			iLocale = getLocale();
		}
		String format = getString(DATE_FORMAT_VAR, iLocale);
		if (format == null)
			format = "dd/MM/yyyy";
		return new SimpleDateFormat(format, iLocale);
	}

	public DateFormat getTimeFormat() {
		return getTimeFormat(getLocale());
	}

	public DateFormat getTimeFormat(Locale iLocale) {
		if (iLocale == null) {
			iLocale = getLocale();
		}
		String format = getString(TIME_FORMAT_VAR, iLocale);
		if (format == null)
			format = "HH:mm:ss";
		return new SimpleDateFormat(format, iLocale);

	}

	public NumberFormat getNumberFormat() {
		return getNumberFormat(getLocale());
	}

	public NumberFormat getNumberFormat(Locale iLocale) {
		if (iLocale == null) {
			iLocale = getLocale();
		}
		String format = getString(NUMBER_FORMAT_VAR, iLocale);
		if (format == null)
			format = "###,###,###.#####";
		return new DecimalFormat(format, new DecimalFormatSymbols(iLocale));
	}

	public String resolveString(Class<?> iObjectClass, String iText, Object... iArgs) {
		return resolveString(Roma.schema().getSchemaClass(iObjectClass), iText, iArgs);
	}

	public String aspectName() {
		return ASPECT_NAME;
	}
}
