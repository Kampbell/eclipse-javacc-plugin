package sf.eclipse.javacc.handlers;

import static sf.eclipse.javacc.base.IConstants.CALL_HIERARCHY_ID;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import sf.eclipse.javacc.base.AbstractActivator;
import sf.eclipse.javacc.editors.CallHierarchyView;
import sf.eclipse.javacc.editors.JJEditor;
import sf.eclipse.javacc.parser.JJNode;

/**
 * Show Call Hierarchy handler.<br>
 * Referenced by plugin.xml.<br>
 * <extension point="org.eclipse.ui.handlers">.<br>
 * 
 * @since 1.5.28 (from when menus and handlers have replaced actions, ...)
 * @author Marc Mazas 2012-2013-2014-2015
 */
public class ShowCallHierarchy extends AbstractHandler {

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
      AbstractActivator.logErr(AbstractActivator.getMsg("Editor.NotOur_problem (" + editor.getClass().getName() + ")")); //$NON-NLS-1$ //$NON-NLS-2$
      return null;
    }
    // our editor
    final JJEditor jEditor = (JJEditor) editor;
    final IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
    Assert.isTrue(windows.length > 0);
    final IWorkbenchPage page = windows[0].getActivePage();
    // find the CallHierarchyView
    CallHierarchyView chv = (CallHierarchyView) page.findView(CALL_HIERARCHY_ID);
    // bring it to front
    try {
      chv = (CallHierarchyView) page.showView(CALL_HIERARCHY_ID);
    } catch (final PartInitException e) {
      AbstractActivator.logBug(e);
      return null;
    }
    // get text selection, extend it, using if necessary the appropriate method in GotoRule
    ITextSelection sel = (ITextSelection) jEditor.getSelectionProvider().getSelection();
    if (sel.getLength() <= 0) {
      sel = jEditor.selectWord(sel);
    }
    if (!sel.isEmpty()) {
      final String text = sel.getText();
      // search matching node in AST
      // the offset is prepended to the text to distinguish between multiples occurrences
      // note that unlike in JDT the cursor is kept on the clicked identifier
      //      final int start = sel.getOffset() + 1;
      final int start = sel.getStartLine() + 1;
      final JJNode node = jEditor.getElements().getIdentOrNodeDesc(start + text);
      // to behave like JDT uncomment this two lines
      //      node = editor.getElements().getNode(text);
      //      editor.setSelection(node);
      //      if (node != null) {
      // pass the JJNode to the CallHierarchyView
      chv.setSelection(node, jEditor);
      //      }
    }
    else {
      //  pass a null node for empty selection to the Call Hierarchy View
      chv.setSelection(null, jEditor);
    }
    return null;
  }

}
