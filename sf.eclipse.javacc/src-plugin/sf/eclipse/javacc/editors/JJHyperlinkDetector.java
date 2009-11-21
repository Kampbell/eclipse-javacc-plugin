package sf.eclipse.javacc.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;

import sf.eclipse.javacc.parser.JJNode;

/**
 * JavaCC hyperlink detector Used in JJSourceViewerConfiguration
 * 
 * @author Remi Koutcherawy 2003-2009 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009
 */
public class JJHyperlinkDetector implements IHyperlinkDetector {

  // MMa 04/09 : formatting and javadoc revision

  /** the editor */
  private final JJEditor editor;

  /**
   * Creates a new JavaCC hyperlink detector.
   * 
   * @param aEditor the editor in which to detect the hyperlink
   */
  public JJHyperlinkDetector(final JJEditor aEditor) {
    editor = aEditor;
  }

  /**
   * @see org.eclipse.jface.text.hyperlink.IHyperlinkDetector#detectHyperlinks(org.eclipse.jface.text.ITextViewer,
   *      org.eclipse.jface.text.IRegion, boolean)
   */
  public IHyperlink[] detectHyperlinks(final ITextViewer aTextViewer, final IRegion aRegion,
                                       @SuppressWarnings("unused") final boolean canShowMultipleHyperlinks) {
    if (aRegion == null) {
      return null;
    }

    final IDocument document = aTextViewer.getDocument();
    if (document == null) {
      return null;
    }

    final ITextSelection textSel = selectWord(document, aRegion);
    if (textSel == null) {
      return null;
    }

    final String word = textSel.getText();
    // If not in JJElements don't go further
    final JJElements jjElements = editor.getJJElements();
    if (!jjElements.isNonIdentifierElement(word)) {
      return null;
    }
    // If JavaCC keyword don't go further
    for (int i = 0; i < JJCodeScanner.fgJJkeywords.length; i++) {
      if (word.equals(JJCodeScanner.fgJJkeywords[i])) {
        return null;
      }
    }
    // Add hyper link for the word associated with the node and the editor
    final IRegion linkRegion = new Region(textSel.getOffset(), textSel.getLength());
    final JJNode node = jjElements.getNonIdentifierNode(word);
    final JJHyperlink link = new JJHyperlink(linkRegion, editor, node);
    return new IHyperlink[] {
      link };
  }

  /**
   * Extens the selection to a whole word.
   * 
   * @param aDoc the document
   * @param aSel the selected text
   * @return the extended selection (up to a whole word)
   */
  public static final ITextSelection selectWord(final IDocument aDoc, final IRegion aSel) {
    final int caretPos = aSel.getOffset();
    int startPos, endPos;
    try {
      int pos = caretPos;
      char c;
      while (pos >= 0) {
        c = aDoc.getChar(pos);
        if (!Character.isJavaIdentifierPart(c)) {
          break;
        }
        pos--;
      }
      startPos = pos + 1;
      pos = caretPos;
      final int length = aDoc.getLength();
      while (pos < length) {
        c = aDoc.getChar(pos);
        if (!Character.isJavaIdentifierPart(c)) {
          break;
        }
        pos++;
      }
      endPos = pos;
      return new TextSelection(aDoc, startPos, endPos - startPos);
    } catch (final BadLocationException x) {
      // Do nothing, except returning
    }
    return null;
  }
}
