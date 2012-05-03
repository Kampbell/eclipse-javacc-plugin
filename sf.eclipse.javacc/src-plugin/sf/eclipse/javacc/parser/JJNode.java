package sf.eclipse.javacc.parser;

import java.util.LinkedList;
import java.util.Queue;

import sf.eclipse.javacc.editors.JJElements;
import sf.eclipse.javacc.head.Activator;

/**
 * The JJNode is a SimpleNode to which have been added all the methods not starting with "jjt". </ul>
 * 
 * @author Remi Koutcherawy 2003-2009 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011
 */
public class JJNode implements Node, JavaCCParserTreeConstants, JavaCCParserConstants {

  // MMa 11/2009 : javadoc and formatting revision ; fixed duplicated JJT identifiers (node #node) in maps ;
  // added lexical states and JJTree nodes in labels (to be displayed in outline and call hierarchy views)
  // MMa 08/2011 : added node_descriptor labels
  // MMa 08/2011 : fixed NPE in buildCalleeMap()
  // MMa 08/2011 : enhanced Call Hierarchy view (to display JJTree node descriptors)
  // MMa 08/2011 : enhanced Outline view (to display JJTree node descriptors and to fix regexpr_spec)
  // TODO add methods and classes call hierarchy callers and callees

  /** the node's parent */
  protected Node              parent;
  /** the parser */
  protected JavaCCParser      parser;
  /** the node's children */
  protected Node[]            children;
  /** the node's id */
  protected int               id;
  /** the node's name */
  protected String            name;
  /** the node's display name */
  protected String            displayName;
  /** the first node */
  protected Token             first;
  /** the last node */
  protected Token             last;
  /** the JavaCC elements */
  private JJElements          jjElements;
  /** the callers */
  private JJNode[]            callers   = new JJNode[0];
  /** the callees */
  private JJNode[]            callees   = new JJNode[0];
  /** the separator string for display names */
  public static final String  DASH_SEP  = " - ";
  /** the separator string for lexical states */
  private static final String COLON_SEP = " : ";

  /** An fake node to display an out of hierarchy selection */
  static final JJNode         oohsJJNode;

  static {
    oohsJJNode = new JJNode(-1);
    oohsJJNode.name = Activator.getString("JJRuntimeOptions.Out_of_hierarchy_selection"); //$NON-NLS-1$
    oohsJJNode.displayName = oohsJJNode.name;
  }

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
   * @return Returns the fake node to display an out of hierarchy selection
   */
  public static final JJNode getOohsjjnode() {
    return oohsJJNode;
  }

  /**
   * Empty open method. Comes from SimpleNode.
   */
  @Override
  public void jjtOpen() {
    // nothing done here
  }

  /**
   * Empty close method. Comes from SimpleNode.
   */
  @Override
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
   * Sets the parent. Comes from SimpleNode.
   * 
   * @param aParent the parent
   */
  @Override
  public void jjtSetParent(final Node aParent) {
    parent = aParent;
  }

  /**
   * Gets the parent. Comes from SimpleNode.
   * 
   * @return the parent
   */
  @Override
  public Node jjtGetParent() {
    return parent;
  }

  /**
   * @return the parent
   */
  public JJNode getParent() {
    if (parent instanceof JJNode) {
      return (JJNode) parent;
    }
    else {
      return null;
    }
  }

  /**
   * Adds a child. Comes from SimpleNode.
   * 
   * @param aNode the child's node
   * @param aId the child's id
   */
  @Override
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
   * Gets the child of a given index. Comes from SimpleNode.
   * 
   * @param i the child's index
   * @return the child's node
   */
  @Override
  public Node jjtGetChild(final int i) {
    return children[i];
  }

  /**
   * Gets the node's number of children. Comes from SimpleNode.
   * 
   * @return The node's number of children
   */
  @Override
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
   * Returns the node's name. Comes from SimpleNode.
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
   * Returns the node's display name.
   * 
   * @return the node's display name
   */
  public String getDisplayName() {
    if (displayName == null) {
      setNames();
    }
    if (displayName != null) {
      return displayName;
    }
    // Should not happen
    return jjtNodeName[id];
  }

