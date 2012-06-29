package sf.eclipse.javacc.headless;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import sf.eclipse.javacc.base.IJJConstants;

/**
 * The Project Nature for JavaCC projects for headless builds.<br>
 * Referenced by plugin.xml<br>
 * <extension point="org.eclipse.core.resources.natures">
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012
 */
public class JJNature implements IProjectNature, IJJConstants {

  // MMa 11/2009 : javadoc and formatting revision
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 03/2010 : some renamings ; removed reapplying the nature

  /** The project */
  private IProject fProject;

  /**
   * Returns the project.
   * 
   * @see IProjectNature#getProject()
   */
  @Override
  public IProject getProject() {
    return fProject;
  }

  /**
   * Sets the project.
   * 
   * @see IProjectNature#setProject(IProject)
   */
  @Override
  public void setProject(final IProject aProject) {
    fProject = aProject;
  }

  /**
   * Configures this nature for its project. This is called by the workspace when natures are added to the project using
   * <code>IProject.setDescription</code> and should not be called directly by clients.<br>
   * The nature extension id is added to the list of natures before this method is called, and need not be added here.
   * 
   * @see IProjectNature#configure()
   */
  @Override
  public void configure() throws CoreException {
    final IProjectDescription desc = fProject.getDescription();
    final ICommand[] cmds = desc.getBuildSpec();
    ICommand command = null;
    for (int i = cmds.length - 1; i >= 0; i--) {
      if (cmds[i].getBuilderName().equals(JJ_BUILDER_ID)) {
        command = cmds[i];
        break;
      }
    }
    if (command == null) {
      // add JJBuilder (ID only)
      command = desc.newCommand();
      command.setBuilderName(JJ_BUILDER_ID);
      final ICommand[] newCommands = new ICommand[cmds.length + 1];
      newCommands[0] = command;
      System.arraycopy(cmds, 0, newCommands, 1, cmds.length);
      desc.setBuildSpec(newCommands);
      desc.setComment(Activator.getString("JJNature.JavaCC_nature_description")); //$NON-NLS-1$
      fProject.setDescription(desc, null);
    }
  }

  /**
   * De-configures this nature for its project. This is called by the workspace when natures are removed from the project using
   * <code>IProject.setDescription</code> and should not be called directly by clients.<br>
   * The nature extension id is removed from the list of natures before this method is called, and need not be removed here.
   * 
   * @see IProjectNature#deconfigure()
   */
  @Override
  public void deconfigure() throws CoreException {
    final IProjectDescription desc = fProject.getDescription();
    final ICommand[] cmds = desc.getBuildSpec();
    for (int i = cmds.length - 1; i >= 0; i--) {
      if (cmds[i].getBuilderName().equals(JJ_BUILDER_ID)) {
        // copy without JJBuilder
        final ICommand[] newCommands = new ICommand[cmds.length - 1];
        System.arraycopy(cmds, 0, newCommands, 0, i);
        System.arraycopy(cmds, i + 1, newCommands, i, cmds.length - i - 1);
        desc.setBuildSpec(newCommands);
        fProject.setDescription(desc, null);
        break;
      }
    }
  }

  /**
   * Adds or removes a JavaCC Nature (nature id only) to the project.
   * 
   * @param aJJNature adds if true, removes if false
   * @param aProject to change
   */
  static public void setJJNature(final boolean aJJNature, final IProject aProject) {
    if (aProject == null) {
      Activator.logErr(Activator.getString("JJNature.Project_null")); //$NON-NLS-1$ 
      return;
    }
    try {
      final IProjectDescription desc = aProject.getDescription();
      final String[] natures = desc.getNatureIds();
      boolean found = false;
      // find whether nature already exists
      for (int i = 0; i < natures.length; ++i) {
        if (natures[i].equals(JJ_NATURE_ID)) {
          found = true;
          break;
        }
      }
      if (!found && aJJNature) {
        // add the nature to the project
        final String[] newNatures = new String[natures.length + 1];
        System.arraycopy(natures, 0, newNatures, 0, natures.length);
        newNatures[natures.length] = JJ_NATURE_ID;
        desc.setNatureIds(newNatures);
        aProject.setDescription(desc, null);
      }
      if (found) {
        if (!aJJNature) {
          // remove the nature from the project
          final String[] newNatures = new String[natures.length - 1];
          for (int i = natures.length - 1; i >= 0; i--) {
            if (natures[i].equals(JJ_NATURE_ID)) {
              // copy without JJNature
              System.arraycopy(natures, 0, newNatures, 0, i);
              System.arraycopy(natures, i + 1, newNatures, i, natures.length - i - 1);
              desc.setNatureIds(newNatures);
              aProject.setDescription(desc, null);
              break;
            }
          }
        }
      }
    } catch (final CoreException e) {
      Activator.logErr(Activator.getString("JJNature.Problem") + " : " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
      e.printStackTrace();
    }
  }
}
