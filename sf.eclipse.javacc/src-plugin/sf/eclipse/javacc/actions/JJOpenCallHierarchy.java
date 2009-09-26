package sf.eclipse.javacc.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.*;

import sf.eclipse.javacc.IJJConstants;
import sf.eclipse.javacc.editors.JJCallHierarchy;
import sf.eclipse.javacc.editors.JJEditor;
import sf.eclipse.javacc.parser.JJNode;

/**
* Open call hierarchy Action referenced by plugin.xml 
* For popup menu on Editor 
*  <extension point="org.eclipse.ui.popupMenus"> 
*   <action class="sf.eclipse.javacc.actions.JJOpenCallHierarchy">
* For key binding
*  <extension point="org.eclipse.ui.editorActions"> 
*   <action class="sf.eclipse.javacc.actions.JJOpenCallHierarchy">
*   
*   
*/
public class JJOpenCallHierarchy implements IEditorActionDelegate, IJJConstants {
  static JJEditor editor;

  public void setActiveEditor(IAction action, IEditorPart targetEditor) {
    if (targetEditor == null)
      return;
    editor = (JJEditor) targetEditor;
  }

  public void run(IAction action) {
    IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
    IWorkbenchPage page = windows[0].getActivePage();
    // Find JJCallHierarchy View part
    JJCallHierarchy view = (JJCallHierarchy) page.findView(CALLHIERARCHY_ID);
    // Bring JJCallHierarchy to front
    try {
      view = (JJCallHierarchy) page.showView(CALLHIERARCHY_ID);
    } catch (PartInitException e) {
      e.printStackTrace();
    }
    // Get text selection, extend it,  using eventually method in JJGotoRule
    ITextSelection sel = (ITextSelection)editor.getSelectionProvider().getSelection();
    if (sel.getLength() <= 0) {
      sel = JJGotoRule.selectWord(sel);
    }
    if (!sel.isEmpty()) {
      String text = sel.getText();
      // Search matching node in AST
      // The line is added to the text to distinguish between multiples occurrences
      // Note that contrary to JDT the cursor is kept on the identifier clicked
      JJNode node = editor.getJJElements().getIdentifierNode(text+(sel.getStartLine()+1));
      // To behave like JDT uncomment this two lines
//      node = editor.getJJElements().getNode(text);
//      editor.setSelection(node);
      if (node != null) {
        // Now pass the JJNode to the JJCallHierarchy View part
        view.setSelection(node, editor);
      }
    }
  }

  public void selectionChanged(IAction action, ISelection selection) {
    // not used. The selection is got inside run()
  }
}
