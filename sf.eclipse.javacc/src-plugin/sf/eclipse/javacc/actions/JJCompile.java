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
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import sf.eclipse.javacc.IJJConstants;
import sf.eclipse.javacc.JJBuilder;
import sf.eclipse.javacc.editors.JJEditor;

/**
 * Compile action. Referenced by plugin.xml<br>
 * <extension point="org.eclipse.ui.popupMenus"><br>
 * for popup menu in Package Explorer AND for popup menu in Editor
 * 
 * @author Remi Koutcherawy 2003-2009 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009
 */
public class JJCompile implements IObjectActionDelegate, IEditorActionDelegate, IJJConstants {

  // MMa 04/09 : formatting and javadoc revision ; removed jtb files (managed in JTBCompile)

  /** the current editor */
  private JJEditor  editor;
  /** the resource to compile */
  private IResource res;

  /**
   * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction,
   *      org.eclipse.ui.IEditorPart)
   */
  public void setActiveEditor(@SuppressWarnings("unused") final IAction action, final IEditorPart targetEditor) {
    if (targetEditor == null) {
      return;
    }
    editor = (JJEditor) targetEditor;
    final IEditorInput input = editor.getEditorInput();
    res = (IResource) input.getAdapter(IResource.class);
  }

  /**
   * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction,
   *      org.eclipse.ui.IWorkbenchPart)
   */
  public void setActivePart(@SuppressWarnings("unused") final IAction action,
                            @SuppressWarnings("unused") final IWorkbenchPart targetPart) {
    // not used
  }

  /**
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction action, ISelection selection)
   */
  public void selectionChanged(@SuppressWarnings("unused") final IAction action, final ISelection selection) {
    if (selection instanceof IStructuredSelection) {
      final Object obj = ((IStructuredSelection) selection).getFirstElement();
      if (obj != null && obj instanceof IFile) {
        res = (IFile) obj;
      }
    }
  }

  /**
   * Compile the .jj or .jjt file.
   * 
   * @see IActionDelegate#run(IAction)
   * @param action the action proxy that handles the presentation portion of the action
   */
  public void run(@SuppressWarnings("unused") final IAction action) {
    if (res == null) {
      return;
    }

    try {
      // Saving the file triggers a new Compilation if project has JJNature
      if (editor != null) {
        editor.doSave(null); // Called from Editor
      }
      else {
        res.touch(null); // Called from Package Explorer
      }

      // Force Compile if not triggered
      final IScopeContext projectScope = new ProjectScope(res.getProject());
      final IEclipsePreferences prefs = projectScope.getNode(IJJConstants.ID);
      if (!("true").equals(prefs.get(JJ_NATURE, "false")) //$NON-NLS-1$ //$NON-NLS-2$
          || !isOnClasspath() || !res.getWorkspace().isAutoBuilding()) {
        JJBuilder.CompileResource(res);
      }

      // Refresh the whole project to trigger compilation of Java files
      res.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);

    } catch (final CoreException e) {
      e.printStackTrace();
    }
    return;
  }

  /**
   * Check if the resource is a .jj, .jjt file and is on classpath.
   * 
   * @return true if all is ok, false otherwise
   */
  protected boolean isOnClasspath() {
    boolean gen = true;
    // Look only for jj, jjt and jtb files
    final String ext = res.getFullPath().getFileExtension();
    if ("jj".equals(ext) || "jjt".equals(ext)) { //$NON-NLS-1$ //$NON-NLS-2$
      final IProject project = res.getProject();
      final IJavaProject javaProject = JavaCore.create(project);
      if (javaProject != null) {
        gen = javaProject.isOnClasspath(res);
      }
    }
    return gen;
  }
}