package sf.eclipse.javacc.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import sf.eclipse.javacc.editors.JJEditor;

/**
 * Toggle Comment action
 * Referenced by plugin.xml 
 * <extension point="org.eclipse.ui.popupMenus">
 *  for popup menu on Editor
 * <extension point="org.eclipse.ui.editorActions"> 
 *  for key binding
 *  
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */

public class JJComment implements IEditorActionDelegate{
  static JJEditor editor;
  static IDocument doc;
  static String prefix = "//"; //$NON-NLS-1$

  /* (non-Javadoc)
   * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
   */
  public void setActiveEditor(IAction action, IEditorPart targetEditor) {
    if (targetEditor == null)
      return;
    editor = (JJEditor) targetEditor;
    doc = editor.getDocument();
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
   */
  public void selectionChanged(IAction action, ISelection selection) {
    // not used
  }
  
  /**
   * Perform Comment or UnComment
   */
  public void run(IAction action) {
    if (editor == null)
      return;

    ISelection selection = editor.getSelectionProvider().getSelection();
    if (!(selection instanceof ITextSelection))
      return;

    ITextSelection ts = (ITextSelection) selection;
    if (ts.getStartLine() < 0 || ts.getEndLine() < 0)
      return;

    boolean doComment = true;
    if (isSelectionCommented(ts))
      doComment = false;

    try {
      // Buffer for replacement text
      StringBuffer strbuf = new StringBuffer();

      // If partial lines are selected, extend selection
      IRegion endLine = doc.getLineInformation(ts.getEndLine());
      IRegion startLine = doc.getLineInformation(ts.getStartLine());
      ts = new TextSelection(doc, startLine.getOffset(), endLine.getOffset()
          + endLine.getLength() - startLine.getOffset());

      int i;
      String endLineDelim = doc.getLegalLineDelimiters()[0];
      String line;
      // For each line, comment out
      for (i = ts.getStartLine(); i < ts.getEndLine(); i++) {
        IRegion reg = doc.getLineInformation(i);
        line = doc.get(reg.getOffset(), reg.getLength());
        if (doComment) {
          strbuf.append(prefix);
          strbuf.append(line);
          strbuf.append(endLineDelim);
        } else {
          strbuf.append(line.substring(line.indexOf(prefix)+2));
          strbuf.append(endLineDelim);
        }
      }
      // Last line doesn't need line delimiter
      IRegion reg = doc.getLineInformation(i);
      line = doc.get(reg.getOffset(), reg.getLength());
      if (doComment) {
        strbuf.append("//"); //$NON-NLS-1$
        strbuf.append(line);
      } else {
           strbuf.append(line.substring(line.indexOf(prefix)+2));
      }
      // Replace the text with the modified version
      doc.replace(startLine.getOffset(), ts.getLength(), strbuf.toString());
      
      // Reselect text... not exactly as JavaEditor... whole text here
      editor.selectAndReveal(startLine.getOffset(), strbuf.length());
      return;
      
    } catch (Exception e) {
      // Should not append
    }
    return;
  }

  /**
   * Determines whether each line is prefixed by one of the prefixes.
   */
  static boolean isSelectionCommented(ITextSelection ts) {
    int startLine = ts.getStartLine();
    int endLine = ts.getEndLine();
    try {
      // check for occurrences of prefixe in the given lines
      for (int i = startLine; i <= endLine; i++) {
        IRegion line = doc.getLineInformation(i);
        String text = doc.get(line.getOffset(), line.getLength());
        int found = text.indexOf(prefix, 0);
        if (found == -1)
          // found a line which is not commented
          return false;
        // From the start of line upto the prefix
        String s = doc.get(line.getOffset(), found);
        s = s.trim();
        if (s.length() != 0)
          // found a line which is not commented
          return false;
      }
      return true;
    } catch (BadLocationException e) {
      // should not happen
    }
    return false;
  }
}