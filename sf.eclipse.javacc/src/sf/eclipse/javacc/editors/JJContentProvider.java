package sf.eclipse.javacc.editors;

import java.io.StringReader;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import sf.eclipse.javacc.parser.JavaCCParser;
import sf.eclipse.javacc.parser.Node;
import sf.eclipse.javacc.parser.SimpleNode;

/**
 * Content provider for outline page.
 * Uses JavaCCParser to build the AST used in the Outline
 * 
 * @author Remi Koutcherawy 2003 
 * GPL Licence www.gnu.org/licenses/gpl.txt
 */
public class JJContentProvider
  implements IContentProvider, ITreeContentProvider {

  protected SimpleNode node;

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  public void dispose() {
    node=null;
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
    SimpleNode nod = (SimpleNode) obj;
    Node[] nd = nod.getChildren();
    return nd;
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
   */
  public Object getParent(Object obj) {
    if (obj == null)
      return obj;
    SimpleNode simpleNode = (SimpleNode) obj;
    return simpleNode.jjtGetParent();
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
   */
  public boolean hasChildren(Object obj) {
    if (obj == null)
      return false;
    SimpleNode simpleNode = (SimpleNode) obj;
    return simpleNode.jjtGetNumChildren() != 0;
  }

  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  public Object[] getElements(Object obj) {
    if (obj == null)
      return null;
    return getChildren(node);
  }
  
  /**
   * Parse a String to build the AST saved in SimpleNode
   * @param String txt 
   */
  protected void parse(String txt) {
    StringReader in = new StringReader(txt);
    SimpleNode simpleNode = null;
    try {
    	simpleNode = JavaCCParser.parse(in);
    } catch (Throwable e) {
      // ignore exception
    }
    if (simpleNode != null)
      this.node = simpleNode;
    in.close();
  }

  /**
   * @return SimpleNode (The AST root)
   */
  public SimpleNode getAST() {
    return node;
  }
}
