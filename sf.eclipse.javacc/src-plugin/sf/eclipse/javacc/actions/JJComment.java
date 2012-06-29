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
 * @author Marc Mazas 2009-2010-2011-2012
 */

public class JJComment implements IEditorActionDelegate {

  // MMa 12/2009 : formatting and javadoc revision

  /** The current editor */
  static JJEditor  sJJEditor;
  /** The current document */
  static IDocument sDocument;
  /** Comment prefix */
  static String    prefix = "//"; //$NON-NLS-1$

  /** {@inheritDoc} */
  @Override
  public void setActiveEditor(@SuppressWarnings("unused") final IAction aAction,
                              final IEditorPart aTargetEditor) {
    if (aTargetEditor == null) {
      return;
    }
    sJJEditor = (JJEditor) aTargetEditor;
    sDocument = sJJEditor.getDocument();
  }

  /** {@inheritDoc} */
  @Override
  public void selectionChanged(@SuppressWarnings("unused") final IAction aAction,
                               @SuppressWarnings("unused") final ISelection aSelection) {
    // not used
  }

  /**
   * Performs Comment or UnComment.
   * 
   * @param aAction - the action
   */
  @Override
  public void run(@SuppressWarnings("unused") final IAction aAction) {
    if (sJJEditor == null) {
      return;
    }

    final ISelection selection = sJJEditor.getSelectionProvider().getSelection();
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
      final IRegion endLine = sDocument.getLineInformation(ts.getEndLine());
      final IRegion startLine = sDocument.getLineInformation(ts.getStartLine());
      ts = new TextSelection(sDocument, startLine.getOffset(), endLine.getOffset() + endLine.getLength()
                                                               - startLine.getOffset());

      int i;
      final String endLineDelim = sDocument.getLegalLineDelimiters()[0];
      String line;
      // comment out each line
      for (i = ts.getStartLine(); i < ts.getEndLine(); i++) {
        final IRegion reg = sDocument.getLineInformation(i);
        line = sDocument.get(reg.getOffset(), reg.getLength());
        if (doComment) {
          strbuf.append(prefix);
          strbuf.append(line);
          strbuf.append(endLineDelim);
        }
        else {
          strbuf.append(line.substring(line.indexOf(prefix) + 2));
          strbuf.append(endLineDelim);
        }
      }
      // last line doesn't need line delimiter
      final IRegion reg = sDocument.getLineInformation(i);
      line = sDocument.get(reg.getOffset(), reg.getLength());
      if (doComment) {
        strbuf.append("//"); //$NON-NLS-1$
        strbuf.append(line);
      }
      else {
        strbuf.append(line.substring(line.indexOf(prefix) + 2));
      }
      // replace the text with the modified version
      sDocument.replace(startLine.getOffset(), ts.getLength(), strbuf.toString());

      // reselect text... not exactly as JavaEditor... whole text here
      sJJEditor.selectAndReveal(startLine.getOffset(), strbuf.length());
      return;

    } catch (final Exception e) {
      // should not append
    }
    return;
  }

  /**
   * Determines whether each line is prefixed by one of the prefixes.
   * 
   * @param aTextSelection - the selected text
   * @return true if all lines of the selected text are commented, false otherwise
   */
  static boolean isSelectionCommented(final ITextSelection aTextSelection) {
    final int startLine = aTextSelection.getStartLine();
    final int endLine = aTextSelection.getEndLine();
    try {
      // check for occurrences of prefix in the given lines
      for (int i = startLine; i <= endLine; i++) {
        final IRegion line = sDocument.getLineInformation(i);
        final String text = sDocument.get(line.getOffset(), line.getLength());
        final int found = text.indexOf(prefix, 0);
        if (found == -1) {
          // found a line which is not commented
          return false;
        }
        // from the start of line up to the prefix
        String s = sDocument.get(line.getOffset(), found);
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