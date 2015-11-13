package sf.eclipse.javacc.editors;

import static sf.eclipse.javacc.parser.JavaCCParserTreeConstants.*;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import sf.eclipse.javacc.parser.JJNode;
import sf.eclipse.javacc.parser.Node;

/**
 * Tree content provider for content Outline Page.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 * @author Bill Fenlason 2012
 */
class OutlineContentProvider implements ITreeContentProvider {

  // MMa 11/2009 : javadoc and formatting revision
  // MMa 08/2011 : modified getChildren() to add identifiers and JJT nodes in Outline Page
  // BF  06/2012 : removed redundant super interface to prevent warning
  // MMa 10/2012 : used static import ; adapted to modifications in grammar nodes and new nodes ; renamed ;
  //               added ability to reveal a node ; moved parsing and root node to JJEditor ; renamed
  // MMa 10/2014 : removed the reference to OutlinePage
  // MMa 11/2014 : fixed in getChildren case array's length is zero

  /** The last AST root node passed as input (to enable detect changes) */
  private JJNode jAstRoot;

  /** {@inheritDoc} */
  @Override
  public void dispose() {
    // nothing done
  }

  /** {@inheritDoc} */
  @Override
  public final void inputChanged(@SuppressWarnings("unused") final Viewer aViewer,
                                 @SuppressWarnings("unused") final Object aOldInput, final Object aNewInput) {
    jAstRoot = (JJNode) aNewInput;
  }

  /** {@inheritDoc} */
  @Override
  public Object getParent(final Object aObj) {
    final Node parent = aObj == null ? null : ((JJNode) aObj).jjtGetParent();
    return parent;
  }

  /** {@inheritDoc} */
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
    if (len == 0) {
      return null;
    }
    int deb = 0;
    final int id = nd.getId();
    switch (id) {
      case JJTMETHODDECL:
      case JJTCONSTRDECL:
        // don't show deeper
        return null;
      case JJTBNF_PROD:
        // don't need the JJTIDENT_BNF_DECL, JJTNODE_DESC_BNF_DECL and JJTBNF_PROD_JAVA_BLOCK levels
        // so keep only the last child JJTBNF_PROD_EXP_BLOCK, and return its children if any except
        // the JJTEXP_UNIT_JAVA_BLOCK
        final Node[] bpebChildren = ((JJNode) children[len - 1]).getChildren();
        if (bpebChildren == null) {
          return null;
        }
        len = bpebChildren.length;
        int newLen = len;
        for (int j = 0; j < len; j++) {
          if (((JJNode) bpebChildren[j]).getId() == JJTEXP_UNIT_JAVA_BLOCK) {
            newLen--;
          }
        }
        if (newLen == 0) {
          return null;
        }
        JJNode[] filteredChildren = new JJNode[newLen];
        int k = 0;
        for (int j = 0; j < len; j++) {
          if (((JJNode) bpebChildren[j]).getId() != JJTEXP_UNIT_JAVA_BLOCK) {
            filteredChildren[k++] = (JJNode) bpebChildren[j];
          }
        }
        return filteredChildren;
      case JJTREGULAR_EXPR_PROD:
        // don't need the JJTREGEXPR_KIND and JJTREG_EXPR_PROD_BLOCK levels,
        // so take the children of JJTREG_EXPR_PROD_BLOCK, which are JJTREGEXPR_SPEC
        if (children.length < 2) {
          return null;
        }
        return ((JJNode) children[1]).getChildren();
      case JJTREGEXPR_SPEC:
        // remove the first child (as it has the same name) and the JJTREGEXPR_SPEC_JAVA_BLOCK
        newLen = len - 1;
        if (newLen <= 0) {
          return null;
        }
        for (int j = 1; j < len; j++) {
          if (((JJNode) children[j]).getId() == JJTREGEXPR_SPEC_JAVA_BLOCK) {
            newLen--;
          }
        }
        filteredChildren = new JJNode[newLen];
        k = 0;
        for (int j = 1; j < len; j++) {
          if (((JJNode) children[j]).getId() != JJTREGEXPR_SPEC_JAVA_BLOCK) {
            filteredChildren[k++] = (JJNode) children[j];
          }
        }
        return filteredChildren;
      case JJTPARSER_BEGIN:
      case JJTCLAORINTDECL:
      case JJTENUMDECL:
      case JJTANNOTTYPEDECL:
        // remove the first child (as it has the same name)
        len--;
        deb = 1;
        if (len <= 0) {
          return null;
        }
        filteredChildren = new JJNode[len];
        for (int j = 0; j < len; j++, deb++) {
          filteredChildren[j] = (JJNode) children[deb];
        }
        return filteredChildren;
      default:
        return children;
    }
  }

  /** {@inheritDoc} */
  @Override
  public final boolean hasChildren(final Object aObj) {
    return getChildren(aObj) == null ? false : getChildren(aObj).length != 0;
  }

  /** {@inheritDoc} */
  @Override
  public final Object[] getElements(@SuppressWarnings("unused") final Object aObj) {
    return getChildren(getAstRoot());
  }

  /**
   * @return the last AST root node passed as input
   */
  public final JJNode getAstRoot() {
    return jAstRoot;
  }

}
