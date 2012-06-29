package sf.eclipse.javacc.head;

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

import sf.eclipse.javacc.base.IJJConsole;
import sf.eclipse.javacc.base.IJJConstants;

/**
 * The main plugin for normal usage (ie non headless builds).<br>
 * Referenced by plugin.xml<br>
 * Bundle-Activator: sf.eclipse.javacc.head.Activator<br>
 * Bundle-ClassPath: plugin.jar, javacc.jar
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012
 */
public class Activator extends AbstractUIPlugin implements IJJConstants {

  // MMa 02/2010 : formatting and javadoc revision

  /** The shared instance */
  private static Activator      sActivator;
  /** The resource bundle 'messages_fr.properties' inside the jar */
  private static ResourceBundle sBundle;
  /** The console for JavaCC output */
  static IJJConsole             sJJConsole;

  /**
   * Standard constructor.
   */
  public Activator() {
    sActivator = this;
  }

  /**
   * @return the shared instance
   */
  public static Activator getDefault() {
    return sActivator;
  }

  /**
   * Called upon plug-in activation.
   */
  @Override
  public void start(final BundleContext aCtx) throws Exception {
    super.start(aCtx);

    // load bundle messages.properties 
    try {
      sBundle = ResourceBundle.getBundle("messages"); //$NON-NLS-1$
    } catch (final MissingResourceException x) {
      sBundle = null;
    }
  }

  /**
   * Called upon plug-in termination.
   */
  @Override
  public void stop(final BundleContext aCtx) throws Exception {
    super.stop(aCtx);
    sActivator = null;
  }

  /**
   * Show and return the output console.
   * 
   * @return the console
   */
  public static IJJConsole getConsole() {
    final IWorkbench workbench = PlatformUI.getWorkbench();

    // access only in the event thread, so create the event thread if not within it
    if (Thread.currentThread() != workbench.getDisplay().getThread()) {
      Display.getDefault().syncExec(new Runnable() {

        @Override
        public void run() {
          sJJConsole = getConsole();
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

      sJJConsole = (JJConsoleView) page.findView(CONSOLE_ID);
      if (sJJConsole == null) {
        // if Console is not up, show it !  (console is doing error reporting, and must be up)
        try {
          page.showView(CONSOLE_ID);
        } catch (final PartInitException e) {
          e.printStackTrace();
        }
        sJJConsole = (JJConsoleView) page.findView(CONSOLE_ID);
      }
    }
    return sJJConsole;
  }

  /**
   * @param aPath - the images path
   * @return the image descriptor
   */
  public static ImageDescriptor getImageDescriptor(final String aPath) {
    return AbstractUIPlugin.imageDescriptorFromPlugin("sf.eclipse.javacc", "icons/" + aPath); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * @param aKey - the key of a resource in the plugin's resource bundle
   * @return the string from the plugin's resource bundle, or 'key' if not found.
   */
  public static String getString(final String aKey) {
    try {
      return (sBundle != null) ? sBundle.getString(aKey) : aKey;
    } catch (final MissingResourceException e) {
      return aKey;
    }
  }

  /**
   * @param aMsg - the message to log as an error (no exception thrown to avoid side effects)
   */
  public static void logErr(final String aMsg) {
    final Status status = new Status(IStatus.ERROR, "JavaCC", IStatus.OK, aMsg, //$NON-NLS-1$
                                     //                                     new Exception("For information only")); //$NON-NLS-1$
                                     null);
    getDefault().getLog().log(status);
  }

  /**
   * @param aMsg - the message to log as an info
   */
  public static void logInfo(final String aMsg) {
    final Status status = new Status(IStatus.INFO, "JavaCC", IStatus.OK, aMsg, //$NON-NLS-1$
                                     null);
    getDefault().getLog().log(status);
  }

  /**
   * @return the resource bundle
   */
  public ResourceBundle getResourceBundle() {
    return sBundle;
  }
}
