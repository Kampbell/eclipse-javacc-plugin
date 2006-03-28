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

import sf.eclipse.javacc.IJJConstants;
import sf.eclipse.javacc.JJBuilder;
import sf.eclipse.javacc.editors.JJEditor;

/**
 * Compile with JJDoc action
 * Referenced by plugin.xml 
 * <extension point="org.eclipse.ui.popupMenus"> 
 *  for popup menu on Editor (only)
 *  
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */

public class JJDocCompile implements IEditorActionDelegate, IJJConstants {
  static JJEditor editor;
  static IResource res;
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction,
   *      org.eclipse.ui.IEditorPart)
   */
  public void setActiveEditor(IAction action, IEditorPart targetEditor) {
    if (targetEditor == null)
      return;
    editor = (JJEditor) targetEditor;
    IEditorInput input = editor.getEditorInput();
    res = (IResource) input.getAdapter(IResource.class);
  }
  
  /* (non-Javadoc) */
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
    
    // Save the file if needed.
    if (editor.isDirty())
      editor.doSave(null);
       
    // Call GenDoc
    JJBuilder.GenDocForJJResource(res);
    
    // Refreshing the whole project (just to show the generated .html)
    // has the side effect to clears the Console if autobuild 
    try {
      res.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
    } catch (CoreException e) {
      e.printStackTrace();
    }
    return;
  }
}