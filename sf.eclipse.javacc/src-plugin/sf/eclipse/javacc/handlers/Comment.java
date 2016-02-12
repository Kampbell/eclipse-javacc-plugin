package sf.eclipse.javacc.handlers;

import static sf.eclipse.javacc.base.IConstants.LS;

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
 * @author Marc Mazas 2012-2013-2014-2015-2016
 */
public class Comment extends AbstractHandler {

  // MMa 10/2012 : created from the corresponding now deprecated action
  // MMa 11/2014 : modified some modifiers
  // MMa 01/2016 : fixed loss of last empty line when commenting

  /** Comment prefix */
  // care : if with an additional space : it may not exist when decommenting
  public static final String COMMENT_PREFIX        = "//";                   //$NON-NLS-1$

  /** Comment prefix length */
  public static final int    COMMENT_PREFIX_LENGTH = COMMENT_PREFIX.length();

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

    int tslen = ts.getLength();
    int sloffset = 0;
    try {
      int tssl = ts.getStartLine();
      int tsel = ts.getEndLine();

      // if partial lines are selected, extend selection
      final IRegion startLine = doc.getLineInformation(tssl);
      final IRegion endLine = doc.getLineInformation(tsel);
      sloffset = startLine.getOffset();
      final int eloffset = endLine.getOffset();
      final String eldelim = doc.getLineDelimiter(tsel);
      final int eldelimlen = eldelim == null ? 0 : eldelim.length();
      final int ellen = endLine.getLength();
      int extlen = eloffset + ellen + eldelimlen - sloffset;
      if (extlen > doc.getLength()) {
        extlen = doc.getLength();
      }
      ts = new TextSelection(doc, sloffset, extlen);
      tssl = ts.getStartLine();
      tsel = ts.getEndLine();
      tslen = ts.getLength();
      final StringBuilder buf = new StringBuilder(tslen + COMMENT_PREFIX_LENGTH * (tsel - tssl));

      // comment out each line
      for (int i = ts.getStartLine(); i <= ts.getEndLine(); i++) {
        final IRegion reg = doc.getLineInformation(i);
        final String line = doc.get(reg.getOffset(), reg.getLength());
        if (doComment) {
          buf.append(COMMENT_PREFIX);
          buf.append(line);
          buf.append(LS);
        }
        else {
          buf.append(line.substring(line.indexOf(COMMENT_PREFIX) + COMMENT_PREFIX_LENGTH));
          buf.append(LS);
        }
      }
      // replace the text with the modified version
      doc.replace(sloffset, ts.getLength(), buf.toString());

      // reselect text... not exactly as JavaEditor... whole text here
      jEditor.selectAndReveal(sloffset, buf.length());
      return null;

    } catch (final BadLocationException e) {
      AbstractActivator.logBug(e, sloffset, tslen);
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
  static boolean isSelectionCommented(final IDocument aDoc, final ITextSelection aTextSelection) {
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
