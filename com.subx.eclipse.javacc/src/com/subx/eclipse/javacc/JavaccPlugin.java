package com.subx.eclipse.javacc;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class to be used in the desktop.
 */
public class JavaccPlugin extends AbstractUIPlugin
{
	private static JavaccPlugin plugin;
	private ResourceBundle resourceBundle;

	/**
	 * The constructor.
	 */
	public JavaccPlugin(IPluginDescriptor descriptor)
	{
		super(descriptor);
		plugin = this;
		try
		{
			resourceBundle = ResourceBundle.getBundle("com.subx.eclipse.javacc.JavaccPluginResources");
		}
		catch (MissingResourceException x)
		{
			resourceBundle = null;
		}
	}

	/**
	 * Returns the shared instance.
	 */
	public static JavaccPlugin getDefault()
	{
		return plugin;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace()
	{
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not found.
	 */
	public static String getResourceString(String key)
	{
		ResourceBundle bundle = JavaccPlugin.getDefault().getResourceBundle();
		try
		{
			return bundle.getString(key);
		}
		catch (MissingResourceException e)
		{
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle()
	{
		return resourceBundle;
	}
}