package sf.eclipse.javacc.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import sf.eclipse.javacc.base.AbstractActivator;
import sf.eclipse.javacc.editors.JJEditor;

/**
 * Content Assistant (completion proposal) handler.<br>
 * Referenced by plugin.xml.<br>
 * <extension point="org.eclipse.ui.handlers">.<br>
 * 
 * @since 1.5.28 (from when menus and handlers have replaced actions, ...)
 * @author Marc Mazas 2012-2013-2014
 */
public class ContentAssist extends AbstractHandler {

  // MMa 11/2012 : created from the corresponding now deprecated action

  /** {@inheritDoc} */
  @Override
  public Object execute(final ExecutionEvent event) {
    // in which part were we called
    final IWorkbenchPart part = HandlerUtil.getActivePart(event);
    if (!(part instanceof IEditorPart)) {
      // on a viewer, do nothing
      return null;
    }
    // on an editor
    final IEditorPart editor = (IEditorPart) part;
    if (!(editor instanceof JJEditor)) {
      // not our editor (no reason why, however), do nothing
      AbstractActivator.logErr(AbstractActivator.getMsg("Editor.Null_problem")); //$NON-NLS-1$
      return null;
    }
    // our editor
    final JJEditor jEditor = (JJEditor) editor;
    final ITextOperationTarget target = (ITextOperationTarget) jEditor.getAdapter(ITextOperationTarget.class);
    if (target != null && target.canDoOperation(ISourceViewer.CONTENTASSIST_PROPOSALS)) {
      target.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
    }
    return null;
  }

}
