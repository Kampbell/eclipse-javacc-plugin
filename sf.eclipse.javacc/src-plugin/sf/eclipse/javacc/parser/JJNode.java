package sf.eclipse.javacc.parser;

import java.util.LinkedList;
import java.util.Queue;

import sf.eclipse.javacc.editors.JJElements;
import sf.eclipse.javacc.editors.JJLabelProvider;

/**
 * The JJNode is a SimpleNode with additions :
 * <ul>
 * <li>toString to have standard node names
 * <li>getLabeledName to have adequate labels (in the outline view)
 * <li>buildHashMap to record identifiers in JJElements
 * </ul>
 * 
 * @author Remi Koutcherawy 2003-2009 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009
 */
public class JJNode implements Node, JavaCCParserTreeConstants, JavaCCParserConstants {

  /*
   * MMa 11/2009 : javadoc and formatting revision ; fixed duplicated JJT identifiers (node #node) in maps ;
   * added lexical states and jjtree nodes in labels (to be displayed in outline and call hierarchy views)
   */
  /** the node's parent */
  protected Node         parent;
  /** the parser */
  protected JavaCCParser parser;
  /** the node's children */
  protected Node[]       children;
  /** the node's id */
  protected int          id;
  /** the node's name */
  protected String       name;
  /** the node's labeled name */
  protected String       labeledName;
  /** the first node */
  protected Token        first;
  /** the last node */
  protected Token        last;
  /** the JavaCC elements */
  private JJElements     jjElements;
  /** the callers */
  private JJNode[]       callers = new JJNode[0];
  /** the callees */
  private JJNode[]       callees = new JJNode[0];

  /**
   * Creates a node with a given id.
   * 
   * @param aId the given id
   */
  public JJNode(final int aId) {
    id = aId;
  }

  /**
   * Creates a node with a given parser and a given id.
   * 
   * @param aParser the given parser
   * @param aId the given id
   */
  public JJNode(final JavaCCParser aParser, final int aId) {
    this(aId);
    parser = aParser;
  }

  /**
   * Empty open method.
   */
  public void jjtOpen() {
    // nothing done here
  }

  /**
   * Empty close method.
   */
  public void jjtClose() {
    // nothing done here
  }

  /**
   * @return the id
   */
  public int getId() {
    return id;
  }

  /**
   * Sets the parent.
   * 
   * @param aParent the parent
   */
  public void jjtSetParent(final Node aParent) {
    parent = aParent;
  }

  /**
   * @return the parent
   */
  public Node jjtGetParent() {
    return parent;
  }

  /**
   * Adds a child.
   * 
   * @param aNode the child's node
   * @param aId the child's id
   */
  public void jjtAddChild(final Node aNode, final int aId) {
    if (children == null) {
      children = new Node[aId + 1];
    }
    else if (aId >= children.length) {
      final Node c[] = new Node[aId + 1];
      System.arraycopy(children, 0, c, 0, children.length);
      children = c;
    }
    children[aId] = aNode;
  }

  /**
   * Gets the child of a given index.
   * 
   * @param i the child's index
   * @return the child's node
   */
  public Node jjtGetChild(final int i) {
    return children[i];
  }

  /**
   * @return The node's number of children
   */
  public int jjtGetNumChildren() {
    return (children == null) ? 0 : children.length;
  }

  /**
   * @return the array of the node's children
   */
  public Node[] getChildren() {
    return children;
  }

  /**
   * @return the node's first token line number
   */
  public int getBeginLine() {
    return first.beginLine;
  }

  /**
   * @return the node's last token line number
   */
  public int getEndLine() {
    return last.endLine;
  }

  /**
   * @return the node's first token
   */
  public Token getFirstToken() {
    return first;
  }

  /**
   * Sets the node's first token.
   * 
   * @param aToken the first token
   */
  public void setFirstToken(final Token aToken) {
    first = aToken;
  }

  /**
   * Sets the node's last token.
   * 
   * @param aToken the last token
   */
  public void setLastToken(final Token aToken) {
    last = aToken;
  }

