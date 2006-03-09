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
 * JavaCC hyperlink detector
 * Used in JJSourceViewerConfiguration 
 *  
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */
public class JJHyperlinkDetector implements IHyperlinkDetector {
  private JJEditor editor;
  
  /**
   * Creates a new JavaCC hyperlink detector.
   * 
   * @param the editor in which to detect the hyperlink
   */
  public JJHyperlinkDetector(JJEditor editor) {
    this.editor = editor;
  }
  
  /*
   * @see org.eclipse.jface.text.hyperlink.IHyperlinkDetector#detectHyperlinks(org.eclipse.jface.text.ITextViewer,
   *      org.eclipse.jface.text.IRegion, boolean)
   */
  public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region,
      boolean canShowMultipleHyperlinks) {
    if (region == null)
      return null;
    
    IDocument document= textViewer.getDocument();
    if (document == null)
      return null;
    
    ITextSelection textSel = selectWord(document, region);
    if (textSel == null)
      return null;
    
    String word = textSel.getText();
    if (!JJElements.isElement(word))
      return null;
    
    IRegion linkRegion = new Region(textSel.getOffset(), textSel.getLength());
    JJNode node = JJElements.getNode(word);
    JJHyperlink link = new JJHyperlink(linkRegion, editor, node);
    return new IHyperlink[] { link };
  }
  
  /**
   * Extend Selection to a whole Word
   */
  public static final ITextSelection selectWord(IDocument doc, IRegion sel) {
    int caretPos = sel.getOffset();
    int startPos, endPos;
    try {
      int pos = caretPos;
      char c;
      while (pos >= 0) {
        c = doc.getChar(pos);
        if (!Character.isJavaIdentifierPart(c))
          break;
        pos--;
      }
      startPos = pos + 1;
      pos = caretPos;
      int length = doc.getLength();
      while (pos < length) {
        c = doc.getChar(pos);
        if (!Character.isJavaIdentifierPart(c))
          break;
        pos++;
      }
      endPos = pos;
      return new TextSelection(doc, startPos, endPos - startPos);
    } catch (BadLocationException x) {
      // Do nothing, except returning
    }
    return null;
  }
}

