package sf.eclipse.javacc.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import sf.eclipse.javacc.base.AbstractActivator;
import sf.eclipse.javacc.base.Builder;
import sf.eclipse.javacc.editors.JJEditor;

/**
 * Compile with JavaCC / JJTree / JTB handler.<br>
 * Referenced by plugin.xml.<br>
 * <extension point="org.eclipse.ui.handlers">.<br>
 * 
 * @since 1.5.28 (from when menus and handlers have replaced actions, ...)
 * @author Marc Mazas 2012-2013-2014
 */
public class Compile extends AbstractHandler {

  // MMa 10/2012 : created from the corresponding now deprecated action
  // MMa 11/2014 : modified some modifiers ; added state management

  /** The builder to use */
  private final Builder jBuilder   = new Builder();

  /** The handler state */
  private boolean       jIsEnabled = false;

  /** {@inheritDoc} */
  @Override
  public void setEnabled(final Object evaluationContext) {
    final IEvaluationContext evco = (IEvaluationContext) evaluationContext;
    final Object obj = HandlerUtil.getVariable(evco, ISources.ACTIVE_EDITOR_INPUT_NAME);
    if (obj instanceof IFileEditorInput) {
      // case project file
      jIsEnabled = true;
    }
    else {
      // case dragged and dropped file
      jIsEnabled = false;
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean isEnabled() {
    return jIsEnabled;
  }

  /** {@inheritDoc} */
  @Override
  public Object execute(final ExecutionEvent event) {
    // in which part were we called
    final IWorkbenchPart part = HandlerUtil.getActivePart(event);
    if (part instanceof IEditorPart) {
      // on an editor
      final IEditorPart editor = (IEditorPart) part;
      if ((editor instanceof JJEditor)) {
        // our editor
        final JJEditor jEditor = (JJEditor) editor;
        // find the resource from the editor input
        final IEditorInput input = jEditor.getEditorInput();
        final IResource res = (IResource) input.getAdapter(IResource.class);
        jBuilder.jj_compile(res);
      }
      else {
        // not our editor (no reason why, however), do nothing
        AbstractActivator.logErr(AbstractActivator.getMsg("Editor.NotOur_problem (" + editor.getClass().getName() + ")")); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
    else {
      // on a viewer
      final ISelection selection = HandlerUtil.getCurrentSelection(event);
      if (selection instanceof IStructuredSelection) {
        // find the resource from the selected object (in the package explorer, ...)
        final Object obj = ((IStructuredSelection) selection).getFirstElement();
        if (obj != null && obj instanceof IFile) {
          final IResource res = (IFile) obj;
          // if res is null nothing will happen
          jBuilder.jj_compile(res);
        }
      }
    }
    return null;
  }

}
