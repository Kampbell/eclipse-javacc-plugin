package sf.eclipse.javacc.editors;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import sf.eclipse.javacc.parser.JJNode;

/**
 * JavaCC element hyperlink.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012
 */
class JJHyperlink implements IHyperlink {

  // MMa 11/2009 : formatting and javadoc revision
  // MMa 02/2010 : formatting and javadoc revision

  /** The region */
  private final IRegion  jRegion;
  /** The editor */
  private final JJEditor jJJEditor;
  /** The node */
  private final JJNode   jJJNode;

  /**
   * Creates a new Java element hyperlink.
   * 
   * @param aRegion - the region
   * @param aJJEditor - the editor
   * @param aJJNode - the node
   */
  public JJHyperlink(final IRegion aRegion, final JJEditor aJJEditor, final JJNode aJJNode) {
    jRegion = aRegion;
    jJJEditor = aJJEditor;
    jJJNode = aJJNode;
  }

  /**
   * Return the region.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public IRegion getHyperlinkRegion() {
    return jRegion;
  }

  /** {@inheritDoc} */
  @Override
  public void open() {
    jJJEditor.setSelection(jJJNode);
  }

  /**
   * Return the node label.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public String getTypeLabel() {
    return jJJNode.toString();
  }

  /**
   * Return the node text.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public String getHyperlinkText() {
    return jJJNode.toString();
  }
}