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
  private final IRegion  jRegion;
  /** the editor */
  private final JJEditor jJJEditor;
  /** the node */
  private final JJNode   jJJNode;

  /**
   * Creates a new Java element hyperlink.
   * 
   * @param aRegion the region
   * @param aJJEditor the editor
   * @param aJJNode the node
   */
  public JJHyperlink(final IRegion aRegion, final JJEditor aJJEditor, final JJNode aJJNode) {
    jRegion = aRegion;
    jJJEditor = aJJEditor;
    jJJNode = aJJNode;
  }

  /**
   * @return the region
   * @see IHyperlink#getHyperlinkRegion()
   */
  @Override
  public IRegion getHyperlinkRegion() {
    return jRegion;
  }

  /**
   * @see IHyperlink#open()
   */
  @Override
  public void open() {
    jJJEditor.setSelection(jJJNode);
  }

  /**
   * @return the node label
   * @see IHyperlink#getTypeLabel()
   */
  @Override
  public String getTypeLabel() {
    return jJJNode.toString();
  }

  /**
   * @return the node text
   * @see IHyperlink#getHyperlinkText()
   */
  @Override
  public String getHyperlinkText() {
    return jJJNode.toString();
  }
}