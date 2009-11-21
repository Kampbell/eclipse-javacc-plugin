package sf.eclipse.javacc.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import sf.eclipse.javacc.editors.JJEditor;
import sf.eclipse.javacc.parser.JJNode;

/**
 * Action to jump to rule declaration Referenced by plugin.xml 
 * <extension point="org.eclipse.ui.popupMenus"> for popup menu on Editor
 * <extension point="org.eclipse.ui.editorActions"> for key binding
 *  
 * @author Remi Koutcherawy 2003-2006
 * CeCILL license http://www.cecill.info/index.en.html
 */

public class JJGotoRule implements IEditorActionDelegate {
  static JJEditor editor;

  /* (non-Javadoc) */
  public void setActiveEditor(IAction action, IEditorPart targetEditor) {
    if (targetEditor == null)
      return;
    editor = (JJEditor) targetEditor;
  }

  /* (non-Javadoc) */
  public void selectionChanged(IAction action, ISelection selection) {
    // not used. The selection is got inside run()
  }

  /**
   * Get Selection from Editor, search matching node in AST then select node
   * corresponding text. Put last selection in History
   * 
   * @see Action#run()
   */
  public void run(IAction a) {
    ITextSelection selection = (ITextSelection) editor.getSelectionProvider()
        .getSelection();
    if (selection.getLength() <= 0) {
      selection = selectWord(selection);
    }
    if (!selection.isEmpty()) {
      String text = selection.getText();
      // Search matching node in AST
      JJNode node = editor.getJJElements().getNonIdentifierNode(text);
      if (node != null) {
        editor.setSelection(node);
      }
    }
  }

  /**
   * Extend Selection to a whole Word
   * static because also used by JJOpenCallHierarchy
   */
  static public ITextSelection selectWord(ITextSelection sel) {
    int caretPos = sel.getOffset();
    IDocument doc = editor.getDocument();
    int startPos, endPos;
    try {
      int pos = caretPos;
      char c;
      while (pos >= 0) {
        c = doc.getChar(pos);
        if (!Character.isJavaIdentifierPart(c))
          break;
        pos--;
      }
      startPos = pos + 1;
      pos = caretPos;
      int length = doc.getLength();
      while (pos < length) {
        c = doc.getChar(pos);
        if (!Character.isJavaIdentifierPart(c))
          break;
        pos++;
      }
      endPos = pos;
      return new TextSelection(doc, startPos, endPos - startPos);
    } catch (BadLocationException x) {
      // Do nothing, except returning
    }
    return sel;
  }
}