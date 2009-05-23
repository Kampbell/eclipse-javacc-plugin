package sf.eclipse.javacc.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;

/**
 * Double click strategy aware of JavaCC identifier syntax rules.
 * 
 * @author Remi Koutcherawy 2003-2006
 * CeCILL license http://www.cecill.info/index.en.html
 */
public class JJDoubleClickStrategy implements ITextDoubleClickStrategy {
  protected ITextViewer fText;

  public JJDoubleClickStrategy() {
    super();
  }
  
  public void doubleClicked(ITextViewer part) {
    int pos = part.getSelectedRange().x;
    if (pos < 0)
      return;
    fText = part;
    selectWord(pos);
    return;
  }

  public boolean selectWord(int caretPos) {
    IDocument doc = fText.getDocument();
    int startPos, endPos;
    try {
      int pos = caretPos;
      char c;
      while (pos >= 0) {
        c = doc.getChar(pos);
        if (!Character.isJavaIdentifierPart(c))
          break;
        --pos;
      }
      startPos = pos;
      pos = caretPos;
      int length = doc.getLength();
      while (pos < length) {
        c = doc.getChar(pos);
        if (!Character.isJavaIdentifierPart(c))
          break;
        ++pos;
      }
      endPos = pos;
      selectRange(startPos, endPos);
      return true;
    } catch (BadLocationException x)
    {
        // Do nothing, except returning false
    }
    return false;
  }

  private void selectRange(int startPos, int stopPos) {
    int offset = startPos + 1;
    int length = stopPos - offset;
    fText.setSelectedRange(offset, length);
  }
}