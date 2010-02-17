package sf.eclipse.javacc.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

import sf.eclipse.javacc.IJJConstants;
import sf.eclipse.javacc.JJBuilder;
import sf.eclipse.javacc.editors.JJEditor;

/**
 * Compile with JJDoc action.<br>
 * Referenced by plugin.xml<br>
 * <extension point="org.eclipse.ui.popupMenus"><br>
 * for popup menu on Editor (only)
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */

public class JJDocCompile implements IEditorActionDelegate, IJJConstants {

  // MMa 12/2009 : formatting and javadoc revision

  /** the current editor */
  private JJEditor  editor;
  /** the resource to compile */
  private IResource res;

  /**
   * @see IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
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
   * @see IActionDelegate#selectionChanged(IAction action, ISelection selection)
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

    // save the file if needed
    if (editor.isDirty()) {
      editor.doSave(null);
    }

    // call GenDoc
    JJBuilder.GenDocForJJResource(res);

    // refreshing the whole project (just to show the generated .html)
    // has the side effect to clear the Console if autobuild is on
    try {
      res.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
    } catch (final CoreException e) {
      e.printStackTrace();
    }
    return;
  }
}