/**
 * The JJNode is a SimpleNode with additions
 * - toString to have a nice label in outline
 * - buildHashMap to record identifiers in JJElements
 * 
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */
package sf.eclipse.javacc.parser;

import sf.eclipse.javacc.editors.JJElements;

public class JJNode implements Node, JavaCCParserTreeConstants, JavaCCParserConstants {
  protected Node parent;
  protected JavaCCParser parser;
  protected Node[] children;
  protected int id;
  protected String name;
  protected Token first, last;

  public JJNode(int i) {
    id = i;
  }
  
  public int getId() {
    return id;
  }
  public JJNode(JavaCCParser p, int i) {
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

  /* You can override these two methods in subclasses of JJNode to
     customize the way the node appears when the tree is dumped.  If
     your output uses more than one line you should override
     toString(String), otherwise overriding toString() is probably all
     you need to do. */
  public String toString(String prefix) { return prefix + toString(); }

  /* Override this method if you want to customize how the node dumps
     out its children. */

  public void dump(String prefix) {
    System.out.println(toString(prefix));
    if (children != null) {
      for (int i = 0; i < children.length; ++i) {
	JJNode n = (JJNode)children[i];
	if (n != null) {
	  n.dump(prefix + " "); //$NON-NLS-1$
	}
      }
    }
  }
  
  /** 
   * Show correct names 
   */
  public String toString() {
    // default name
    name = getName();
    
    // Options option_binding => Option name
    if (id == JJTOPTION_BINDING){
      Token f = first;
      while(f != last && f.kind != JavaCCParserConstants.IDENTIFIER
            && f.kind != 2 && f.kind != 3) {
        f = f.next;
      }
      name = f.image;
    }
    // Parser ClassDeclaration => Class name
    if (id == JJTCLASSORINTERFACEDECLARATION){
      Token f = first;
      while(f != last && f.next.kind != JavaCCParserConstants.LPAREN)
        f = f.next;
      name = f.image;
    }      
    // Parser MethodDeclaration => Method name
    if (id == JJTMETHODDECLARATION){
      Token f = first;
      while(f != last && f.next.kind != JavaCCParserConstants.LPAREN)
        f = f.next;
      name = f.image;
    }    
    // Rules => Rule name
    if (id == JJTBNF_PRODUCTION){
      Token f = first;
      while(f != last && f.next.kind!=JavaCCParserConstants.LPAREN)
        f = f.next;
      name = f.image;
    }
    // Token section => Token section name, skip "<"
    if (id == JJTREGULAR_EXPR_PRODUCTION){
      Token f = first;
      while(f != last && f.kind == JavaCCParserConstants.LT)
        f = f.next;
      name = f.image;
    }    
    // Token => Token name, skip "<" and keep "#" plus name
    if (id == JJTREGEXPR_SPEC){
      Token f = first;
      while(f != last && f.kind == JavaCCParserConstants.LT )
        f = f.next;
      name = f.image;
      if ( f.kind == SHARP)
        name += f.next.image;
    }
    if (name != null)
      return name;

    // Should not append
    return jjtNodeName[id]; 
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
  
  public Token getFirstToken() { return first; }
  public void setFirstToken(Token t) { first = t; }
  public Token getLastToken() { return last;  }
  public void setLastToken(Token t) { last = t; }
  
  /**
   * Search children corresponding to txt
   */
  public JJNode search(String txt) {
    if (txt.equals(this.toString()))
      return this;
    if (this.toString().startsWith("#") //$NON-NLS-1$
        && txt.equals(this.toString().substring(1)))
      return this;
    if (children != null) {
      for (int i = 0; i < children.length; ++i) {
        JJNode n = (JJNode) children[i];
        if (n != null) {
          JJNode c = n.search(txt);
          if (c != null)
            return c;
        }
      }
    }
    //System.out.println("not found:"+txt);
    return null;
  }
  
  /**
   * Build recursively a HashMap of JavaCC Elements
   * given JJnode at the root of parse tree.
   */
  public void buildHashMap() {
    if (this.toString().startsWith("#")) //$NON-NLS-1$
      JJElements.put(this.toString().substring(1), this);
    else
      JJElements.put(this.toString(), this);
    if (children != null) {
      for (int i = 0; i < children.length; ++i) {
        JJNode n = (JJNode) children[i];
        if (n != null) {
          n.buildHashMap();
        }
      }
    }
    return;
  }
}

