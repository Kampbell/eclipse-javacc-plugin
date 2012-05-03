package sf.eclipse.javacc.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import sf.eclipse.javacc.base.IJJConstants;
import sf.eclipse.javacc.editors.JJCallHierarchyView;
import sf.eclipse.javacc.editors.JJEditor;
import sf.eclipse.javacc.parser.JJNode;

/**
 * Open call hierarchy Action referenced by plugin.xml.<br>
 * For popup menu on Editor<br>
 * <extension point="org.eclipse.ui.popupMenus"><br>
 * <action class="sf.eclipse.javacc.actions.JJOpenCallHierarchy"><br>
 * For key binding<br>
 * <extension point="org.eclipse.ui.editorActions"><br>
 * <action class="sf.eclipse.javacc.actions.JJOpenCallHierarchy"><br>
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011
 */
public class JJOpenCallHierarchy implements IEditorActionDelegate, IJJConstants {

  // MMa 11/2009 : javadoc and formatting revision
  // MMa 08/2011 : enhanced Call Hierarchy view (changed selection key)

  /** the action's editor */
  static JJEditor sJJEditor;

  /**
   * @see IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
   */
  @Override
  public void setActiveEditor(@SuppressWarnings("unused") final IAction aAction,
                              final IEditorPart aTargetEditor) {
    if (aTargetEditor == null) {
      return;
    }
    sJJEditor = (JJEditor) aTargetEditor;
  }

  /**
   * @see IEditorActionDelegate#run(IAction)
   */
  @Override
  public void run(@SuppressWarnings("unused") final IAction aAction) {
    final IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
    final IWorkbenchPage page = windows[0].getActivePage();
    // Find JJCallHierarchy View part
    JJCallHierarchyView view = (JJCallHierarchyView) page.findView(CALLHIERARCHY_ID);
    // Bring JJCallHierarchy to front
    try {
      view = (JJCallHierarchyView) page.showView(CALLHIERARCHY_ID);
    } catch (final PartInitException e) {
      e.printStackTrace();
    }
    // Get text selection, extend it, using if necessary the appropriate method in JJGotoRule
    ITextSelection sel = (ITextSelection) sJJEditor.getSelectionProvider().getSelection();
    if (sel.getLength() <= 0) {
      sel = JJGotoRule.selectWord(sel);
    }
    if (!sel.isEmpty()) {
      final String text = sel.getText();
      // Search matching node in AST
      // The line is prepended to the text to distinguish between multiples occurrences
      // TODO this does not work for cases where 2 or more occurrences of the same production appear on the same line
      // Note that unlike in JDT the cursor is kept on the clicked identifier
      final int start = sel.getStartLine() + 1;
      final JJNode node = sJJEditor.getJJElements().getIdentOrNodeDesc(start + text);
      // To behave like JDT uncomment this two lines
      //      node = editor.getJJElements().getNode(text);
      //      editor.setSelection(node);
      //      if (node != null) {
      // Now pass the JJNode to the JJCallHierarchy View part
      view.setSelection(node, sJJEditor);
      //      }
    }
    else {
      // Now pass a null node to the JJCallHierarchy View part
      view.setSelection(null, sJJEditor);
    }
  }

  /**
   * @see IEditorActionDelegate#selectionChanged(IAction, ISelection)
   */
  @Override
  public void selectionChanged(@SuppressWarnings("unused") final IAction aAction,
                               @SuppressWarnings("unused") final ISelection aSelection) {
    // Not used. The selection is retrieved inside run()
  }
}
