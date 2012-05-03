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

import sf.eclipse.javacc.actions.JJGotoRule;
import sf.eclipse.javacc.parser.JJNode;
import sf.eclipse.javacc.parser.JavaCCParserTreeConstants;

/**
 * JavaCC hyperlink detector. Used in JJSourceViewerConfiguration.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011
 */
public class JJHyperlinkDetector implements IHyperlinkDetector, JavaCCParserTreeConstants {

  // MMa 04/2009 : formatting and javadoc revision
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 08/2011 : effects of refactoring in JJElements
  // MMa 08/2011 : fixed missing hyperlinks

  /** The editor */
  private final JJEditor jJJEditor;

  /**
   * Creates a new JavaCC hyperlink detector.
   * 
   * @param aJJEditor the editor in which to detect the hyperlink
   */
  public JJHyperlinkDetector(final JJEditor aJJEditor) {
    jJJEditor = aJJEditor;
  }

  /**
   * @see IHyperlinkDetector#detectHyperlinks(ITextViewer, IRegion, boolean)
   */
  @Override
  public IHyperlink[] detectHyperlinks(final ITextViewer aTextViewer, final IRegion aRegion,
                                       @SuppressWarnings("unused") final boolean aCanShowMultipleHyperlinks) {
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
    // if not in JJElements don't go further
    final JJElements jjElements = jJJEditor.getJJElements();
    if (!jjElements.isHyperlinkTarget(word)) {
      return null;
    }
    // if JavaCC keyword don't go further
    for (int i = 0; i < JJCodeScanner.sJJkeywords.length; i++) {
      if (word.equals(JJCodeScanner.sJJkeywords[i])) {
        return null;
      }
    }
    // add hyperlink for the word associated with the node and the editor
    final IRegion linkRegion = new Region(textSel.getOffset(), textSel.getLength());
    final JJNode node = jjElements.getHyperlinkTarget(word);
    if (node != null) {
      //      final int ndId = node.getId();
      //      if (ndId == JJTIDENT_BNF_DECL || ndId == JJTIDENT_REG_EXPR_LABEL) {
      //        return null;
      //      }
      final JJHyperlink link = new JJHyperlink(linkRegion, jJJEditor, node);
      return new IHyperlink[] {
        link };
    }
    return null;
  }

  /**
   * Extends Selection to a whole Word (not including the '#' for private label identifiers and JJTree node
   * descriptors).<br>
   * Quite like {@link JJGotoRule#selectWord(ITextSelection)}.
   * 
   * @param aDoc the document
   * @param aSelection the selected text
   * @return the extended selection (up to a whole word)
   */
  public static final ITextSelection selectWord(final IDocument aDoc, final IRegion aSelection) {
    final int caretPos = aSelection.getOffset();
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
      // so nothing, except returning
    }
    return null;
  }
}
