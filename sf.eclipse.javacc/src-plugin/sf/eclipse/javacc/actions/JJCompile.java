package sf.eclipse.javacc.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
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
 * Compile action
 * Referenced by plugin.xml 
 * <extension point="org.eclipse.ui.popupMenus"> 
 *  for popup menu on Package Explorer 
 * AND
 *  for popup menu on Editor
 *  
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */
public class JJCompile implements IObjectActionDelegate, IEditorActionDelegate, IJJConstants{
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
   * Compile the .jj or .jjt file
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
      
      // Force Compile if not triggered
      if (!("true").equals(res.getProject().getPersistentProperty(QN_JJ_NATURE))){ //$NON-NLS-1$ 
        JJBuilder.CompileJJResource(res);
      }
      
      // Refresh the whole project to trigger compilation of Java files
      res.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
      
    } catch (CoreException e) {
      e.printStackTrace();
    }
    return;
  }
}