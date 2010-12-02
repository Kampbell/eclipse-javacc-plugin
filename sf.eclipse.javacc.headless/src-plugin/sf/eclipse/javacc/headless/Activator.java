package sf.eclipse.javacc.headless;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

import sf.eclipse.javacc.base.IJJConsole;

/**
 * The main plugin for headless builds.<br>
 * Referenced by plugin.xml<br>
 * Bundle-Activator: sf.eclipse.javacc.headless.Activator<br>
 * Bundle-ClassPath: plugin.jar, javacc.jar
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class Activator extends Plugin {

  // MMa 02/2010 : formatting and javadoc revision

  /** The shared instance */
  private static Activator      fPlugin;
  /** The resource bundle 'messages_fr.properties' inside the jar */
  private static ResourceBundle fBundle;
  /** The console for JavaCC output */
  static IJJConsole             fConsole;

  /**
   * Standard constructor.
   */
  public Activator() {
    fPlugin = this;
  }

  /**
   * @return the shared instance
   */
  public static Activator getDefault() {
    return fPlugin;
  }

  /**
   * Called upon plug-in activation.
   */
  @Override
  public void start(final BundleContext context) throws Exception {
    super.start(context);

    // load bundle messages.properties 
    try {
      fBundle = ResourceBundle.getBundle("messages"); //$NON-NLS-1$
    } catch (final MissingResourceException x) {
      fBundle = null;
    }
  }

  /**
   * Called upon plug-in termination.
   */
  @Override
  public void stop(final BundleContext context) throws Exception {
    super.stop(context);
    fPlugin = null;
  }

  /**
   * Creates a new output console.
   * 
   * @return the console
   */
  public static IJJConsole getConsole() {
    return new BasicConsole();
  }

  /**
   * @param key the key of a resource in the plugin's resource bundle
   * @return the string from the plugin's resource bundle, or 'key' if not found.
   */
  public static String getString(final String key) {
    try {
      return (fBundle != null) ? fBundle.getString(key) : key;
    } catch (final MissingResourceException e) {
      return key;
    }
  }

  /**
   * @param msg the message to log as an error (no exception thrown to avoid side effects)
   */
  public static void logErr(final String msg) {
    final Status status = new Status(IStatus.ERROR, "JavaCC", IStatus.OK, msg, //$NON-NLS-1$
                                     //                                     new Exception("For information only")); //$NON-NLS-1$
                                     null);
    getDefault().getLog().log(status);
  }

  /**
   * @param msg the message to log as an info
   */
  public static void logInfo(final String msg) {
    final Status status = new Status(IStatus.INFO, "JavaCC", IStatus.OK, msg, //$NON-NLS-1$
                                     null);
    getDefault().getLog().log(status);
  }

  /**
   * @return the resource bundle
   */
  public ResourceBundle getResourceBundle() {
    return fBundle;
  }
}
