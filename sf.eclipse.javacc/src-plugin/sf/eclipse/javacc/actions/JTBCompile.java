package sf.eclipse.javacc.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import sf.eclipse.javacc.base.IJJConstants;
import sf.eclipse.javacc.editors.JTBEditor;
import sf.eclipse.javacc.head.JJBuilder;

/**
 * Compile with JTB action.<br>
 * Referenced by plugin.xml<br>
 * <extension point="org.eclipse.ui.popupMenus"><br>
 * for popup menu in Package Explorer AND for popup menu in Editor
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012
 */
public class JTBCompile implements IObjectActionDelegate, IEditorActionDelegate, IJJConstants {

  // MMa 04/2009 : formatting and javadoc revision ; adapted to JTBEditor ; removed jj and jjt files (managed in JJEditor)
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 03/2010 : doSave changed into touch

  /** The current editor */
  private JTBEditor jJTBEditor;
  /** The resource to compile */
  private IResource jRes;

  /** {@inheritDoc} */
  @Override
  public void setActiveEditor(@SuppressWarnings("unused") final IAction aAction,
                              final IEditorPart aTargetEditor) {
    if (aTargetEditor == null) {
      return;
    }
    jJTBEditor = (JTBEditor) aTargetEditor;
    final IEditorInput input = jJTBEditor.getEditorInput();
    jRes = (IResource) input.getAdapter(IResource.class);
  }

  /** {@inheritDoc} */
  @Override
  public void setActivePart(@SuppressWarnings("unused") final IAction aAction,
                            @SuppressWarnings("unused") final IWorkbenchPart aTargetPart) {
    // not used
  }

  /** {@inheritDoc} */
  @Override
  public void selectionChanged(@SuppressWarnings("unused") final IAction aAction, final ISelection aSelection) {
    if (aSelection instanceof IStructuredSelection) {
      final Object obj = ((IStructuredSelection) aSelection).getFirstElement();
      if (obj != null && obj instanceof IFile) {
        jRes = (IFile) obj;
      }
    }
  }

  /**
   * Compiles the .jtb file.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public void run(@SuppressWarnings("unused") final IAction aAction) {
    if (jRes == null) {
      return;
    }

    try {
      //      // saving the file triggers a new Compilation if project has JJNature
      //      if (fEditor != null) {
      //        // called from Editor
      //                fEditor.doSave(null);
      //      }
      //      else {
      //        // called from Package explorer
      //        fRes.touch(null);
      //      }
      // touch the file 
      jRes.touch(null);

      // force Compile if not triggered
      final IScopeContext projectScope = new ProjectScope(jRes.getProject());
      final IEclipsePreferences prefs = projectScope.getNode(IJJConstants.ID);
      if (!("true").equals(prefs.get(JJ_NATURE, "false")) //$NON-NLS-1$ //$NON-NLS-2$
          || !isOnClasspath() || !jRes.getWorkspace().isAutoBuilding()) {
        JJBuilder.CompileResource(jRes);
      }

      // refresh the whole project to trigger compilation of .jj and .java files
      jRes.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);

    } catch (final CoreException e) {
      e.printStackTrace();
    }
    return;
  }

  /**
   * Checks if the resource is a .jtb file and is on classpath.
   * 
   * @return true if all is OK, false otherwise
   */
  protected boolean isOnClasspath() {
    boolean gen = true;
    // look only for jtb files
    final String ext = jRes.getFullPath().getFileExtension();
    if ("jtb".equals(ext)) { //$NON-NLS-1$
      final IProject project = jRes.getProject();
      final IJavaProject javaProject = JavaCore.create(project);
      if (javaProject != null) {
        gen = javaProject.isOnClasspath(jRes);
      }
    }
    return gen;
  }
}