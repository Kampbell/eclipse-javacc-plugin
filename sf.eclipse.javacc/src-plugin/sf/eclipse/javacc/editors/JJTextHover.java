package sf.eclipse.javacc.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ISourceViewer;

import sf.eclipse.javacc.parser.JJNode;

/**
 * ITextHover for the JJEditor / JTBEditor.<br>
 * The information is shown in a text hover over on top of the text viewer's text widget (if the information
 * is null no hover is shown).<br>
 * Text hovers are shown in code sections only for JavaCC non identifier nodes, and they show their
 * definitions.<br>
 * Text hovers are shown in comments sections only for spelling errors, and they do not show anything special
 * (for the moment).
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011
 */
class JJTextHover implements ITextHover, ITextHoverExtension2 {

  // MMa 12/2009 : added getHoverInfo2() and deprecated getHoverInfo ; added spell checking
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 08/2011 : effects of refactoring in JJElements

  /** the current editor */
  private final JJEditor      jJJEditor;
  /** the current source viewer */
  @SuppressWarnings("unused")
  private final ISourceViewer jSourceViewer;
  /** the current content type */
  private final String        jContentType;

  /**
   * Standard constructor.
   * 
   * @param aSourceViewer the source viewer
   * @param aContentType the content type
   * @param aJJEditor the editor
   */
  public JJTextHover(final ISourceViewer aSourceViewer, final String aContentType, final JJEditor aJJEditor) {
    jSourceViewer = aSourceViewer;
    jContentType = aContentType;
    jJJEditor = aJJEditor;
  }

  /**
   * @param aTextViewer the viewer on which the hover popup should be shown
   * @param aHoverRegion the text range in the viewer which is used to determine the hover display information
   * @return the hover popup display information, or <code>null</code> if none available
   * @deprecated @see ITextHover#getHoverInfo(ITextViewer, IRegion)
   */
  @Override
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
  @Override
  public String getHoverInfo2(final ITextViewer aTextViewer, final IRegion aHoverRegion) {
    String hoverInfo = null;
    final IDocument document = aTextViewer.getDocument();
    try {
      //      if (fContentType.equals(UnusedJJDocumentProvider.JJ_CODE)) {
      if (jContentType.equals(IDocument.DEFAULT_CONTENT_TYPE)) {
        // hover info for code regions (grammar)
        String word;
        word = document.get(aHoverRegion.getOffset(), aHoverRegion.getLength());
        final JJElements jjElements = jJJEditor.getJJElements();
        if (!jjElements.isNonIdentNorNodeDescElement(word)) {
          return null;
        }

        final JJNode node = jjElements.getNonIdentNorNodeDesc(word);
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
      //      else if (fContentType.equals(UnusedJJDocumentProvider.JJ_COMMENT)) {
      //        // hover info for comments regions (spelling)
      //        final IAnnotationModel model = fSourceViewer.getAnnotationModel();
      //        final Iterator<?> iter = model.getAnnotationIterator();
      //        while (iter.hasNext()) {
      //          final Object obj = iter.next();
      //          if (obj instanceof SpellingAnnotation) {
      //            final SpellingAnnotation annotation = (SpellingAnnotation) obj;
      //            final int offset = annotation.getSpellingProblem().getOffset();
      //            try {
      //              final int line = fSourceViewer.getDocument().getLineOfOffset(offset);
      //              if (line == document.getLineOfOffset(aHoverRegion.getOffset())) { // same start numbers
      //                hoverInfo = annotation.getText();
      //                break;
      //              }
      //            } catch (final BadLocationException e) {
      //              e.printStackTrace();
      //              return null;
      //            }
      //          }
      //        }
      //        //        return "JJTextHover.getHoverInfo2";
      //      }
    } catch (final BadLocationException e) {
      // e.printStackTrace();
    }
    return hoverInfo;
  }

  /**
   * @see ITextHover#getHoverRegion(ITextViewer, int)
   */
  @Override
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
   * @param aDoc the current document
   * @param aOffset the offset
   * @return the corresponding region
   */
  private static final IRegion findWord(final IDocument aDoc, final int aOffset) {
    int start = -1;
    int end = -1;
    try {
      int pos = aOffset;
      char c;
      while (pos >= 0) {
        c = aDoc.getChar(pos);
        if (!Character.isUnicodeIdentifierPart(c)) {
          break;
        }
        --pos;
      }
      start = pos;
      pos = aOffset;
      final int length = aDoc.getLength();
      while (pos < length) {
        c = aDoc.getChar(pos);
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