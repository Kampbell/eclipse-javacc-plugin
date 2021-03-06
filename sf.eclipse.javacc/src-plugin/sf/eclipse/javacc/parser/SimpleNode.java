/* Generated By:JJTree: Do not edit this line. SimpleNode.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package sf.eclipse.javacc.parser;

public @SuppressWarnings("all")
class SimpleNode implements Node {

  protected Node         parent;
  protected Node[]       children;
  protected int          id;
  protected Object       value;
  protected JavaCCParser parser;

  public SimpleNode(final int i) {
    id = i;
  }

  public SimpleNode(final JavaCCParser p, final int i) {
    this(i);
    parser = p;
  }

  public void jjtOpen() {
  }

  public void jjtClose() {
  }

  public void jjtSetParent(final Node n) {
    parent = n;
  }

  public Node jjtGetParent() {
    return parent;
  }

  public void jjtAddChild(final Node n, final int i) {
    if (children == null) {
      children = new Node[i + 1];
    }
    else if (i >= children.length) {
      final Node c[] = new Node[i + 1];
      System.arraycopy(children, 0, c, 0, children.length);
      children = c;
    }
    children[i] = n;
  }

  public Node jjtGetChild(final int i) {
    return children[i];
  }

  public int jjtGetNumChildren() {
    return (children == null) ? 0 : children.length;
  }

  public void jjtSetValue(final Object value) {
    this.value = value;
  }

  public Object jjtGetValue() {
    return value;
  }

  /* You can override these two methods in subclasses of SimpleNode to
     customize the way the node appears when the tree is dumped.  If
     your output uses more than one line you should override
     toString(String), otherwise overriding toString() is probably all
     you need to do */

  public String toString() {
    return JavaCCParserTreeConstants.jjtNodeName[id];
  }

  public String toString(final String prefix) {
    return prefix + toString();
  }

  /* Override this method if you want to customize how the node dumps
     out its children */

  public void dump(final String prefix) {
    System.out.println(toString(prefix));
    if (children != null) {
      for (int i = 0; i < children.length; ++i) {
        final SimpleNode n = (SimpleNode) children[i];
        if (n != null) {
          n.dump(prefix + " "); //$NON-NLS-1$
        }
      }
    }
  }
}

/* JavaCC - OriginalChecksum=3d7d2b6a9dbcb37f88227f887bc46b29 (do not edit this line) */
