package sf.eclipse.javacc.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import sf.eclipse.javacc.base.AbstractActivator;
import sf.eclipse.javacc.editors.JJEditor;

/**
 * Comment / UnComment handler.<br>
 * Referenced by plugin.xml.<br>
 * <extension point="org.eclipse.ui.handlers">.<br>
 * 
 * @since 1.5.28 (from when menus and handlers have replaced actions, ...)
 * @author Marc Mazas 2012-2013-2014
 */
public class Comment extends AbstractHandler {

  // MMa 10/2012 : created from the corresponding now deprecated action
  // MMa 11/2014 : modified some modifiers

  /** Comment prefix */
  public static final String COMMENT_PREFIX = "//"; //$NON-NLS-1$

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
    final ISelection selection = jEditor.getSelectionProvider().getSelection();
    if (!(selection instanceof ITextSelection)) {
      return null;
    }

    ITextSelection ts = (ITextSelection) selection;
    if (ts.getStartLine() < 0 || ts.getEndLine() < 0) {
      return null;
    }

    final IDocument doc = jEditor.getDocument();
    boolean doComment = true;
    if (isSelectionCommented(doc, ts)) {
      doComment = false;
    }

    try {
      // replacement buffer
      final StringBuffer strbuf = new StringBuffer(128);

      // if partial lines are selected, extend selection
      final IRegion endLine = doc.getLineInformation(ts.getEndLine());
      final IRegion startLine = doc.getLineInformation(ts.getStartLine());
      ts = new TextSelection(doc, startLine.getOffset(), endLine.getOffset() + endLine.getLength()
                                                         - startLine.getOffset());

      int i;
      final String endLineDelim = doc.getLegalLineDelimiters()[0];
      String line;
      // comment out each line
      for (i = ts.getStartLine(); i < ts.getEndLine(); i++) {
        final IRegion reg = doc.getLineInformation(i);
        line = doc.get(reg.getOffset(), reg.getLength());
        if (doComment) {
          strbuf.append(COMMENT_PREFIX);
          strbuf.append(line);
          strbuf.append(endLineDelim);
        }
        else {
          strbuf.append(line.substring(line.indexOf(COMMENT_PREFIX) + 2));
          strbuf.append(endLineDelim);
        }
      }
      // last line doesn't need line delimiter
      final IRegion reg = doc.getLineInformation(i);
      line = doc.get(reg.getOffset(), reg.getLength());
      if (doComment) {
        strbuf.append(COMMENT_PREFIX);
        strbuf.append(line);
      }
      else {
        strbuf.append(line.substring(line.indexOf(COMMENT_PREFIX) + 2));
      }
      // replace the text with the modified version
      doc.replace(startLine.getOffset(), ts.getLength(), strbuf.toString());

      // reselect text... not exactly as JavaEditor... whole text here
      jEditor.selectAndReveal(startLine.getOffset(), strbuf.length());
      return null;

    } catch (final BadLocationException e) {
      AbstractActivator.logBug(e);
    }
    return null;
  }

  /**
   * Determines whether each line is prefixed by one of the prefixes.
   * 
   * @param aDoc - the document
   * @param aTextSelection - the selected text
   * @return true if all lines of the selected text are commented, false otherwise
   */
  boolean isSelectionCommented(final IDocument aDoc, final ITextSelection aTextSelection) {
    final int startLine = aTextSelection.getStartLine();
    final int endLine = aTextSelection.getEndLine();
    try {
      // check for occurrences of prefix in the given lines
      for (int i = startLine; i <= endLine; i++) {
        final IRegion line = aDoc.getLineInformation(i);
        final String text = aDoc.get(line.getOffset(), line.getLength());
        final int found = text.indexOf(COMMENT_PREFIX, 0);
        if (found == -1) {
          // found a line which is not commented
          return false;
        }
        // from the start of line up to the prefix
        String s = aDoc.get(line.getOffset(), found);
        s = s.trim();
        if (s.length() != 0) {
          // found a line which is not commented
          return false;
        }
      }
      return true;
    } catch (final BadLocationException e) {
      AbstractActivator.logBug(e);
    }
    return false;
  }

}
