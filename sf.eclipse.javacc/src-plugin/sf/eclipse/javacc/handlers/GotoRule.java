package sf.eclipse.javacc.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import sf.eclipse.javacc.base.AbstractActivator;
import sf.eclipse.javacc.editors.JJEditor;
import sf.eclipse.javacc.parser.JJNode;

/**
 * Jump to rule declaration handler.<br>
 * Referenced by plugin.xml.<br>
 * <extension point="org.eclipse.ui.handlers">.<br>
 * 
 * @since 1.5.28 (from when menus and handlers have replaced actions, ...)
 * @author Marc Mazas 2012-2013-2014-2015
 */
public class GotoRule extends AbstractHandler {

  // MMa 10/2012 : created from the corresponding now deprecated action

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
      AbstractActivator.logErr(AbstractActivator.getMsg("Editor.NotOur_problem (" + editor.getClass().getName() + ")")); //$NON-NLS-1$ //$NON-NLS-2$
      return null;
    }
    // our editor
    final JJEditor jEditor = (JJEditor) editor;
    ITextSelection sel = (ITextSelection) jEditor.getSelectionProvider().getSelection();
    if (sel.getLength() <= 0) {
      sel = jEditor.selectWord(sel);
    }
    if (!sel.isEmpty()) {
      final String text = sel.getText();
      // search matching node in AST
      final JJNode node = jEditor.getElements().getNonIdentNorNodeDesc(text);
      if (node != null) {
        jEditor.selectNode(node);
      }
    }
    return null;
  }

}
