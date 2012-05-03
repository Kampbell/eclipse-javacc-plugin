package sf.eclipse.javacc.editors;

import java.io.StringReader;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import sf.eclipse.javacc.parser.JJNode;
import sf.eclipse.javacc.parser.JavaCCParser;
import sf.eclipse.javacc.parser.JavaCCParserTreeConstants;
import sf.eclipse.javacc.parser.Node;

/**
 * Content provider for outline page. Uses JavaCCParser to build the AST used in the Outline.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011
 */
public class JJOutlinePageContentProvider implements IContentProvider, ITreeContentProvider,
                                         JavaCCParserTreeConstants {

  // MMa 11/2009 : javadoc and formatting revision
  // MMa 08/2011 : modified getChildren() to add identifiers and JJT nodes in Outline view

  /** the AST node built from the text */
  protected JJNode jJJNode;

  /**
   * @see IContentProvider#dispose()
   */
  @Override
  public void dispose() {
    jJJNode = null;
  }

  /**
   * @see IContentProvider#inputChanged(Viewer, Object, Object)
   */
  @Override
  public void inputChanged(@SuppressWarnings("unused") final Viewer aViewer,
                           @SuppressWarnings("unused") final Object aOldInput, final Object aNewInput) {
    if (aNewInput != null) {
      final IDocument doc = (IDocument) aNewInput;
      parse(doc.get());
    }
  }

  /**
   * @see ITreeContentProvider#getChildren(Object)
   */
  @Override
  public Object[] getChildren(final Object aObj) {
    if (aObj == null) {
      return null;
    }
    final JJNode nd = (JJNode) aObj;
    final Node[] children = nd.getChildren();
    if (children == null) {
      return null;
    }
    int len = children.length;
    int déb = 0;
    final int id = nd.getId();
    if (id == JJTREGEXPR_SPEC) {
      // remove the node descriptor if there is one
      final int chId = ((JJNode) children[0]).getId();
      if (len > 0 && (chId == JJTIDENT_REG_EXPR_LABEL || chId == JJTIDENT_REG_EXPR_PRIVATE_LABEL)) {
        // remove the first child (as it has the same name)
        len--;
        déb = 1;
      }
    }
    else if (id == JJTCLASSORINTERFACEDECLARATION || id == JJTMETHODDECLARATION || id == JJTPARSER_BEGIN) {
      // remove the first child (as it has the same name)
      len--;
      déb = 1;
    }
    else if (id == JJTBNF_PRODUCTION) {
      // remove the first child (as it has the same name)
      len--;
      déb = 1;
      if (len > 0 && (((JJNode) children[1]).getId() == JJTNODE_DESC_BNF_DECL)) {
        // remove the node descriptor if there is one
        len--;
        déb = 2;
      }
    }
    if (len <= 0) {
      return null;
    }
    final JJNode[] filteredChildren = new JJNode[len];
    for (int j = 0; j < len; j++, déb++) {
      filteredChildren[j] = (JJNode) children[déb];
    }
    return filteredChildren;
  }

  /**
   * @see ITreeContentProvider#getParent(Object)
   */
  @Override
  public Object getParent(final Object aObj) {
    return aObj == null ? null : ((JJNode) aObj).jjtGetParent();
  }

  /**
   * @see ITreeContentProvider#hasChildren(Object)
   */
  @Override
  public boolean hasChildren(final Object aObj) {
    return getChildren(aObj) == null ? false : getChildren(aObj).length != 0;
  }

  /**
   * @see IStructuredContentProvider#getElements(Object)
   */
  @Override
  public Object[] getElements(@SuppressWarnings("unused") final Object aObj) {
    return getChildren(jJJNode);
  }

  /**
   * Parse a String to build the AST node (saved in the class member).
   * 
   * @param aTxt the string to parse
   */
  protected void parse(final String aTxt) {
    final StringReader in = new StringReader(aTxt);
    jJJNode = JavaCCParser.parse(in);
    in.close();
  }

  /**
   * @return JJNode (The AST root)
   */
  public JJNode getAST() {
    return jJJNode;
  }
}
