/**
 * The JJNode is a SimpleNode with additions
 * - toString to have a nice label in outline
 * - buildHashMap to record identifiers in JJElements
 * 
 * @author Remi Koutcherawy 2003-2006
 * CeCILL license http://www.cecill.info/index.en.html
 */
package sf.eclipse.javacc.parser;

import java.util.ArrayDeque;
import java.util.Deque;

import sf.eclipse.javacc.editors.JJElements;

public class JJNode implements Node, JavaCCParserTreeConstants,
    JavaCCParserConstants {
  protected Node         parent;
  protected JavaCCParser parser;
  protected Node[]       children;
  protected int          id;
  protected String       name;
  protected Token        first, last;
  private JJElements     jjElements;
  private JJNode[]       callers = new JJNode[0];
  private JJNode[]       callees = new JJNode[0];

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
  public void jjtSetParent(Node n) {
    parent = n;
  }
  public Node jjtGetParent() {
    return parent;
  }

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
  public Node[] getChildren() {
    return children;
  }
  public int getBeginLine() {
    return first.beginLine;
  }
  public int getEndLine() {
    return last.endLine;
  }
  public Token getFirstToken() {
    return first;
  }
  public void setFirstToken(Token t) {
    first = t;
  }
  public void setLastToken(Token t) {
    last = t;
  }
  public Token getLastToken() {
    return last;
  }
  // public String getName() { return first.image; }

  /**
   * Show correct names
   */
  public String toString() {
    // default name
    name = first.image;

    // Options option_binding => Option name
    if (id == JJTOPTION_BINDING) {
      Token f = first;
      while (f != last && f.kind != JavaCCParserConstants.IDENTIFIER
          && f.kind != 2 && f.kind != 3) {
        f = f.next;
      }
      name = f.image;
    }
    // Parser ClassDeclaration => Class name
    if (id == JJTCLASSORINTERFACEDECLARATION) {
      Token f = first;
      while (f != last && f.next.kind != JavaCCParserConstants.LBRACE)
        f = f.next;
      name = f.image;
    }
    // Parser MethodDeclaration => Method name
    if (id == JJTMETHODDECLARATION) {
      Token f = first;
      while (f != last && f.next.kind != JavaCCParserConstants.LPAREN)
        f = f.next;
      name = f.image;
    }
    // Rules => Rule name
    if (id == JJTBNF_PRODUCTION) {
      Token f = first;
      while (f != last && f.next.kind != JavaCCParserConstants.LPAREN)
        f = f.next;
      name = f.image;
    }
    // Token section => Token section name, skip "<"
    if (id == JJTREGULAR_EXPR_PRODUCTION) {
      Token f = first;
      while (f != last && f.kind == JavaCCParserConstants.LT)
        f = f.next;
      name = f.image;
    }
    // Token => Token name, skip "<" and keep "#" plus name
    if (id == JJTREGEXPR_SPEC) {
      Token f = first;
      while (f != last && f.kind == JavaCCParserConstants.LT)
        f = f.next;
      name = f.image;
      if (f.kind == SHARP)
        name += f.next.image;
    }
    if (name != null)
      return name;

    // Should not happen
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
   * To buildHashMap each JJNode needs to know where to put Name / Node
   * associations
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
   * Build recursively a HashMap of JavaCC Elements given JJnode at the root of
   * parse tree.
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
  /**
   * Build array of Callers given JJnode
   */
  public void buildCallerMap() {
    // Clear callers
    callers = new JJNode[0];
    // Get up to the root
    JJNode root = this;
    while (root.parent != null)
      root = (JJNode) root.parent;
    // Search all tree for this identifier
    Deque<JJNode> stack = new ArrayDeque<JJNode>();
    stack.push(root);
    // Examine each element of the stack, and if it has children push children
    while (!stack.isEmpty()) {
      JJNode n = stack.pop();
      if (n.getId() == JJTIDENTIFIER && this.toString().equals(n.toString())) {
        // Add caller to this identifier, not to declaration node
        // Get the node where this identifier appears
        JJNode parent = (JJNode) n.jjtGetParent();
        // Ignore if this identifier is the identifier of the parent 
        if (parent.children[0] == n)
          continue;
        this.addCaller((JJNode) n.jjtGetParent());
      }
      if (n.children != null)
        for (int i = 0; i < n.children.length; ++i)
          stack.push((JJNode) n.children[i]);
    }
  }
  /**
   * Add node at the beginning of the array of callers of this node
   */
  public void addCaller(JJNode node) {
    JJNode c[] = new JJNode[callers.length + 1];
    System.arraycopy(callers, 0, c, 1, callers.length);
    callers = c;
    callers[0] = node;
  }
  /**
   * Get array of callers nodes
   */
  public JJNode[] getCallers() {
    return callers;
  }

  /**
   * Build array of called rules
   */
  public void buildCalleeMap() {
    // Clear callees
    callees = new JJNode[0];
    // Get the declaration node with jjElements.getNode()
    JJNode declarationNode = jjElements.getNode(this.toString());
    // Search callees in declaration node
    Deque<JJNode> stack = new ArrayDeque<JJNode>();
    // Push all children of declaration node
    if (declarationNode.children != null)
      for (int i = 0; i < declarationNode.children.length; ++i)
        stack.push((JJNode) declarationNode.children[i]);
    // Examine stack and push children of children
    while (!stack.isEmpty()) {
      JJNode n = stack.pop();
      if (n.getId() == JJTIDENTIFIER) {
        // Add callee to this identifier, not to declaration node
        // Except if this identifier is the name of the declaration node
        JJNode parent = (JJNode) n.jjtGetParent();
        if (parent.children[0] == n)
          continue;
        // Except if this identifier is a node_descriptor (no declaration node)
        if (jjElements.getNode(n.toString()) == null)
          continue;
        this.addCallee(n); 
      }
      if (n.children != null)
        for (int i = 0; i < n.children.length; ++i)
          stack.push((JJNode) n.children[i]);
    }
    return;
  }
  /**
   * Add node at the beginning of the array of called rules of this node
   */
  public void addCallee(JJNode node) {
    JJNode c[] = new JJNode[callees.length + 1];
    System.arraycopy(callees, 0, c, 1, callees.length);
    callees = c;
    callees[0] = node;
  }
  /**
   * Get array of callees nodes
   */
  public JJNode[] getCallees() {
    return callees;
  }
}
