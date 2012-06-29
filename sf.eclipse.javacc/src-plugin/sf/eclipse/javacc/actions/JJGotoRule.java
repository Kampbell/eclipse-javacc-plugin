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
import sf.eclipse.javacc.editors.JJHyperlinkDetector;
import sf.eclipse.javacc.parser.JJNode;

/**
 * Action to jump to rule declaration.<br>
 * Referenced by plugin.xml<br>
 * <extension point="org.eclipse.ui.popupMenus"> for popup menu on Editor<br>
 * <extension point="org.eclipse.ui.editorActions"> for key binding
 * 
 * @author Remi Koutcherawy 2003-2006 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012
 */
public class JJGotoRule implements IEditorActionDelegate {

  // MMa 02/2010 : formatting and javadoc revision
  // MMa 08/2011 : enhanced Call Hierarchy view (changed selection for JJTree node descriptors)

  /** The current editor */
  static JJEditor sJJEditor;

  /** {@inheritDoc} */
  @Override
  public void setActiveEditor(@SuppressWarnings("unused") final IAction aAction,
                              final IEditorPart aTargetEditor) {
    if (aTargetEditor == null) {
      return;
    }
    sJJEditor = (JJEditor) aTargetEditor;
  }

  /** {@inheritDoc} */
  @Override
  public void selectionChanged(@SuppressWarnings("unused") final IAction aAction,
                               @SuppressWarnings("unused") final ISelection aSelection) {
    // not used. The selection is got inside run()
  }

  /**
   * Gets Selection from Editor, searches matching node in AST then select node corresponding text, puts last
   * selection in History.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public void run(@SuppressWarnings("unused") final IAction aAction) {
    ITextSelection selection = (ITextSelection) sJJEditor.getSelectionProvider().getSelection();
    if (selection.getLength() <= 0) {
      selection = selectWord(selection);
    }
    if (!selection.isEmpty()) {
      final String text = selection.getText();
      // search matching node in AST
      final JJNode node = sJJEditor.getJJElements().getNonIdentNorNodeDesc(text);
      if (node != null) {
        sJJEditor.setSelection(node);
      }
    }
  }

  /**
   * Extends Selection to a whole Word (including the '#' for private label identifiers and JJTree node
   * descriptors).<br>
   * Quite like {@link JJHyperlinkDetector#selectWord(IDocument, IRegion)}.
   * 
   * @param aSelection - the selection
   * @return the extended selection
   */
  static public ITextSelection selectWord(final ITextSelection aSelection) {
    final int caretPos = aSelection.getOffset();
    final IDocument doc = sJJEditor.getDocument();
    int startPos, endPos;
    try {
      int pos = caretPos;
      char c;
      while (pos >= 0) {
        c = doc.getChar(pos);
        if (!Character.isJavaIdentifierPart(c) && c != '#') {
          break;
        }
        pos--;
      }
      startPos = pos + 1;
      pos = caretPos;
      final int length = doc.getLength();
      while (pos < length) {
        c = doc.getChar(pos);
        if (!Character.isJavaIdentifierPart(c)) {
          break;
        }
        pos++;
      }
      endPos = pos;
      return new TextSelection(doc, startPos, endPos - startPos);
    } catch (final BadLocationException x) {
      // Do nothing, except returning
    }
    return aSelection;
  }
}