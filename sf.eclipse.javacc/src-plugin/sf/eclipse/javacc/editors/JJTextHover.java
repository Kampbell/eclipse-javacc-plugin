package sf.eclipse.javacc.editors;

import java.util.Iterator;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.spelling.SpellingAnnotation;

import sf.eclipse.javacc.parser.JJNode;

/**
 * ITextHover for the JJEditor / JTBEditor.<br>
 * The information is shown in a text hover over on top of the text viewer's text widget (if the information
 * is null no hover is shown).<br>
 * Text hovers are shown in code sections only for JavaCC non identifier nodes, and they show their
 * definitions.<br>
 * Text hovers are shown in comments sections only for spelling errors, and they show TODO .
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
class JJTextHover implements ITextHover, ITextHoverExtension2 {

  // MMa 12/2009 : added getHoverInfo2() and deprecated getHoverInfo ; added spell checking
  // MMa 02/2010 : formatting and javadoc revision

  /** the current editor */
  private final JJEditor      fEditor;
  /** the current source viewer */
  private final ISourceViewer fSourceViewer;
  /** the current content type */
  private final String        fContentType;

  /**
   * Standard constructor.
   * 
   * @param aSourceViewer the source viewer
   * @param aContentType the content type
   * @param aEditor the editor
   */
  public JJTextHover(final ISourceViewer aSourceViewer, final String aContentType, final JJEditor aEditor) {
    fSourceViewer = aSourceViewer;
    fContentType = aContentType;
    fEditor = aEditor;
  }

  /**
   * @param aTextViewer the viewer on which the hover popup should be shown
   * @param aHoverRegion the text range in the viewer which is used to determine the hover display information
   * @return the hover popup display information, or <code>null</code> if none available
   * @deprecated @see ITextHover#getHoverInfo(ITextViewer, IRegion)
   */
  @SuppressWarnings( {
      "deprecation", "dep-ann" })
  public String getHoverInfo(final ITextViewer aTextViewer, final IRegion aHoverRegion) {
    return getHoverInfo2(aTextViewer, aHoverRegion);
  }

  /**
   * (Old {@link #getHoverInfo(ITextViewer, IRegion)}).
   * 
   * @see ITextHoverExtension2#getHoverInfo2(ITextViewer, IRegion)
   */
  public String getHoverInfo2(final ITextViewer aTextViewer, final IRegion aHoverRegion) {
    String hoverInfo = null;
    final IDocument document = aTextViewer.getDocument();
    try {
      if (fContentType.equals(JJDocumentProvider.JJ_CODE)) {
        //      if (fContentType.equals(IDocument.DEFAULT_CONTENT_TYPE)) {
        // hover info for code regions (grammar)
        String word;
        word = document.get(aHoverRegion.getOffset(), aHoverRegion.getLength());
        final JJElements jjElements = fEditor.getJJElements();
        if (!jjElements.isNonIdentifierElement(word)) {
          return null;
        }

        final JJNode node = jjElements.getNonIdentifierNode(word);
        // If the  node is on the same line as the word under the mouse
        // Definition is over itself : do not show it
        if (node.getBeginLine() - 1 == document.getLineOfOffset(aHoverRegion.getOffset())) {
          return null;
        }

        // Get the definition
        final int start = document.getLineOffset(node.getBeginLine() - 1);
        int end = document.getLineOffset(node.getEndLine());
        if (start > end) {
          end = start;
        }
        final int length = end - start;
        hoverInfo = document.get(start, length);
      }
      else if (fContentType.equals(JJDocumentProvider.JJ_COMMENT)) {
        //      else if (fContentType.equals(IJavaPartitions.JAVA_DOC)
        //               || fContentType.equals(IJavaPartitions.JAVA_MULTI_LINE_COMMENT)
        //               || fContentType.equals(IJavaPartitions.JAVA_SINGLE_LINE_COMMENT)) {
        // hover info for comments regions (spelling)
        final IAnnotationModel model = fSourceViewer.getAnnotationModel();
        final Iterator<?> iter = model.getAnnotationIterator();
        while (iter.hasNext()) {
          final Object obj = iter.next();
          if (obj instanceof SpellingAnnotation) {
            final SpellingAnnotation annotation = (SpellingAnnotation) obj;
            final int offset = annotation.getSpellingProblem().getOffset();
            try {
              final int line = fSourceViewer.getDocument().getLineOfOffset(offset);
              if (line == document.getLineOfOffset(aHoverRegion.getOffset())) { // same start numbers
                hoverInfo = annotation.getText();
                break;
              }
            } catch (final BadLocationException e) {
              e.printStackTrace();
              return null;
            }
          }
        }
        //        return "JJTextHover.getHoverInfo2";
      }
    } catch (final BadLocationException e) {
      // e.printStackTrace();
    }
    return hoverInfo;
  }

  /**
   * @see ITextHover#getHoverRegion(ITextViewer, int)
   */
  public IRegion getHoverRegion(final ITextViewer aTextViewer, final int aOffset) {
    final IDocument document = aTextViewer.getDocument();
    final IRegion region = findWord(document, aOffset);
    if (region.getLength() < 1) {
      return null;
    }
    return region;
  }

  /**
   * Extends the character at a given offset to a whole word.
   * 
   * @param doc the current document
   * @param aOffset the offset
   * @return the corresponding region
   */
  private static final IRegion findWord(final IDocument doc, final int aOffset) {
    int start = -1;
    int end = -1;
    try {
      int pos = aOffset;
      char c;
      while (pos >= 0) {
        c = doc.getChar(pos);
        if (!Character.isUnicodeIdentifierPart(c)) {
          break;
        }
        --pos;
      }
      start = pos;
      pos = aOffset;
      final int length = doc.getLength();
      while (pos < length) {
        c = doc.getChar(pos);
        if (!Character.isUnicodeIdentifierPart(c)) {
          break;
        }
        ++pos;
      }
      end = pos;
    } catch (final BadLocationException e) {
      //      e.printStackTrace();
    }
    if (start > -1 && end > -1) {
      if (start == aOffset && end == aOffset) {
        return new Region(aOffset, 0);
      }
      else if (start == aOffset) {
        return new Region(start, end - start);
      }
      else {
        return new Region(start + 1, end - start - 1);
      }
    }
    return null;
  }
}