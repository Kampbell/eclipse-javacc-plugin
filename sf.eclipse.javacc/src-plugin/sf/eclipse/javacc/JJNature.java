package sf.eclipse.javacc;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 * The Project Nature for JavaCC projects
 * Referenced by plugin.xml
 *  <extension point="org.eclipse.core.resources.natures">
 *  
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */
public class JJNature implements IProjectNature, IJJConstants {
  private IProject project;

  /** 
   * Return the project.
   * @see org.eclipse.core.resources.IProjectNature#getProject()
   */
  public IProject getProject() {
    return project;
  }
  
  /** 
   * Set the project.
   * @see org.eclipse.core.resources.IProjectNature#setProject(IProject)
   */
  public void setProject(IProject project) {
    this.project = project;
  }
  
  /** 
   * Configure this nature for the project.
   * Called by the workspace when nature is added to the project
   *  with <code>IProject.setDescription</code>
   * @see org.eclipse.core.resources.IProjectNature#configure()
   */
  public void configure() throws CoreException {
    IProjectDescription desc = project.getDescription();
    ICommand[] cmds = desc.getBuildSpec();
    ICommand command = null;
    for (int i = cmds.length - 1; i >= 0; i--) {
      if (cmds[i].getBuilderName().equals(JJ_BUILDER_ID)) {
        command = cmds[i];
        break;
      }
    }
    if (command == null) {
      // Add JavaCC JJBuilder (ID only)
      command = desc.newCommand();
      command.setBuilderName(JJ_BUILDER_ID);
      ICommand[] newCommands = new ICommand[cmds.length + 1];
      newCommands[0] = command;
      System.arraycopy(cmds, 0, newCommands, 1, cmds.length);
      desc.setBuildSpec(newCommands);
      desc.setComment(Activator.getString("JJNature.JavaCC_nature_description")); //$NON-NLS-1$
      project.setDescription(desc, null);
    }
  }

  /** 
   * Deconfigure this nature for the project.
   * Called by the workspace when nature is removed from the project.
   * @see org.eclipse.core.resources.IProjectNature#deconfigure()
   */
  public void deconfigure() throws CoreException {
    IProjectDescription desc = project.getDescription();
    ICommand[] cmds = desc.getBuildSpec();
    for (int i = cmds.length - 1; i >= 0; i--) {
      if (cmds[i].getBuilderName().equals(JJ_BUILDER_ID)) {
        // Copy without JavaCC JJBuilder
        ICommand[] newCommands = new ICommand[cmds.length - 1];
        System.arraycopy(cmds, 0, newCommands, 0, i);
        System.arraycopy(cmds, i + 1, newCommands, i, cmds.length - i - 1);
        desc.setBuildSpec(newCommands);
        project.setDescription(desc, null);
        break;
      }
    }
  }
  
  /**
   * Static help method.
   * Adds a JavaCC Nature to the project.
   * Used only in sf.eclipse.javacc.options.JJPropertyPage.
   * @param boolean isJJNature adds if true, removes if false
   * @param IProject project to change
   */
  static public void setJJNature(boolean isJJNature, IProject project) {
    if (project == null) return;
    try {   
      IProjectDescription desc = project.getDescription();
      String[] natures = desc.getNatureIds();
      boolean found = false;
      for (int i = 0; i < natures.length; ++i) {
        if (natures[i].equals(JJ_NATURE_ID)) {
          found = true;
          break;
        }
      }
      if (!found && isJJNature) {
        // Adds nature to the projet (only adds ID)
        String[] newNatures = new String[natures.length + 1];
        System.arraycopy(natures, 0, newNatures, 0, natures.length);
        newNatures[natures.length] = JJ_NATURE_ID;
        desc.setNatureIds(newNatures);
        project.setDescription(desc, null);
      }
      if (found && !isJJNature) {
        // Remove the nature
        String[] newNatures = new String[natures.length - 1];
        for (int i = natures.length - 1; i >= 0; i--) {
          if (natures[i].equals(JJ_NATURE_ID)) {
            // Copy without JJNature
            System.arraycopy(natures, 0, newNatures, 0, i);
            System.arraycopy(natures, i + 1, newNatures, i, natures.length - i - 1);
            desc.setNatureIds(newNatures);
            project.setDescription(desc, null);
            break;
          }
        }
      }
    } catch (CoreException e) {
      e.printStackTrace();
    }
  }
}
