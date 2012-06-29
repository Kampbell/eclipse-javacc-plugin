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

import sf.eclipse.javacc.base.IJJConstants;
import sf.eclipse.javacc.editors.JJEditor;
import sf.eclipse.javacc.head.JJBuilder;

/**
 * Compile with JJDoc action.<br>
 * Referenced by plugin.xml<br>
 * <extension point="org.eclipse.ui.popupMenus"><br>
 * for popup menu on Editor (only)
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012
 */

public class JJDocCompile implements IEditorActionDelegate, IJJConstants {

  // MMa 12/2009 : formatting and javadoc revision

  /** The current editor */
  private JJEditor  jJJEditor;
  /** The resource to compile */
  private IResource jRes;

  /** {@inheritDoc} */
  @Override
  public void setActiveEditor(@SuppressWarnings("unused") final IAction aAction,
                              final IEditorPart aTargetEditor) {
    if (aTargetEditor == null) {
      return;
    }
    jJJEditor = (JJEditor) aTargetEditor;
    final IEditorInput input = jJJEditor.getEditorInput();
    jRes = (IResource) input.getAdapter(IResource.class);
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
   * Compile the .jj or .jjt file.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public void run(@SuppressWarnings("unused") final IAction aAction) {
    if (jRes == null) {
      return;
    }

    // save the file if needed
    if (jJJEditor.isDirty()) {
      jJJEditor.doSave(null);
    }

    // call GenDoc
    JJBuilder.GenDocForJJResource(jRes);

    // refreshing the whole project (just to show the generated .html)
    // has the side effect to clear the Console if automatic build is on
    try {
      jRes.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
    } catch (final CoreException e) {
      e.printStackTrace();
    }
    return;
  }
}