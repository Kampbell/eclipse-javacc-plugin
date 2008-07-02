package sf.eclipse.javacc.editors;

import org.eclipse.jface.text.*;
import sf.eclipse.javacc.parser.JJNode;

/**
 * Annotation hover for text in JJEditor
 * 
 * @author Remi Koutcherawy 2003-2008
 * CeCILL Licence http://www.cecill.info/index.en.html
 */
class JJTextHover  implements ITextHover {
  
  public JJTextHover() {}

  /* (non-Javadoc)
   * @see org.eclipse.jface.text.ITextHover#getHoverInfo(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
   */
  public String getHoverInfo(ITextViewer textViewer, IRegion region) {
    String info = null;
    IDocument doc= textViewer.getDocument();
    try {
      String word;
      word = doc.get(region.getOffset(), region.getLength());
      if (!JJElements.isElement(word))
        return null;

      JJNode node = JJElements.getNode(word);
      // If the  node is on the same line as the word under the mouse
      // Definition is over itself : do not show it
      if (node.getBeginLine()-1 == doc.getLineOfOffset(region.getOffset()))
        return null;
      
      // Get the definition
      int start = doc.getLineOffset(node.getBeginLine() - 1);
      int end = doc.getLineOffset(node.getEndLine());
      if (start > end)
        end = start;
      int length = end - start;
      info = doc.get(start, length);
    }
    catch (BadLocationException e) {
      // e.printStackTrace();
    }
    return info;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.text.ITextHover#getHoverRegion(org.eclipse.jface.text.ITextViewer, int)
   */
  public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
    IDocument document= textViewer.getDocument();
    IRegion region = findWord(document, offset);
    if (region.getLength() < 1)
      return null;
    return region;
  }
  
  /**
   * Extend to a whole Word
   */
  private static final IRegion findWord(IDocument doc, int offset) {
    int start = -1;
    int end = -1;
    try {
      int pos = offset;
      char c;
      while (pos >= 0) {
        c = doc.getChar(pos);
        if (!Character.isUnicodeIdentifierPart(c))
          break;
        --pos;
      }
      start = pos;
      pos = offset;
      int length = doc.getLength();
      while (pos < length) {
        c = doc.getChar(pos);
        if (!Character.isUnicodeIdentifierPart(c))
          break;
        ++pos;
      }
      end = pos;
    }
    catch (BadLocationException x) {}
    if (start > -1 && end > -1) {
      if (start == offset && end == offset)
        return new Region(offset, 0);
      else if (start == offset)
        return new Region(start, end - start);
      else return new Region(start + 1, end - start - 1);
    }
    return null;
  }
}