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

import sf.eclipse.javacc.IJJConstants;
import sf.eclipse.javacc.JJBuilder;
import sf.eclipse.javacc.editors.JJEditor;

/**
 * Compile action referenced by plugin.xml 
 * For popup menu on Editor 
 * <extension point="org.eclipse.ui.popupMenus"> 
 *  
 * @author Remi Koutcherawy 2003-2006
 * CeCILL license http://www.cecill.info/index.en.html
 */
public class JTBCompile implements IObjectActionDelegate, IEditorActionDelegate, IJJConstants{
  private JJEditor editor;
  private IResource res;
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
   */
  public void setActiveEditor(IAction action, IEditorPart targetEditor) {
    if (targetEditor == null)
      return;
    editor = (JJEditor) targetEditor;
    IEditorInput input = editor.getEditorInput();
    res = (IResource) input.getAdapter(IResource.class);
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
   */  
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    // not used
  }

  /* (non-Javadoc) 
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction action, ISelection selection)
   */
  public void selectionChanged(IAction action, ISelection selection) {
    if (selection instanceof IStructuredSelection) {
      Object obj = ((IStructuredSelection) selection).getFirstElement();
      if (obj != null && obj instanceof IFile) {
        res = (IFile) obj;
      }
    }
  }
  
  /**
   * Compile the .jtb file
   */
  public void run(IAction action) {
    if (res == null)
      return;
    
    try {
      // Saving the file triggers a new Compilation if project has JJNature
      if (editor != null) 
        editor.doSave(null); // Called from Editor
      else
        res.touch(null);     // Called from Package explorer    
      
      // Force Compile is not triggered
      IScopeContext projectScope = new ProjectScope(res.getProject());
      IEclipsePreferences prefs = projectScope.getNode(IJJConstants.ID);

      if (!("true").equals(prefs.get(JJ_NATURE, "false"))  //$NON-NLS-1$ //$NON-NLS-2$
          || !isOnClasspath(res) )
        JJBuilder.CompileResource(res);
      
      // Refresh the whole project to trigger compilation of Java files
      res.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
      
    } catch (CoreException e) {
      e.printStackTrace();
    }
    return;   
  }
  /**
   * Check if res is a .jj, .jjt, jtb file and is on classpath
   * @param Object obj
   */
  protected boolean isOnClasspath(Object obj) {
    boolean gen = true;
    if (obj instanceof IResource) {
      IResource res = (IResource)obj;
      // Look only for jj, jjt and jtb files
      String ext = res.getFullPath().getFileExtension();
      if ("jj".equals(ext) || "jjt".equals(ext) || "jtb".equals(ext)){ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        IProject project = res.getProject();
        IJavaProject javaProject = JavaCore.create(project);
        if (javaProject != null) 
          gen = javaProject.isOnClasspath(res);
      }
    }
    return gen;
  }
}