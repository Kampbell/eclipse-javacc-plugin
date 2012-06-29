package sf.eclipse.javacc.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;

/**
 * Double click strategy aware of JavaCC identifier syntax rules. Allows the viewer to select the JavaCC
 * identifier around the first character of the selected text.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012
 */
public class JJDoubleClickStrategy implements ITextDoubleClickStrategy {

  // MMa 12/2009 : javadoc and formatting revision ; some refactoring
  // MMa 02/2010 : formatting and javadoc revision

  /** {@inheritDoc} */
  @Override
  public void doubleClicked(final ITextViewer aTextViewer) {
    final int selectionStartPos = aTextViewer.getSelectedRange().x;
    if (selectionStartPos < 0) {
      return;
    }
    selectWord(aTextViewer, selectionStartPos);
    return;
  }

  /**
   * @param aTextViewer - the current viewer
   * @param aCharPos - a character position
   * @return the whole word around the character position
   */
  public boolean selectWord(final ITextViewer aTextViewer, final int aCharPos) {
    final IDocument doc = aTextViewer.getDocument();
    int startPos, endPos;
    try {
      int pos = aCharPos;
      char c;
      while (pos >= 0) {
        c = doc.getChar(pos);
        if (!Character.isJavaIdentifierPart(c)) {
          break;
        }
        --pos;
      }
      startPos = pos;
      pos = aCharPos;
      final int length = doc.getLength();
      while (pos < length) {
        c = doc.getChar(pos);
        if (!Character.isJavaIdentifierPart(c)) {
          break;
        }
        ++pos;
      }
      endPos = pos;
      selectRange(aTextViewer, startPos, endPos);
      return true;
    } catch (final BadLocationException x) {
      // do nothing, except returning false
    }
    return false;
  }

  /**
   * @param aTextViewer - the current viewer
   * @param aStartPos - the starting position
   * @param aEndPos - the ending position
   */
  private void selectRange(final ITextViewer aTextViewer, final int aStartPos, final int aEndPos) {
    final int offset = aStartPos + 1;
    final int length = aEndPos - offset;
    aTextViewer.setSelectedRange(offset, length);
  }
}