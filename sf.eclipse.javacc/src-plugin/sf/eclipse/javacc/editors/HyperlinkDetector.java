package sf.eclipse.javacc.editors;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;

import sf.eclipse.javacc.parser.JJNode;

/**
 * JavaCC hyperlink detector.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 */
class HyperlinkDetector implements IHyperlinkDetector {

  // MMa 04/2009 : formatting and javadoc revision
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 08/2011 : effects of refactoring in Elements
  // MMa 08/2011 : fixed missing hyperlinks
  // BF  05/2012 : rename and change location of JavaCC keyword table
  // MMa 10/2012 : removed useless check on JavaCC keywords ; renamed
  // MMa 11/2014 : modified some modifiers

  /** The editor */
  private final JJEditor jEditor;

  /**
   * Creates a new JavaCC hyperlink detector.
   * 
   * @param aJJEditor - the editor in which to detect the hyperlink
   */
  public HyperlinkDetector(final JJEditor aJJEditor) {
    jEditor = aJJEditor;
  }

  /** {@inheritDoc} */
  @Override
  public IHyperlink[] detectHyperlinks(final ITextViewer aTextViewer, final IRegion aRegion,
                                       final boolean aCanShowMultipleHyperlinks) {
    if (aRegion == null) {
      return null;
    }

    final IDocument document = aTextViewer.getDocument();
    if (document == null) {
      return null;
    }

    final ITextSelection textSel = JJEditor.selectWord(document, aRegion);
    if (textSel == null) {
      return null;
    }

    final String word = textSel.getText();
    // if not in Elements don't go further
    final Elements jElements = jEditor.getElements();
    if (!jElements.isHyperlinkTarget(word)) {
      return null;
    }
    // add hyperlink for the word associated with the node and the editor
    final IRegion linkRegion = new Region(textSel.getOffset(), textSel.getLength());
    final JJNode node = jElements.getHyperlinkTarget(word);
    if (node != null) {
      final Hyperlink link = new Hyperlink(linkRegion, jEditor, node);
      return new IHyperlink[] {
        link };
    }
    return null;
  }

}