  /**
   * @return the node's last token
   */
  public Token getLastToken() {
    return last;
  }

  /**
   * Returns the node's name.
   * 
   * @return the node's name
   */
  @Override
  public String toString() {
    if (name == null) {
      setNames();
    }
    if (name != null) {
      return name;
    }
    // Should not happen
    return jjtNodeName[id];
  }

  /**
   * Returns the node's labeled name.
   * 
   * @return the node's labeled name
   */
  public String getLabeledName() {
    if (labeledName == null) {
      setNames();
    }
    if (labeledName != null) {
      return labeledName;
    }
    // Should not happen
    return jjtNodeName[id];
  }

  /**
   * Sets the node's name and labeled name.
   */
  public void setNames() {
    // default name
    name = labeledName = first.image;

    // Options option_binding => Option name
    if (id == JJTOPTION_BINDING) {
      Token f = first;
      while (f != last && f.kind != JavaCCParserConstants.IDENTIFIER
             && f.kind != JavaCCParserConstants._LOOKAHEAD && f.kind != JavaCCParserConstants._IGNORE_CASE) {
        f = f.next;
      }
      name = labeledName = f.image;
    }
    // Parser ClassDeclaration => Class name
    else if (id == JJTCLASSORINTERFACEDECLARATION) {
      Token f = first;
      while (f != last && f.next.kind != JavaCCParserConstants.LBRACE) {
        f = f.next;
      }
      name = labeledName = f.image;
    }
    // Parser MethodDeclaration => Method name
    else if (id == JJTMETHODDECLARATION) {
      Token f = first;
      while (f != last && f.next.kind != JavaCCParserConstants.LPAREN) {
        f = f.next;
      }
      name = labeledName = f.image;
    }
    // Bnf production => prod name or prod name - #node
    else if (id == JJTBNF_PRODUCTION) {
      Token f = first;
      while (f != last && f.next.kind != JavaCCParserConstants.LPAREN) {
        f = f.next;
      }
      name = labeledName = f.image;
      while (f != last && f.next.kind != JavaCCParserConstants.COLON) {
        f = f.next;
      }
      if (f.kind != JavaCCParserConstants.RPAREN) {
        labeledName += " - #" + f.image;
      }
    }
    // Regular expression => kind name or kind name : all lexical state
    else if (id == JJTREGULAR_EXPR_PRODUCTION) {
      Token f = first;
      String ls = null;
      if (f.kind == JavaCCParserConstants.LT) {
        f = f.next;
        ls = " : ";
        while (f != last && f.kind != JavaCCParserConstants.GT) {
          ls += f.image;
          f = f.next;
        }
        f = f.next;
      }
      name = labeledName = f.image;
      if (ls != null) {
        labeledName += ls;
      }
    }
    // Token => Token name or token name : lexical state (keep "#" plus name for private label identifier)
    else if (id == JJTREGEXPR_SPEC) {
      Token f = first;
      while (f != last && f.kind == JavaCCParserConstants.LT) {
        f = f.next;
      }
      name = labeledName = f.image;
      if (f.kind == SHARP) {
        f = f.next;
        name = labeledName += f.image;
      }
      int lvl = 1;
      while (f != last && lvl != 0) {
        if (f.kind == JavaCCParserConstants.LT) {
          lvl++;
        }
        else if (f.kind == JavaCCParserConstants.GT) {
          lvl--;
        }
        f = f.next;
      }
      String ls = null;
      if (f.kind == JavaCCParserConstants.COLON) {
        f = f.next;
        ls = " : " + f.image;
      }
      if (ls != null) {
        labeledName += ls;
      }
    }
  }

  /**
   * To buildHashMap each JJNode needs to know where to put Name / Node associations.
   * 
   * @param aJjElements the JavaCC elements
   */
  public void setJJElementsToUpdate(final JJElements aJjElements) {
    jjElements = aJjElements;
    if (children != null) {
      for (final Node child : children) {
        final JJNode n = (JJNode) child;
        n.setJJElementsToUpdate(aJjElements);
      }
    }
  }

