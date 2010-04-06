package sf.eclipse.javacc;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin.<br>
 * Referenced by plugin.xml<br>
 * Bundle-Activator: sf.eclipse.javacc.Activator<br>
 * Bundle-ClassPath: plugin.jar, javacc.jar
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class Activator extends AbstractUIPlugin implements IJJConstants {

  // MMa 02/2010 : formatting and javadoc revision

  /** The shared instance */
  private static Activator      fPlugin;
  /** The resource bundle 'messages_fr.properties' inside the jar */
  private static ResourceBundle fBundle;
  /** The console for JavaCC output */
  static JJConsole              fConsole;

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
   * Show and return the output console.
   * 
   * @return the console
   */
  public static JJConsole getConsole() {
    final IWorkbench workbench = PlatformUI.getWorkbench();

    // access only in the event thread, so create the event thread if not within it
    if (Thread.currentThread() != workbench.getDisplay().getThread()) {
      Display.getDefault().syncExec(new Runnable() {

        public void run() {
          fConsole = getConsole();
        }
      });
    }

    // here we are in the event thread 
    else {
      final IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
      if (windows.length == 0) {
        return null;
      }
      final IWorkbenchPage page = windows[0].getActivePage();

      fConsole = (JJConsole) page.findView(CONSOLE_ID);
      if (fConsole == null) {
        // if Console is not up, sShow it !  (console is doing error reporting, and must be up)
        try {
          page.showView(CONSOLE_ID);
        } catch (final PartInitException e) {
          e.printStackTrace();
        }
        fConsole = (JJConsole) page.findView(CONSOLE_ID);
      }
    }
    return fConsole;
  }

  /**
   * @param path the images path
   * @return the image descriptor
   */
  public static ImageDescriptor getImageDescriptor(final String path) {
    return AbstractUIPlugin.imageDescriptorFromPlugin("sf.eclipse.javacc", "icons/" + path); //$NON-NLS-1$ //$NON-NLS-2$
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
