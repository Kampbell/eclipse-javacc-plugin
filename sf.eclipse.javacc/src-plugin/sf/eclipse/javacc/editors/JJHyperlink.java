package sf.eclipse.javacc.editors;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import sf.eclipse.javacc.parser.JJNode;

/**
 * JavaCC element hyperlink.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
class JJHyperlink implements IHyperlink {

  // MMa 11/2009 : formatting and javadoc revision
  // MMa 02/2010 : formatting and javadoc revision

  /** the region */
  private final IRegion  fRegion;
  /** the editor */
  private final JJEditor fEditor;
  /** the node */
  private final JJNode   fNode;

  /**
   * Creates a new Java element hyperlink.
   * 
   * @param region the region
   * @param editor the editor
   * @param node the node
   */
  public JJHyperlink(final IRegion region, final JJEditor editor, final JJNode node) {
    fRegion = region;
    fEditor = editor;
    fNode = node;
  }

  /**
   * @return the region
   * @see IHyperlink#getHyperlinkRegion()
   */
  public IRegion getHyperlinkRegion() {
    return fRegion;
  }

  /**
   * @see IHyperlink#open()
   */
  public void open() {
    fEditor.setSelection(fNode);
  }

  /**
   * @return the node label
   * @see IHyperlink#getTypeLabel()
   */
  public String getTypeLabel() {
    return fNode.toString();
  }

  /**
   * @return the node text
   * @see IHyperlink#getHyperlinkText()
   */
  public String getHyperlinkText() {
    return fNode.toString();
  }
}