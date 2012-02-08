package org.romaframework.core.component;

import java.util.Map;

import org.romaframework.core.Utility;
import org.romaframework.core.config.AbstractServiceable;
import org.romaframework.core.config.ContextException;
import org.romaframework.core.schema.SchemaAction;
import org.romaframework.core.schema.SchemaClassDefinition;
import org.romaframework.core.schema.SchemaEvent;
import org.romaframework.core.schema.SchemaField;
import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@SuppressWarnings("unchecked")
public class SpringComponentEngine extends AbstractServiceable implements ComponentAspect {
	private static final String								COMPONENT_SRV_FILE_PATTERN				= "META-INF/components/applicationContext*.xml";
	private static final String								COMPONENT_SRV_FILE_PATTERN_SUBDIR	= "META-INF/components/*/applicationContext*.xml";

	protected ClassPathXmlApplicationContext	springContext;
	protected String													basePath;
	protected String[]												additionalPaths;

	public boolean existComponent(Class<? extends Object> iComponentClass) {
		return springContext.containsBean(Utility.getClassName(iComponentClass));
	}

	public boolean existComponent(String iComponentName) {
		return springContext.containsBean(iComponentName);
	}

	public <T> T getComponent(Class<T> iComponentClass) throws ContextException {
		try {
			return (T) springContext.getBean(Utility.getClassName(iComponentClass), iComponentClass);
		} catch (BeansException e) {
			throw new ContextException("Error on retrieving component '" + Utility.getClassName(iComponentClass) + "'", e);
		}
	}

	public <T> T getComponent(String iComponentName) throws ContextException {
		try {
			return (T) springContext.getBean(iComponentName);
		} catch (BeansException e) {
			throw new ContextException("Error on retrieving component '" + iComponentName + "'", e);
		}
	}

	public <T> Map<String, T> getComponentsOfClass(Class<T> iComponentClass) throws ContextException {
		try {
			return springContext.getBeansOfType(iComponentClass);
		} catch (BeansException e) {
			throw new ContextException("Error on retrieving components of class '" + iComponentClass + "'", e);
		}
	}

	/**
	 * Load all Spring configuration files and start the context.
	 */
	public void startup() throws RuntimeException {
		status = STATUS_STARTING;
		int size = 2;
		if (additionalPaths != null)
			size += additionalPaths.length;
		String paths[] = new String[size];
		if (basePath != null) {
			paths[0] = basePath + COMPONENT_SRV_FILE_PATTERN;
			paths[1] = basePath + COMPONENT_SRV_FILE_PATTERN_SUBDIR;
		} else {
			paths[0] = COMPONENT_SRV_FILE_PATTERN;
			paths[1] = COMPONENT_SRV_FILE_PATTERN_SUBDIR;
		}
		if (additionalPaths != null)
			System.arraycopy(additionalPaths, 0, paths, 2, additionalPaths.length);

		springContext = new ClassPathXmlApplicationContext(paths, false);

		springContext.refresh();
		status = STATUS_UP;
	}

	public void shutdown() throws RuntimeException {
		status = STATUS_SHUTDOWNING;

		if (springContext != null) {
			springContext.destroy();
		}

		status = STATUS_DOWN;
	}

	public String aspectName() {
		return ASPECT_NAME;
	}

	public void beginConfigClass(SchemaClassDefinition iClass) {
	}

	public void endConfigClass(SchemaClassDefinition iClass) {
	}

	public void configAction(SchemaAction action) {
	}

	public void configClass(SchemaClassDefinition class1) {
	}

	public void configEvent(SchemaEvent event) {
	}

	public void configField(SchemaField field) {
	}

	public Object getUnderlyingComponent() {
		return springContext;
	}

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public void setAdditionalPaths(String[] additionalApplicationContext) {
		this.additionalPaths = additionalApplicationContext;
	}
}
