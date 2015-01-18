package sf.eclipse.javacc.editors;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import sf.eclipse.javacc.parser.JJNode;

/**
 * JavaCC element hyperlink.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 */
class Hyperlink implements IHyperlink {

  // MMa 11/2009 : formatting and javadoc revision
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 10/2012 : renamed

  /** The region */
  private final IRegion  jRegion;
  /** The editor */
  private final JJEditor jEditor;
  /** The node */
  private final JJNode   jNode;

  /**
   * Creates a new Java element hyperlink.
   * 
   * @param aRegion - the region
   * @param aJJEditor - the editor
   * @param aJJNode - the node
   */
  public Hyperlink(final IRegion aRegion, final JJEditor aJJEditor, final JJNode aJJNode) {
    jRegion = aRegion;
    jEditor = aJJEditor;
    jNode = aJJNode;
  }

  /** {@inheritDoc} */
  @Override
  public final IRegion getHyperlinkRegion() {
    return jRegion;
  }

  /** {@inheritDoc} */
  @Override
  public final void open() {
    jEditor.selectNode(jNode);
  }

  /** {@inheritDoc} */
  @Override
  public final String getTypeLabel() {
    return jNode.toString();
  }

  /** {@inheritDoc} */
  @Override
  public final String getHyperlinkText() {
    return jNode.toString();
  }
}