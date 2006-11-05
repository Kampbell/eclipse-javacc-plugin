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
 * The main plugin.
 * Referenced by plugin.xml
 *  Bundle-Activator: sf.eclipse.javacc.Activator
 *  Bundle-ClassPath: plugin.jar, javacc.jar
 * 
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */
public class Activator extends AbstractUIPlugin implements IJJConstants {
  // The shared instance.
  private static Activator plugin;
  // The resource bundle 'messages_fr.properties' inside the jar
  private static ResourceBundle bundle;
  
  /**
   * The constructor
   */
  public Activator() {
    plugin = this;
  }
  
  /**
   * Returns the shared instance.
   */
  public static Activator getDefault() {
    return plugin;
  }

  /**
   * This method is called upon plug-in activation
   */
  public void start(BundleContext context) throws Exception {
    super.start(context);
    
    // Load bundle messages.properties 
    try {
      bundle = ResourceBundle.getBundle("messages"); //$NON-NLS-1$
    } catch (MissingResourceException x) {
      bundle = null;
    }
  }

  /**
   * This method is called when the plug-in is stopped
   */
  public void stop(BundleContext context) throws Exception {
    super.stop(context);
    plugin = null;
  }

  /**
   * Returns Console for JavaCC output.
   */
  static JJConsole console;
  public static JJConsole getConsole() {
    IWorkbench workbench = PlatformUI.getWorkbench();

    // Access only in the event thread
    if (Thread.currentThread() != workbench.getDisplay().getThread()) {
      Display.getDefault().syncExec(new Runnable() {
        public void run() {
          console = getConsole();
        }
      });
    }
    
    // Here we are in the event thread 
    else {
      IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
      if (windows.length == 0) // This happens when dispose
        return null;
      IWorkbenchPage page = windows[0].getActivePage();
      
      console = (JJConsole) page.findView(CONSOLE_ID);
      if (console == null) {
        // if Console is not up Show it !
        // Console is doing error reporting, and must be up.
        try {
          page.showView(CONSOLE_ID);
        } catch (PartInitException e) {
          e.printStackTrace();
        }
        console = (JJConsole) page.findView(CONSOLE_ID);
      }
    }
    return console;
  }

  /**
   * Returns image descriptor
   */
  public static ImageDescriptor getImageDescriptor(String path) {
    return AbstractUIPlugin.imageDescriptorFromPlugin("sf.eclipse.javacc", "icons/"+path); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * Returns the string from the plugin's resource bundle, or 'key' if not
   * found.
   */
  public static String getString(String key) {
    try {
      return (bundle != null) ? bundle.getString(key) : key;
    } catch (MissingResourceException e) {
      return key;
    }
  }

  public static void log(String msg) {
    Status status = new Status(IStatus.INFO, "JavaCC", IStatus.OK, msg, //$NON-NLS-1$
        new Exception("For information only")); //$NON-NLS-1$
    getDefault().getLog().log(status);
  }

  public ResourceBundle getResourceBundle() {
    return bundle;
  }
}
