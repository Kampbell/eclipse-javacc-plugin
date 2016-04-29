package sf.eclipse.javacc.base;

import static sf.eclipse.javacc.base.IConstants.*;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin part, common for normal usage and headless builds (except for the
 * {@link #getImageDescriptor(String)} method which is specific to head builds).<br>
 * Referenced by plugin.xml<br>
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 */
public abstract class AbstractActivator extends AbstractUIPlugin {

  // MMa 02/2010 : formatting and javadoc revision
  // MMa 10/2012 : some renamings ; added ability to read the plugin properties
  // MMa 11/2014 : added logBug & logErr methods ; modified some modifiers
  // MMa 12/2014 : splitted Activator in an abstract part and a non abstract one

  /** The shared instance */
  private static AbstractActivator sActivator;
  /** The resource bundle 'messages.properties' inside the jar */
  private static ResourceBundle    sMsgBundle;
  /** The console for JavaCC output */
  public static IConsole           sConsole;
  /** The plugin version (as set in plugin.xml) */
  private static String            sJJVersion;
  /** The bug message */
  private static String            sBugMsg;

  /**
   * Standard constructor.
   */
  public AbstractActivator() {
    sActivator = this;
  }

  /**
   * @return the shared instance
   */
  public final static AbstractActivator getDefault() {
    return sActivator;
  }

  /**
   * Called upon plug-in activation.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public void start(final BundleContext aCtx) throws Exception {
    super.start(aCtx);
    // load bundle messages.properties 
    try {
      sMsgBundle = ResourceBundle.getBundle("messages"); //$NON-NLS-1$
    } catch (final MissingResourceException e) {
      logErr(e.getMessage());
    }
    sJJVersion = PIB.getHeaders().get("Bundle-Version"); //$NON-NLS-1$
    sBugMsg = getMsg("Bug.Msg"); //$NON-NLS-1$
  }

  /**
   * Called upon plug-in termination.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public void stop(final BundleContext aCtx) throws Exception {
    super.stop(aCtx);
    sActivator = null;
  }

  /**
   * Shows or creates the output console view. Specific to head and headless builds.
   * 
   * @return the console
   */
  public abstract IConsole getConsole();

  /**
   * Returns the image descriptor. Specific to head builds.
   * 
   * @param aPath - the image path under the icons directory
   * @return the image descriptor
   */
  public static ImageDescriptor getImageDescriptor(final String aPath) {
    return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_QN, ICONS_FOLDER + aPath);
  }

  /**
   * @param aKey - the key of a resource in the plugin's messages resource bundle
   * @return the string from the plugin's resource bundle, or 'key' if not found
   */
  public static String getMsg(final String aKey) {
    try {
      return (sMsgBundle != null) ? sMsgBundle.getString(aKey) : aKey;
    } catch (final MissingResourceException e) {
      return aKey;
    }
  }

  /**
   * @param aKey - the key of a resource in the plugin's plugin properties resource bundle (without the "%")
   * @return the string from the plugin's resource bundle, or 'key' if not found
   */
  public static String getProp(final String aKey) {
    return Platform.getResourceString(getDefault().getBundle(), "%" + aKey); //$NON-NLS-1$
  }

  /**
   * @param aTh - the exception for which the stacktrace is to be logged
   * @param aIntData - the context data to log at the end of the message
   */
  public static void logBug(final Throwable aTh, final int... aIntData) {
    logErr(sBugMsg, aTh, aIntData);
  }

  /**
   * @param aTh - the exception for which the stacktrace is to be logged
   * @param aStringData - the context data to log at the end of the message
   */
  public static void logBug(final Throwable aTh, final String... aStringData) {
    logErr(sBugMsg, aTh, aStringData);
  }

  /**
   * @param aTh - the exception for which the stacktrace is to be logged
   */
  public static void logBug(final Throwable aTh) {
    logErr(sBugMsg, aTh);
  }

  /**
   * @param aMsg - the message to log as an error
   * @param aTh - the exception for which the stacktrace is to be logged
   * @param aIntData - the context data to log at the end of the message
   */
  public static void logErr(final String aMsg, final Throwable aTh, final int... aIntData) {
    final Status status = new Status(IStatus.ERROR, PLUGIN_NAME, IStatus.OK, addNameVersionContext(aMsg,
                                                                                                   aIntData),
                                     aTh);
    getDefault().getLog().log(status);
  }

  /**
   * @param aMsg - the message to log as an error
   * @param aTh - the exception for which the stacktrace is to be logged
   * @param aStringData - the context data to log at the end of the message
   */
  public static void logErr(final String aMsg, final Throwable aTh, final String... aStringData) {
    final Status status = new Status(IStatus.ERROR, PLUGIN_NAME, IStatus.OK,
                                     addNameVersionContext(aMsg, aStringData), aTh);
    getDefault().getLog().log(status);
  }

  /**
   * @param aMsg - the message to log as an error
   * @param aTh - the exception for which the stacktrace is to be logged
   */
  public static void logErr(final String aMsg, final Throwable aTh) {
    final Status status = new Status(IStatus.ERROR, PLUGIN_NAME, IStatus.OK, addNameVersion(aMsg), aTh);
    getDefault().getLog().log(status);
  }

  /**
   * @param aMsg - the message to log as an error (no exception thrown to avoid side effects)
   */
  public static void logErr(final String aMsg) {
    logErr(aMsg, null);
  }

  /**
   * @param aMsg - the message to log as an error (no exception thrown to avoid side effects)
   * @param aIntData - the context data to log at the end of the message
   * @return one line with the message, one line with the plugin name and its version, one line with the
   *         context data
   */
  public static String addNameVersionContext(final String aMsg, final int... aIntData) {
    final StringBuffer sb = new StringBuffer(200);
    sb.append(aMsg).append(LS);
    sb.append(" Plugin ").append(PLUGIN_NAME).append(", version ").append(sJJVersion).append(LS); //$NON-NLS-1$  //$NON-NLS-2$
    if (aIntData != null) {
      sb.append(" context data : <"); //$NON-NLS-1$
      for (final int s : aIntData) {
        sb.append(s).append(">, <"); //$NON-NLS-1$
      }
      sb.setLength(sb.length() - 2);
    }
    return sb.toString();
  }

  /**
   * @param aMsg - the message to log as an error (no exception thrown to avoid side effects)
   * @param aStringData - the context data to log at the end of the message
   * @return one line with the message, one line with the plugin name and its version, one line with the
   *         context data
   */
  public static String addNameVersionContext(final String aMsg, final String... aStringData) {
    final StringBuffer sb = new StringBuffer(200);
    sb.append(aMsg).append(LS);
    sb.append(" Plugin ").append(PLUGIN_NAME).append(", version ").append(sJJVersion).append(LS); //$NON-NLS-1$  //$NON-NLS-2$
    if (aStringData != null) {
      sb.append(" context data : <"); //$NON-NLS-1$
      for (final String s : aStringData) {
        sb.append(s).append(">, <"); //$NON-NLS-1$
      }
      sb.setLength(sb.length() - 2);
      sb.append(">"); //$NON-NLS-1$
    }
    return sb.toString();
  }

  /**
   * @param aMsg - the message to log as an error (no exception thrown to avoid side effects)
   * @return one line with the message and one line with the plugin name and its version
   */
  public static String addNameVersion(final String aMsg) {
    final StringBuffer sb = new StringBuffer(140);
    sb.append(aMsg).append(LS);
    sb.append(" Plugin ").append(PLUGIN_NAME).append(", version ").append(sJJVersion).append(LS); //$NON-NLS-1$  //$NON-NLS-2$
    return sb.toString();
  }

  /**
   * @param aMsg - the message to log as an info
   */
  public static void logInfo(final String aMsg) {
    final Status status = new Status(IStatus.INFO, PLUGIN_NAME, IStatus.OK, aMsg, null);
    getDefault().getLog().log(status);
  }

}
