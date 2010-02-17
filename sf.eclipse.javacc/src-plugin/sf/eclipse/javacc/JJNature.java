package sf.eclipse.javacc;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 * The Project Nature for JavaCC projects.<br>
 * Referenced by plugin.xml<br>
 * <extension point="org.eclipse.core.resources.natures">
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class JJNature implements IProjectNature, IJJConstants {

  // MMa 11/2009 : javadoc and formatting revision
  // MMa 02/2010 : formatting and javadoc revision

  /** The project */
  private IProject fProject;

  /**
   * Returns the project.
   * 
   * @see IProjectNature#getProject()
   */
  public IProject getProject() {
    return fProject;
  }

  /**
   * Sets the project.
   * 
   * @see IProjectNature#setProject(IProject)
   */
  public void setProject(final IProject aProject) {
    fProject = aProject;
  }

  /**
   * Configures this nature for the project. Called by the workspace when nature is added to the project with
   * <code>IProject.setDescription</code>.
   * 
   * @see IProjectNature#configure()
   */
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
   * Deconfigures this nature for the project. Called by the workspace when nature is removed from the
   * project.
   * 
   * @see org.eclipse.core.resources.IProjectNature#deconfigure()
   */
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
   * Adds a JavaCC Nature to the project.
   * 
   * @param isJJNature adds if true, removes if false
   * @param project to change
   */
  static public void setJJNature(final boolean isJJNature, final IProject project) {
    if (project == null) {
      return;
    }
    try {
      final IProjectDescription desc = project.getDescription();
      final String[] natures = desc.getNatureIds();
      boolean found = false;
      for (int i = 0; i < natures.length; ++i) {
        if (natures[i].equals(JJ_NATURE_ID)) {
          found = true;
          break;
        }
      }
      if (!found && isJJNature) {
        // add nature to the project (ID only)
        final String[] newNatures = new String[natures.length + 1];
        System.arraycopy(natures, 0, newNatures, 0, natures.length);
        newNatures[natures.length] = JJ_NATURE_ID;
        desc.setNatureIds(newNatures);
        project.setDescription(desc, null);
      }
      if (found && !isJJNature) {
        // remove the nature
        final String[] newNatures = new String[natures.length - 1];
        for (int i = natures.length - 1; i >= 0; i--) {
          if (natures[i].equals(JJ_NATURE_ID)) {
            // copy without JJNature
            System.arraycopy(natures, 0, newNatures, 0, i);
            System.arraycopy(natures, i + 1, newNatures, i, natures.length - i - 1);
            desc.setNatureIds(newNatures);
            project.setDescription(desc, null);
            break;
          }
        }
      }
    } catch (final CoreException e) {
      e.printStackTrace();
    }
  }
}