  /**
   * Sets the node's name and display name.
   */
  public void setNames() {
    // default name
    name = displayName = first.image;

    if (id == JJTOPTION_BINDING) {
      // Options option_binding => Option name
      Token f = first;
      while (f != last && f.kind != JavaCCParserConstants.IDENTIFIER
             && f.kind != JavaCCParserConstants._LOOKAHEAD && f.kind != JavaCCParserConstants._IGNORE_CASE) {
        f = f.next;
      }
      name = displayName = f.image;
    }
    else if (id == JJTCLASSORINTERFACEDECLARATION) {
      // Parser ClassDeclaration => Class name
      Token f = first;
      while (f != last && f.next.kind != JavaCCParserConstants.LBRACE) {
        f = f.next;
      }
      name = displayName = f.image;
    }
    // Parser MethodDeclaration => Method name
    else if (id == JJTMETHODDECLARATION) {
      Token f = first;
      while (f != last && f.next.kind != JavaCCParserConstants.LPAREN) {
        f = f.next;
      }
      name = displayName = f.image;
    }
    else if (id == JJTBNF_PRODUCTION) {
      // Bnf production => identifier name or identifier name - #node name
      Token f = first;
      while (f != last && f.next.kind != JavaCCParserConstants.LPAREN) {
        f = f.next;
      }
      name = displayName = f.image;
      while (f != last && f.next.kind != JavaCCParserConstants.COLON) {
        f = f.next;
        if (f.kind == JavaCCParserConstants.SHARP) {
          displayName += DASH_SEP + f.image + f.next.image;
        }
      }
    }
    else if (id == JJTREGULAR_EXPR_PRODUCTION) {
      // Regular expression => kind name or or kind name : all lexical state
      Token f = first;
      String lexState = null;
      if (f.kind == JavaCCParserConstants.LT) {
        f = f.next;
        lexState = COLON_SEP;
        while (f != last && f.kind != JavaCCParserConstants.GT) {
          lexState += f.image;
          f = f.next;
        }
        f = f.next;
      }
      name = displayName = f.image;
      if (lexState != null) {
        displayName += lexState;
      }
    }
    else if (id == JJTREGEXPR_SPEC) {
      // Token => Token name or token name : lexical state (keep "#" plus name for private label identifier)
      boolean foundLT = false;
      Token f = first;
      while (f != last) {
        if (f.kind == JavaCCParserConstants.LT) {
          foundLT = true;
          f = f.next;
          break;
        }
        f = f.next;
      }
      if (foundLT) {
        // "<" "#" IDENTIFIER ":" complex_regular_expression_choices ">
        if (f.kind == JavaCCParserConstants.SHARP) {
          name = displayName = f.image;
          f = f.next;
          name = displayName += f.image;
        }
        else if (f.kind == JavaCCParserConstants.IDENTIFIER) {
          // "<" IDENTIFIER ":" complex_regular_expression_choices "> or "<" < IDENTIFIER > ">
          name = displayName = f.image;
        }
        else {
          // "<" complex_regular_expression_choices ">"
          name = displayName = "< ... >";
        }
        // skip up to last ">"
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
      }
      else {
        // no "<" ... ">", it's a single StringLiteral
        name = displayName = f.image;
      }
      // set lexical state
      String lexState = null;
      if (f.kind == JavaCCParserConstants.COLON) {
        f = f.next;
        lexState = COLON_SEP + f.image;
      }
      if (lexState != null) {
        displayName += lexState;
      }
    }
    else if (id == JJTIDENT_REG_EXPR_PRIVATE_LABEL) {
      name = displayName = "#" + first.image; //$NON-NLS-1$
    }
    else if (id == JJTIDENT_BNF_DECL) {
      name = first.image;
      displayName = ((JJNode) parent).displayName;
    }
    else if (id == JJTNODE_DESC_IN_EXP || id == JJTNODE_DESC_IN_METH) {
      name = displayName = first.image + first.next.image;
    }
    else if (id == JJTNODE_DESC_BNF_DECL) {
      name = first.image + first.next.image;
      displayName = ((JJNode) parent).displayName;
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
   * Builds recursively a map of JavaCC elements given the node at the root of parse tree.
   */
  public void buildJJNodesMap() {
    setNames();
    jjElements.put(name, this);
    if (children != null) {
      for (final Node child : children) {
        final JJNode n = (JJNode) child;
        n.buildJJNodesMap();
      }
    }
    return;
  }

  /**
   * Builds the array of callers for this node.
   */
  public void buildCallers() {
    // Clear callers
    callers = new JJNode[0];
    // Get up to the root
    JJNode root = this;
    while (root.parent != null) {
      root = (JJNode) root.parent;
    }
    // Search the whole tree from the root
    final Queue<JJNode> stack = new LinkedList<JJNode>();
    stack.offer(root);
    // take the display name for node descriptors and private label identifiers for later comparison
    String sel = name;
    final int thId = this.getId();
    if (thId == JJTNODE_DESC_BNF_DECL || thId == JJTNODE_DESC_IN_EXP || thId == JJTNODE_DESC_IN_METH
        || thId == JJTIDENT_REG_EXPR_PRIVATE_LABEL) {
      sel = displayName;
    }
    // Examine each element of the stack
    while (!stack.isEmpty()) {
      final JJNode nd = stack.remove();
      final int ndId = nd.getId();
      if (sel.equals(nd.name)) {
        if (ndId == JJTIDENT_BNF_DECL || ndId == JJTIDENT_USE || ndId == JJTIDENT_REG_EXPR_LABEL
            || ndId == JJTIDENT_REG_EXPR_PRIVATE_LABEL || ndId == JJTNODE_DESC_BNF_DECL
            || ndId == JJTNODE_DESC_IN_EXP || ndId == JJTNODE_DESC_IN_METH) {
          // Take JJTree identifier nodes with the same name
          // Cases productions Q inside the body of a production P or the production P in its declaration
          // as in 'type P() [#N] : {} {... Q() ...}'
          // Same for label identifiers and private labels identifiers
          // Get its parent
          final JJNode ndParent = (JJNode) nd.parent;
          // Skip this parent if the JJTree identifier node is its first child
          // Case the production P in its declaration, as in 'type P() [#N] : ...'
          if (ndParent.children[0] == nd) {
            continue;
          }
          // Found a caller (the parent), record it
          addCaller(ndParent, false);
        }
      }
      else if (ndId == JJTREGULAR_EXPR_PRODUCTION //
               && this.getParent().getParent() == nd) {
        // Take token / special_token / skip / more if ancestor
        // Found a caller (the node), record it
        addCaller(nd, false);
      }
      // If it has children push them
      if (nd.children != null) {
        for (final Node child : nd.children) {
          stack.offer((JJNode) child);
        }
      }
    }
  }

  /**
   * Adds a given node at the beginning or the end of the array of callers of this node only if it does not
   * exist yet in the array.<br>
   * 
   * @param aNode the node to add
   * @param toTop true if at the beginning, false if at the end
   */
  public void addCaller(final JJNode aNode, final boolean toTop) {
    for (final JJNode caller : callers) {
      if (caller == aNode) {
        return;
      }
    }
    final JJNode newCallers[] = new JJNode[callers.length + 1];
    System.arraycopy(callers, 0, newCallers, (toTop ? 1 : 0), callers.length);
    newCallers[toTop ? 0 : callers.length] = aNode;
    callers = newCallers;
  }

  /**
   * @return the array of callers for this node
   */
  public JJNode[] getCallers() {
    return callers;
  }

  /**
   * Clears the array of callers for this node.
   */
  public void clearCallers() {
    //    callers = new JJNode[0];
    callers = new JJNode[1];
    callers[0] = oohsJJNode;
  }

  /**
   * Builds the array of callees for this node.
   */
  public void buildCallees() {
    // Clear callees
    callees = new JJNode[0];
    // Get the node to which this node belongs
    String declName = name;
    if (this.getId() == JJTNODE_DESC_BNF_DECL) {
      declName = name.substring(1) + DASH_SEP + name;
    }
    final JJNode declarationNode = jjElements.getNonIdentNorNodeDesc(declName);
    if (declarationNode == null) {
      return;
    }
    // Search callees within the declaration node tree
    final Queue<JJNode> stack = new LinkedList<JJNode>();
    // Push all children of the declaration node
    if (declarationNode.children != null) {
      for (final Node child : declarationNode.children) {
        stack.offer((JJNode) child);
      }
    }
    // Examine each element of the stack
    while (!stack.isEmpty()) {
      final JJNode nd = stack.remove();
      // Take all JJTree identifier and node descriptor nodes
      final int ndId = nd.getId();
      if (ndId == JJTIDENT_BNF_DECL //
          || ndId == JJTIDENT_REG_EXPR_LABEL //
          || ndId == JJTIDENT_REG_EXPR_PRIVATE_LABEL //
          || ndId == JJTIDENT_USE //
          || ndId == JJTNODE_DESC_BNF_DECL //
          || ndId == JJTNODE_DESC_IN_EXP //
          || ndId == JJTNODE_DESC_IN_METH //
      ) {
        // Get its parent
        final JJNode ndParent = (JJNode) nd.jjtGetParent();
        // Skip this parent if the JJTree node is its first child
        if ((ndParent.children[0] == nd) //
            || (ndId == JJTNODE_DESC_BNF_DECL //
                && ndParent.children.length > 1 //
            && ndParent.children[1] == nd)) {
          continue;
        }
        // Found a callee, record it
        addCallee(nd);
        // If it has children push them
        if (nd.children != null) {
          for (final Node child : nd.children) {
            stack.offer((JJNode) child);
          }
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

  /**
   * Clears the array of callees for this node.
   */
  public void clearCallees() {
    //    callees = new JJNode[0];
    callees = new JJNode[1];
    callees[0] = oohsJJNode;
  }

}
