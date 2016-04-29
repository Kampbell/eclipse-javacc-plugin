package sf.eclipse.javacc.base;

import static sf.eclipse.javacc.base.IConstants.*;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.IValueVariableInitializer;
import org.eclipse.core.variables.VariablesPlugin;
import org.osgi.framework.Version;

/**
 * The Value Variables Initializer is used for plugin and default jars names and versions:
 * <ul>
 * <li>{@link IConstants#PLUGIN_VERSION_VV}
 * <li>{@link IConstants#PLUGIN_LOCATION_VV}
 * <li>{@link IConstants#DEF_JAVACC_JAR_NAME_VV}
 * <li>{@link IConstants#DEF_JTB_JAR_NAME_VV}
 * <li>{@link IConstants#DEF_JAVACC_JAR_VERSION_VV}
 * <li>{@link IConstants#DEF_JTB_JAR_VERSION_VV}
 * </ul>
 * Referenced by plugin.xml<br>
 * 
 * @author Marc Mazas 2016
 */
public class ValueVariableInitializer implements IValueVariableInitializer {

  /** {@inheritDoc} */
  @Override
  public void initialize(final IValueVariable aValVar) {
    final String vvName = aValVar.getName();
    if (PLUGIN_VERSION_VV.equals(vvName)) {
      final Version ver = PIB.getVersion();
      aValVar.setValue(ver.toString());
    }
    else if (PLUGIN_LOCATION_VV.equals(vvName)) {
      final String loc = getBundleDir();
      aValVar.setValue(loc);
    }
    else if (PLUGIN_PATH_VV.equals(vvName)) {
      String loc = getBundleDir();
      final String ehv = "${eclipse_home}"; //$NON-NLS-1$
      String ehl = ""; //$NON-NLS-1$
      try {
        ehl = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(ehv, true);
        if ("\\".equals(FS)) { //$NON-NLS-1$
          ehl = ehl.replace(FS, "/"); //$NON-NLS-1$
        }
        if (loc.startsWith(ehl)) {
          loc = loc.substring(ehl.length());
          aValVar.setValue(loc);
        }
        else {
          AbstractActivator.logInfo("Unable to find for the bundle (" + loc //$NON-NLS-1$
                                    + ") a relative path to eclipse home (" + ehl + ")"); //$NON-NLS-1$ //$NON-NLS-2$
          aValVar.setValue("Not yet implemented - development case - user writable"); //$NON-NLS-1$
        }
      } catch (final CoreException e) {
        AbstractActivator.logBug(e, "Problem substituting variable in " + ehv); //$NON-NLS-1$
        aValVar.setValue("Unhandled error - user writable"); //$NON-NLS-1$
      }
    }
    else if (DEF_JAVACC_JAR_NAME_VV.equals(vvName)) {
      aValVar.setValue(DEF_JAVACC_JAR_NAME);
    }
    else if (DEF_JTB_JAR_NAME_VV.equals(vvName)) {
      aValVar.setValue(DEF_JTB_JAR_NAME);
    }
    else if (DEF_JAVACC_JAR_VERSION_VV.equals(vvName)) {
      aValVar.setValue(DEF_JAVACC_JAR_VERSION);
    }
    else if (DEF_JTB_JAR_VERSION_VV.equals(vvName)) {
      aValVar.setValue(DEF_JTB_JAR_VERSION);
    }
  }

  /**
   * @return the full path to the bundle directory or null in case of error
   */
  public static String getBundleDir() {
    String dir = null;
    try {
      final URL installURL = AbstractActivator.getDefault().getBundle().getEntry("/"); //$NON-NLS-1$
      final URL resolvedURL = FileLocator.resolve(installURL);
      dir = FileLocator.toFileURL(resolvedURL).getFile();
      // returned String is like "/C:/workspace/sf.eclipse.javacc/"
      if (dir.startsWith("/") && dir.startsWith(":", 2)) { //$NON-NLS-1$ //$NON-NLS-2$
        dir = dir.substring(1);
      }
    } catch (final IOException e) {
      AbstractActivator.logBug(e, "Unable to find the bundle directory"); //$NON-NLS-1$
    }
    return dir;
  }

}
