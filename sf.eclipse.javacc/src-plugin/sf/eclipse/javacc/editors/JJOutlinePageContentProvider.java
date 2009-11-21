package sf.eclipse.javacc.editors;

import java.io.StringReader;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import sf.eclipse.javacc.parser.JJNode;
import sf.eclipse.javacc.parser.JavaCCParser;
import sf.eclipse.javacc.parser.JavaCCParserTreeConstants;
import sf.eclipse.javacc.parser.Node;

/**
 * Content provider for outline page. Uses JavaCCParser to build the AST used in the Outline
 * 
 * @author Remi Koutcherawy 2003-2009 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009
 */
public class JJOutlinePageContentProvider implements IContentProvider, ITreeContentProvider,
                                         JavaCCParserTreeConstants {

  /*
   * MMa 11/09 : javadoc and formatting revision
   */
  /** the AST node built from the text */
  protected JJNode node;

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  public void dispose() {
    node = null;
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
   *      java.lang.Object, java.lang.Object)
   */
  public void inputChanged(@SuppressWarnings("unused") final Viewer viewer,
                           @SuppressWarnings("unused") final Object oldInput, final Object newInput) {
    if (newInput != null) {
      final IDocument doc = (IDocument) newInput;
      parse(doc.get());
    }
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
   */
  public Object[] getChildren(final Object aObj) {
    if (aObj == null) {
      return null;
    }
    final JJNode nd = (JJNode) aObj;
    // Remove JJTIDENTIFIER nodes
    int n = 0;
    final Node[] children = nd.getChildren();
    if (children == null) {
      return null;
    }
    for (final Node child : children) {
      if (((JJNode) child).getId() != JJTIDENTIFIER) {
        n++;
      }
    }
    final JJNode[] filteredChildren = new JJNode[n];
    int j = 0;
    for (final Node child : children) {
      if (((JJNode) child).getId() != JJTIDENTIFIER) {
        filteredChildren[j++] = (JJNode) child;
      }
    }
    return filteredChildren;
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
   */
  public Object getParent(final Object obj) {
    return obj == null ? null : ((JJNode) obj).jjtGetParent();
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
   */
  public boolean hasChildren(final Object obj) {
    return getChildren(obj) == null ? false : getChildren(obj).length != 0;
  }

  /**
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  public Object[] getElements(@SuppressWarnings("unused") final Object obj) {
    return getChildren(node);
  }

  /**
   * Parse a String to build the AST node (saved in the class member).
   * 
   * @param txt the string to parse
   */
  protected void parse(final String txt) {
    final StringReader in = new StringReader(txt);
    node = JavaCCParser.parse(in);
    in.close();
  }

  /**
   * @return JJNode (The AST root)
   */
  public JJNode getAST() {
    return node;
  }
}
