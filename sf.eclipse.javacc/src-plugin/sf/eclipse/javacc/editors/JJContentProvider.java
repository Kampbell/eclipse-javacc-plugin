package sf.eclipse.javacc.editors;

import java.io.StringReader;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import sf.eclipse.javacc.parser.JJNode;
import sf.eclipse.javacc.parser.JavaCCParser;

/**
 * Content provider for outline page.
 * Uses JavaCCParser to build the AST used in the Outline
 * 
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */
public class JJContentProvider
  implements IContentProvider, ITreeContentProvider {

  protected JJNode node;

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
    JJNode node = (JJNode) obj;
    return node.getChildren();
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
   */
  public Object getParent(Object obj) {
    if (obj == null)
      return obj;
    JJNode node = (JJNode) obj;
    return node.jjtGetParent();
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
   */
  public boolean hasChildren(Object obj) {
    if (obj == null)
      return false;
    JJNode node = (JJNode) obj;
    return node.jjtGetNumChildren() != 0;
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
