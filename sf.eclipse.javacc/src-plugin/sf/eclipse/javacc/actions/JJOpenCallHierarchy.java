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

import sf.eclipse.javacc.IJJConstants;
import sf.eclipse.javacc.editors.JJCallHierarchy;
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
 * @author Marc Mazas 2009-2010
 */
public class JJOpenCallHierarchy implements IEditorActionDelegate, IJJConstants {

  // MMa 11/2009 : javadoc and formatting revision

  /** the action's editor */
  static JJEditor fEditor;

  /**
   * @see IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
   */
  public void setActiveEditor(@SuppressWarnings("unused") final IAction action, final IEditorPart targetEditor) {
    if (targetEditor == null) {
      return;
    }
    fEditor = (JJEditor) targetEditor;
  }

  /**
   * @see IEditorActionDelegate#run(IAction)
   */
  public void run(@SuppressWarnings("unused") final IAction action) {
    final IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
    final IWorkbenchPage page = windows[0].getActivePage();
    // Find JJCallHierarchy View part
    JJCallHierarchy view = (JJCallHierarchy) page.findView(CALLHIERARCHY_ID);
    // Bring JJCallHierarchy to front
    try {
      view = (JJCallHierarchy) page.showView(CALLHIERARCHY_ID);
    } catch (final PartInitException e) {
      e.printStackTrace();
    }
    // Get text selection, extend it, using if necessary the appropriate method in JJGotoRule
    ITextSelection sel = (ITextSelection) fEditor.getSelectionProvider().getSelection();
    if (sel.getLength() <= 0) {
      sel = JJGotoRule.selectWord(sel);
    }
    if (!sel.isEmpty()) {
      final String text = sel.getText();
      // Search matching node in AST
      // The line is added to the text to distinguish between multiples occurrences
      // TODO this does not work for cases where 2 or more occurrences of the same production appear on the same line
      // Note that unlike in JDT the cursor is kept on the clicked identifier
      final JJNode node = fEditor.getJJElements().getIdentifierNode(text + (sel.getStartLine() + 1));
      // To behave like JDT uncomment this two lines
      //      node = editor.getJJElements().getNode(text);
      //      editor.setSelection(node);
      if (node != null) {
        // Now pass the JJNode to the JJCallHierarchy View part
        view.setSelection(node, fEditor);
      }
    }
  }

  /**
   * @see IEditorActionDelegate#selectionChanged(IAction, ISelection)
   */
  public void selectionChanged(@SuppressWarnings("unused") final IAction action,
                               @SuppressWarnings("unused") final ISelection selection) {
    // Not used. The selection is retreived inside run()
  }
}
