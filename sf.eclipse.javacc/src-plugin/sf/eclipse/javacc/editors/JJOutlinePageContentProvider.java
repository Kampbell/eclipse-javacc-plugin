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
 * Content provider for outline page.
 * Uses JavaCCParser to build the AST used in the Outline
 * 
 * @author Remi Koutcherawy 2003-2009
 * CeCILL license http://www.cecill.info/index.en.html
 */
public class JJOutlinePageContentProvider
  implements IContentProvider, ITreeContentProvider, JavaCCParserTreeConstants {

  protected JJNode node;

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  public void dispose() {
    node = null;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
   */
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    if (newInput != null) {   
      IDocument doc = (IDocument)newInput;
      parse(doc.get());
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
   */
  public Object[] getChildren(Object obj) {
    if (obj == null)
      return null;
    JJNode node = (JJNode) obj;
    // Remove JJTIDENTIFIER nodes
    int n = 0;
    Node[] children = node.getChildren();
    if (children == null)
      return null;
    for (int i = 0; i < children.length; i++)
      if (((JJNode)children[i]).getId() != JJTIDENTIFIER)
        n++;
    JJNode[] filteredChildren = new JJNode[n];
    for (int i = 0, j = 0; i < children.length; i++)
      if (((JJNode)children[i]).getId() != JJTIDENTIFIER)
        filteredChildren[j++] = (JJNode)children[i];
    return filteredChildren;
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
   */
  public Object getParent(Object obj) {
    return obj == null ? null : ((JJNode)obj).jjtGetParent();
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
   */
  public boolean hasChildren(Object obj) {
    return getChildren(obj) ==  null ? false : getChildren(obj).length != 0;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  public Object[] getElements(Object obj) {
    return getChildren(node);
  }
  
  /**
   * Parse a String to build the AST saved in JJNode
   * @param String txt 
   */
  protected void parse(String txt) {
    StringReader in = new StringReader(txt);
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
