package sf.eclipse.javacc.base;

import static sf.eclipse.javacc.base.IConstants.BUILDER_ID;
import static sf.eclipse.javacc.base.IConstants.NATURE_ID;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 * The Project Nature for JavaCC projects for normal usage (ie non headless builds).<br>
 * Referenced by plugin.xml.<br>
 * <extension point="org.eclipse.core.resources.natures">
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 */
public class Nature implements IProjectNature {

  // MMa 11/2009 : javadoc and formatting revision
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 03/2010 : some renamings ; removed reapplying the nature
  // MMa 10/2012 : renamed
  // MMa 11/2014 : modified some modifiers
  // MMa 12/2014 : changed package

  /** The project */
  private IProject jProject;

  /** {@inheritDoc} */
  @Override
  public final IProject getProject() {
    return jProject;
  }

  /** {@inheritDoc} */
  @Override
  public final void setProject(final IProject aProject) {
    jProject = aProject;
  }

  /**
   * Configures this nature for its project. This is called by the workspace when natures are added to the
   * project using <code>IProject.setDescription</code> and should not be called directly by clients.<br>
   * The nature extension id is added to the list of natures before this method is called, and need not be
   * added here.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public void configure() throws CoreException {
    final IProjectDescription desc = jProject.getDescription();
    final ICommand[] cmds = desc.getBuildSpec();
    ICommand command = null;
    for (int i = cmds.length - 1; i >= 0; i--) {
      if (cmds[i].getBuilderName().equals(BUILDER_ID)) {
        command = cmds[i];
        break;
      }
    }
    if (command == null) {
      // add Compiler (ID only)
      command = desc.newCommand();
      command.setBuilderName(BUILDER_ID);
      final ICommand[] newCommands = new ICommand[cmds.length + 1];
      newCommands[0] = command;
      System.arraycopy(cmds, 0, newCommands, 1, cmds.length);
      desc.setBuildSpec(newCommands);
      desc.setComment(AbstractActivator.getMsg("Nature.Nature_description")); //$NON-NLS-1$
      jProject.setDescription(desc, null);
    }
  }

  /**
   * De-configures this nature for its project. This is called by the workspace when natures are removed from
   * the project using <code>IProject.setDescription</code> and should not be called directly by clients.<br>
   * The nature extension id is removed from the list of natures before this method is called, and need not be
   * removed here.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public void deconfigure() throws CoreException {
    final IProjectDescription desc = jProject.getDescription();
    final ICommand[] cmds = desc.getBuildSpec();
    for (int i = cmds.length - 1; i >= 0; i--) {
      if (cmds[i].getBuilderName().equals(BUILDER_ID)) {
        // copy without Compiler
        final ICommand[] newCommands = new ICommand[cmds.length - 1];
        System.arraycopy(cmds, 0, newCommands, 0, i);
        System.arraycopy(cmds, i + 1, newCommands, i, cmds.length - i - 1);
        desc.setBuildSpec(newCommands);
        jProject.setDescription(desc, null);
        break;
      }
    }
  }

  /**
   * Adds or removes a JavaCC Nature (nature id only) to the project.
   * 
   * @param aNature - adds if true, removes if false
   * @param aProject - to change
   */
  static public void setNature(final boolean aNature, final IProject aProject) {
    if (aProject == null) {
      AbstractActivator.logErr(AbstractActivator.getMsg("Nature.Project_null")); //$NON-NLS-1$ 
      return;
    }
    try {
      final IProjectDescription desc = aProject.getDescription();
      final String[] natures = desc.getNatureIds();
      boolean found = false;
      // find whether nature already exists
      for (int i = 0; i < natures.length; ++i) {
        if (natures[i].equals(NATURE_ID)) {
          found = true;
          break;
        }
      }
      if (!found && aNature) {
        // add the nature to the project
        final String[] newNatures = new String[natures.length + 1];
        System.arraycopy(natures, 0, newNatures, 0, natures.length);
        newNatures[natures.length] = NATURE_ID;
        desc.setNatureIds(newNatures);
        aProject.setDescription(desc, null);
      }
      if (found) {
        if (!aNature) {
          // remove the nature from the project
          final String[] newNatures = new String[natures.length - 1];
          for (int i = natures.length - 1; i >= 0; i--) {
            if (natures[i].equals(NATURE_ID)) {
              // copy without Nature
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
      AbstractActivator.logBug(e, AbstractActivator.getMsg("Nature.Problem")); //$NON-NLS-1$
    }
  }
}
