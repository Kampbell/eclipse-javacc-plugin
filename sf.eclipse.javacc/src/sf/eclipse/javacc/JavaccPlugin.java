package sf.eclipse.javacc;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin.
 * 
 * @author Remi Koutcherawy 2003 
 * GPL Licence www.gnu.org/licenses/gpl.txt
 */
public class JavaccPlugin extends AbstractUIPlugin {
  // The shared instance.
  private static JavaccPlugin plugin;
  private ResourceBundle resourceBundle;

  /**
   * The constructor.
   */
  public JavaccPlugin() {
    super();
 	plugin = this;
	try {
		resourceBundle = ResourceBundle.getBundle("plugin");
	} catch (MissingResourceException x) {
		resourceBundle = null;
	}
  }

  /**
   * Returns the shared instance.
   */
  public static JavaccPlugin getDefault() {
    return plugin;
  }

  /**
   * Returns Console for JavaCC output.
   */
  public static JJConsole getConsole() {
    try {
      IWorkbench workbench = PlatformUI.getWorkbench();
      IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
      IWorkbenchPage page = windows[0].getActivePage();
      JJConsole console = (JJConsole) page.findView(JJConsole.CONSOLE_ID);
      return console;
    } catch (Throwable e) {
      // if Console doesn't exist, ignore
    }
    return null;
  }

  /**
   * Returns image descriptor
   */
  public ImageDescriptor getResourceImageDescriptor(String relativePath) {
    try {
      URL installURL= JavaccPlugin.getDefault().getBundle().getEntry("/icons/");
      URL url = new URL(installURL, relativePath);
      return ImageDescriptor.createFromURL(url);
    } catch (MalformedURLException e) {
      // should not happen
      return ImageDescriptor.getMissingImageDescriptor();
    }
  }

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = JavaccPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
}