  /**
   * Builds recursively a HashMap of JavaCC elements given the node at the root of parse tree.
   */
  public void buildHashMap() {
    setNames();
    jjElements.put(name, this);
    if (children != null) {
      for (final Node child : children) {
        final JJNode n = (JJNode) child;
        n.buildHashMap();
      }
    }
    return;
  }

  /**
   * Builds the array of Callers for this node.
   */
  public void buildCallerMap() {
    // Clear callers
    callers = new JJNode[0];
    // Get up to the root
    JJNode root = this;
    while (root.parent != null) {
      root = (JJNode) root.parent;
    }
    // Search all tree for this identifier
    final Queue<JJNode> stack = new LinkedList<JJNode>();
    stack.offer(root);
    // Examine each element of the stack, and if it has children push children
    while (!stack.isEmpty()) {
      final JJNode nd = stack.remove();
      if (nd.getId() == JJTIDENTIFIER && name.equals(nd.name)) {
        // Add caller to this identifier, not to declaration node
        // Get the node where this identifier appears
        final JJNode stackedParent = (JJNode) nd.jjtGetParent();
        // Ignore if this identifier is the identifier of the parent or has the same name
        if (stackedParent.children[0] == nd || name.equals(stackedParent.name)) {
          continue;
        }
        addCaller(stackedParent);
      }
      if (nd.children != null) {
        for (final Node child : nd.children) {
          stack.offer((JJNode) child);
        }
      }
    }
  }

  /**
   * Adds a given node at the beginning of the array of callers of this node.<br>
   * Changes the node's labeled name to the name as {@link JJLabelProvider#getText(Object)} will use it in the
   * caller hierarchy view.
   * 
   * @param aNode the node to add
   */
  public void addCaller(final JJNode aNode) {
    final JJNode c[] = new JJNode[callers.length + 1];
    System.arraycopy(callers, 0, c, 1, callers.length);
    callers = c;
    aNode.labeledName = aNode.name;
    callers[0] = aNode;
  }

  /**
   * @return the array of callers for this node
   */
  public JJNode[] getCallers() {
    return callers;
  }

  /**
   * Builds the array of callees for this node.
   */
  public void buildCalleeMap() {
    // Clear callees
    callees = new JJNode[0];
    // Get the declaration node with jjElements.getNode()
    final JJNode declarationNode = jjElements.getNonIdentifierNode(name);
    // Search callees in declaration node
    final Queue<JJNode> stack = new LinkedList<JJNode>();
    // Push all children of declaration node
    if (declarationNode.children != null) {
      for (final Node child : declarationNode.children) {
        stack.offer((JJNode) child);
      }
    }
    // Examine stack and push children of children
    final String declNdShortName = declarationNode.name;
    while (!stack.isEmpty()) {
      final JJNode nd = stack.remove();
      if (nd.getId() == JJTIDENTIFIER) {
        // Add callee to this identifier, not to declaration node
        // Except if this identifier is the name of the declaration node
        final JJNode stackedParent = (JJNode) nd.jjtGetParent();
        if (stackedParent.children[0] == nd) {
          continue;
        }
        // Except if this identifier is a node_descriptor (no or same declaration node)
        if (jjElements.getNonIdentifierNode(nd.name) == null || nd.name.equals(declNdShortName)) {
          continue;
        }
        addCallee(nd);
      }
      if (nd.children != null) {
        for (final Node child : nd.children) {
          stack.offer((JJNode) child);
        }
      }
    }
    return;
  }

  /**
   * Adds a given node at the end of the array of callees of this node.
   * 
   * @param aNode the node to add
   */
  public void addCallee(final JJNode aNode) {
    final JJNode newCallees[] = new JJNode[callees.length + 1];
    System.arraycopy(callees, 0, newCallees, 0, callees.length);
    newCallees[callees.length] = aNode;
    callees = newCallees;
  }

  /**
   * @return the array of callees
   */
  public JJNode[] getCallees() {
    return callees;
  }
}
