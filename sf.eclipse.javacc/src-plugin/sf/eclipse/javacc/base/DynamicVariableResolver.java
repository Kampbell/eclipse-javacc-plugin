package sf.eclipse.javacc.base;

import static sf.eclipse.javacc.base.IConstants.*;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;

/**
 * The Dynamic Variable Resolver is used for jars paths substitution:
 * <ul>
 * <li>{@link IConstants#DEF_JAVACC_JAR_PATH_VV}
 * <li>{@link IConstants#DEF_JTB_JAR_PATH_VV}
 * <li>{@link IConstants#PROJ_JAVACC_JAR_PATH_DV}
 * <li>{@link IConstants#PROJ_JTB_JAR_PATH_DV}
 * </ul>
 * Referenced by plugin.xml<br>
 * 
 * @author Marc Mazas 2016
 */
public class DynamicVariableResolver implements IDynamicVariableResolver {

  /** {@inheritDoc} */
  @Override
  public String resolveValue(final IDynamicVariable aDynVar, final String aArg) {

    final String dvName = aDynVar.getName();

    if (DEF_JAVACC_JAR_PATH_VV.equals(dvName)) {
      return Compiler.getDefaultJarFile("jj"); //$NON-NLS-1$
    }

    if (DEF_JTB_JAR_PATH_VV.equals(dvName)) {
      return Compiler.getDefaultJarFile("jtb"); //$NON-NLS-1$
    }

    final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(aArg);
    if (project == null) {
      AbstractActivator.logErr("Unknown project " + aArg); //$NON-NLS-1$
      return ""; //$NON-NLS-1$
    }

    final IEclipsePreferences prefs = new ProjectScope(project).getNode(PLUGIN_QN);
    if (PROJ_JAVACC_JAR_PATH_DV.equals(dvName)) {
      final String jar = prefs.get(RUNTIME_JJJAR, ""); //$NON-NLS-1$
      return jar;
    }

    if (PROJ_JTB_JAR_PATH_DV.equals(dvName)) {
      final String jar = prefs.get(RUNTIME_JTBJAR, ""); //$NON-NLS-1$
      return jar;
    }

    AbstractActivator.logErr("Unknown dynamic variable " + dvName + " / " //$NON-NLS-1$ //$NON-NLS-2$
                             + aDynVar.getDescription());
    return null;
  }

}
