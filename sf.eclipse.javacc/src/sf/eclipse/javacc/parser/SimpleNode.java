/**
 *  Modified SimpleNode.java 
 */

package sf.eclipse.javacc.parser;

public class SimpleNode implements Node {
  protected Node parent;
  protected Node[] children;
  protected int id;
  protected JavaCCParser parser;
  protected String name;
  protected Token first, last;

  public SimpleNode(int i) {
    id = i;
  }

  public SimpleNode(JavaCCParser p, int i) {
    this(i);
    parser = p;
  }

  public void jjtOpen() {
  }

  public void jjtClose() {
  }
  
  public void jjtSetParent(Node n) { parent = n; }
  public Node jjtGetParent() { return parent; }

  public void jjtAddChild(Node n, int i) {
    if (children == null) {
      children = new Node[i + 1];
    } else if (i >= children.length) {
      Node c[] = new Node[i + 1];
      System.arraycopy(children, 0, c, 0, children.length);
      children = c;
    }
    children[i] = n;
  }

  public Node jjtGetChild(int i) {
    return children[i];
  }

  public int jjtGetNumChildren() {
    return (children == null) ? 0 : children.length;
  }

  /* You can override these two methods in subclasses of SimpleNode to
     customize the way the node appears when the tree is dumped.  If
     your output uses more than one line you should override
     toString(String), otherwise overriding toString() is probably all
     you need to do. */

  public String toString() {
    name = getName();
    if (name != null)
      return name;
    return JavaCCParserTreeConstants.jjtNodeName[id]; 
  }
  public String toString(String prefix) {
     return prefix + toString(); 
  }

  /* Override this method if you want to customize how the node dumps
     out its children. */
  public void dump(String prefix) {
    System.out.println(toString(prefix));
    if (children != null) {
      for (int i = 0; i < children.length; ++i) {
        SimpleNode n = (SimpleNode) children[i];
        if (n != null) {
          n.dump(prefix + " ");
        }
      }
    }
  }
  /**
   * Returns the child elements of this element.
   */
  public Node[] getChildren() {
    return children;
  }

  /**
   * @param string
   */
  public String getName() {
    return first.image;
  }

  /**
   * @return
   */
  public int getBeginLine() {
    return first.beginLine;
  }  
  
  /**
   * @return
   */
  public int getEndLine() {
    return last.endLine;
  }  
  /**
   * @return
   */
  public int getId() {
    return id;
  }
  
  public Token getFirstToken() { return first; }
  public void setFirstToken(Token t) { first = t; }
  public Token getLastToken() { return last;  }
  public void setLastToken(Token t) { last = t; }
  
  /**
   * search children 
   */
  public SimpleNode search(String txt) {
   if (txt.equals(this.toString()))
    return this;
   if (children != null) {
      for (int i = 0; i < children.length; ++i) {
        SimpleNode n = (SimpleNode) children[i];
        if (n != null) {
          SimpleNode c = n.search(txt);
          if (c != null)
            return c;
        }
      }
    }
    return null;
  }

}

