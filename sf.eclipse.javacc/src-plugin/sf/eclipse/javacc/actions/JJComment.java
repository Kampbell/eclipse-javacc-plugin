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
 * Manages Comment / UnComment actions.<br>
 * Referenced by plugin.xml<br>
 * <extension point="org.eclipse.ui.popupMenus"><br>
 * for popup menu on Editor<br>
 * <extension point="org.eclipse.ui.editorActions"><br>
 * for key binding<br>
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */

public class JJComment implements IEditorActionDelegate {

  // MMa 12/2009 : formatting and javadoc revision

  /** the current editor */
  static JJEditor  fEditor;
  /** the current document */
  static IDocument fDocument;
  /** comment prefix */
  static String    fPrefix = "//"; //$NON-NLS-1$

  /**
   * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
   */
  public void setActiveEditor(@SuppressWarnings("unused") final IAction action, final IEditorPart targetEditor) {
    if (targetEditor == null) {
      return;
    }
    fEditor = (JJEditor) targetEditor;
    fDocument = fEditor.getDocument();
  }

  /**
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
   */
  public void selectionChanged(@SuppressWarnings("unused") final IAction action,
                               @SuppressWarnings("unused") final ISelection selection) {
    // not used
  }

  /**
   * Performs Comment or UnComment.
   * 
   * @param action the action
   */
  public void run(@SuppressWarnings("unused") final IAction action) {
    if (fEditor == null) {
      return;
    }

    final ISelection selection = fEditor.getSelectionProvider().getSelection();
    if (!(selection instanceof ITextSelection)) {
      return;
    }

    ITextSelection ts = (ITextSelection) selection;
    if (ts.getStartLine() < 0 || ts.getEndLine() < 0) {
      return;
    }

    boolean doComment = true;
    if (isSelectionCommented(ts)) {
      doComment = false;
    }

    try {
      // replacement buffer
      final StringBuffer strbuf = new StringBuffer(128);

      // if partial lines are selected, extend selection
      final IRegion endLine = fDocument.getLineInformation(ts.getEndLine());
      final IRegion startLine = fDocument.getLineInformation(ts.getStartLine());
      ts = new TextSelection(fDocument, startLine.getOffset(), endLine.getOffset() + endLine.getLength()
                                                               - startLine.getOffset());

      int i;
      final String endLineDelim = fDocument.getLegalLineDelimiters()[0];
      String line;
      // comment out each line
      for (i = ts.getStartLine(); i < ts.getEndLine(); i++) {
        final IRegion reg = fDocument.getLineInformation(i);
        line = fDocument.get(reg.getOffset(), reg.getLength());
        if (doComment) {
          strbuf.append(fPrefix);
          strbuf.append(line);
          strbuf.append(endLineDelim);
        }
        else {
          strbuf.append(line.substring(line.indexOf(fPrefix) + 2));
          strbuf.append(endLineDelim);
        }
      }
      // last line doesn't need line delimiter
      final IRegion reg = fDocument.getLineInformation(i);
      line = fDocument.get(reg.getOffset(), reg.getLength());
      if (doComment) {
        strbuf.append("//"); //$NON-NLS-1$
        strbuf.append(line);
      }
      else {
        strbuf.append(line.substring(line.indexOf(fPrefix) + 2));
      }
      // replace the text with the modified version
      fDocument.replace(startLine.getOffset(), ts.getLength(), strbuf.toString());

      // reselect text... not exactly as JavaEditor... whole text here
      fEditor.selectAndReveal(startLine.getOffset(), strbuf.length());
      return;

    } catch (final Exception e) {
      // should not append
    }
    return;
  }

  /**
   * Determines whether each line is prefixed by one of the prefixes.
   * 
   * @param ts the selected text
   * @return true if all lines of the selected text are commented, false otherwise
   */
  static boolean isSelectionCommented(final ITextSelection ts) {
    final int startLine = ts.getStartLine();
    final int endLine = ts.getEndLine();
    try {
      // check for occurrences of prefix in the given lines
      for (int i = startLine; i <= endLine; i++) {
        final IRegion line = fDocument.getLineInformation(i);
        final String text = fDocument.get(line.getOffset(), line.getLength());
        final int found = text.indexOf(fPrefix, 0);
        if (found == -1) {
          // found a line which is not commented
          return false;
        }
        // from the start of line up to the prefix
        String s = fDocument.get(line.getOffset(), found);
        s = s.trim();
        if (s.length() != 0) {
          // found a line which is not commented
          return false;
        }
      }
      return true;
    } catch (final BadLocationException e) {
      // should not happen
    }
    return false;
  }
}