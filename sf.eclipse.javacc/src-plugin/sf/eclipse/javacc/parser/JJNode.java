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
  private JJElements jjElements;

  public JJNode(int i) { id = i; }
  public int getId() { return id; }
  
  public JJNode(JavaCCParser p, int i) {
    this(i);
    parser = p;
  }

  public void jjtOpen() {}
  public void jjtClose() {}
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
  public Node jjtGetChild(int i) { return children[i];  }
  public int jjtGetNumChildren() { return (children == null) ? 0 : children.length; }
  public Node[] getChildren() { return children; }
  public int getBeginLine() { return first.beginLine; }  
  public int getEndLine() { return last.endLine; }  
  public Token getFirstToken() { return first; }
  public void setFirstToken(Token t) { first = t; }
  public void setLastToken(Token t) { last = t; }
//  public Token getLastToken() { return last;  }
//  public String getName() { return first.image; }
  
  /** 
   * Show correct names 
   */
  public String toString() {
    // default name
    name = first.image;
    
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
      while(f != last && f.next.kind != JavaCCParserConstants.LBRACE)
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
   * Search children corresponding to text
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
    return null;
  }
  
  /**
   * To buildHashMap each JJNode needs to know where to put Name / Node associations
   * @param jjElements
   */
  public void setJJElementsToUpdate(JJElements jjElements) {
    this.jjElements = jjElements;
    if (children != null) {
      for (int i = 0; i < children.length; ++i) {
        JJNode n = (JJNode) children[i];
        n.setJJElementsToUpdate(jjElements);
      }
    }
  }
  /**
   * Build recursively a HashMap of JavaCC Elements
   * given JJnode at the root of parse tree.
   */
  public void buildHashMap() {
    if (this.toString().startsWith("#")) //$NON-NLS-1$
      jjElements.put(this.toString().substring(1), this);
    else
      jjElements.put(this.toString(), this);
    if (children != null) {
      for (int i = 0; i < children.length; ++i) {
        JJNode n = (JJNode) children[i];
        n.buildHashMap();
      }
    }
    return;
  }
}

