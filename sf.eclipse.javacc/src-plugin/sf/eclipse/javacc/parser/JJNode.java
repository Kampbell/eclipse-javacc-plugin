package sf.eclipse.javacc.parser;

import static sf.eclipse.javacc.parser.JavaCCParserConstants.*;
import static sf.eclipse.javacc.parser.JavaCCParserTreeConstants.*;

import java.util.LinkedList;
import java.util.Queue;

import sf.eclipse.javacc.base.AbstractActivator;
import sf.eclipse.javacc.editors.Elements;

/**
 * The JJNode is a SimpleNode to which have been added all the methods not starting with "jjt".
 * 
 * @author Remi Koutcherawy 2003-2009 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015-2016
 * @author Bill Fenlason 2012
 */
public class JJNode implements Node {

  // MMa 11/2009 : javadoc and formatting revision ; fixed duplicated JJT identifiers (node #node) in maps ;
  //               added lexical states and JJTree nodes in labels (to be displayed in outline and call
  //               hierarchy views)
  // MMa 08/2011 : added node_descriptor labels
  // MMa 08/2011 : fixed NPE in buildCalleeMap()
  // MMa 08/2011 : enhanced Call Hierarchy View (to display JJTree node descriptors)
  // MMa 08/2011 : enhanced Outline Page (to display JJTree node descriptors and to fix regexpr_spec)
  // BF  06/2012 : added NLS tags and eliminated else clauses to prevent warning messages
  // MMa 10/2012 : added begin and end columns, used static imports, adapted to the no node creation flags, 
  //               fixed outline names and tree ; adapted to modifications in grammar nodes ;
  //               adapted to the new token offset availability
  // MMa 11/2014 : added nameToken field
  // MMa 02/2016 : fixed nameToken for  "<" "#" < IDENTIFIER > ":" complex_regular_expression_choices ">
  //               added classes and methods and ... in Call Hierarchy View callers and callees
  // MMa 04/2016 : fixed first children in CHV callees when a bnf production has a node identifier

  /** The node's parent */
  protected Node                parent;
  /** The parser */
  protected JavaCCParser        parser;
  /** The node's children */
  protected Node[]              children;
  /** The node's id */
  protected int                 id;
  /** The node's name */
  protected String              name;
  /** The node's display name */
  protected String              displayName;
  /** The node's name token */
  protected Token               nameToken;
  /** The first node */
  protected Token               first;
  /** The last node */
  protected Token               last;
  /** The callers */
  protected JJNode[]            callers   = new JJNode[0];
  /** The callees */
  protected JJNode[]            callees   = new JJNode[0];
  /** The separator string for display names */
  public static final String    DASH_SEP  = " - ";        //$NON-NLS-1$
  /** The separator string for lexical states */
  protected static final String COLON_SEP = " : ";        //$NON-NLS-1$
  /** A fake node to display an out of hierarchy selection */
  protected static final JJNode oohsJJNode;
  static {
    oohsJJNode = new JJNode(-1);
    oohsJJNode.name = AbstractActivator.getMsg("OptGlob.Out_of_hierarchy_selection"); //$NON-NLS-1$
    oohsJJNode.displayName = oohsJJNode.name;
  }

  /**
   * Creates a node with a given id.
   * 
   * @param aId - the given id
   */
  public JJNode(final int aId) {
    id = aId;
  }

  /**
   * Creates a node with a given parser and a given id.
   * 
   * @param aParser - the given parser
   * @param aId - the given id
   */
  public JJNode(final JavaCCParser aParser, final int aId) {
    this(aId);
    parser = aParser;
  }

  /*
   * SimpleNode methods.
   */

  /**
   * Empty open method. Comes from SimpleNode.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public void jjtOpen() {
    // nothing done here
  }

  /**
   * Empty close method. Comes from SimpleNode.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public void jjtClose() {
    // nothing done here
  }

  /**
   * Sets the parent. Comes from SimpleNode.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public void jjtSetParent(final Node aParent) {
    parent = aParent;
  }

  /**
   * Gets the parent. Comes from SimpleNode.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public Node jjtGetParent() {
    return parent;
  }

  /**
   * Adds a child. Comes from SimpleNode.
   * <p>
   * {@inheritDoc}
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
   * <p>
   * {@inheritDoc}
   */
  @Override
  public Node jjtGetChild(final int i) {
    return children[i];
  }

  /**
   * Gets the node's number of children. Comes from SimpleNode.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public int jjtGetNumChildren() {
    return (children == null) ? 0 : children.length;
  }

  /*
   * Specific methods
   */

  /**
   * @return the parent
   */
  public JJNode getParent() {
    if (parent instanceof JJNode) {
      return (JJNode) parent;
    }
    return null;
  }

  /**
   * @return Returns the fake node to display an out of hierarchy selection
   */
  public static final JJNode getOohsjjnode() {
    return oohsJJNode;
  }

  /**
   * @return the array of the node's children
   */
  public Node[] getChildren() {
    return children;
  }

  /**
   * @return the id
   */
  public int getId() {
    return id;
  }

  /**
   * @return the node's first token line number
   */
  public int getBeginLine() {
    return first.beginLine;
  }

  /**
   * @return the node's first token column number
   */
  public int getBeginColumn() {
    return first.beginColumn;
  }

  /**
   * @return the node's last token line number
   */
  public int getEndLine() {
    return last.endLine;
  }

  /**
   * @return the node's last token column number
   */
  public int getEndColumn() {
    return last.endColumn;
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
   * @param aToken - the first token
   */
  public void setFirstToken(final Token aToken) {
    first = aToken;
  }

  /**
   * Sets the node's last token.
   * 
   * @param aToken - the last token
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
   * <p>
   * {@inheritDoc}
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
   * Returns the node's name.
   * 
   * @return the node's name
   */
  public String getName() {
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
   * Returns the node's name column.
   * 
   * @return the node's name column
   */
  public Token getNameToken() {
    if (nameToken == null) {
      setNames();
    }
    return nameToken;
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
   * <p>
   * Note : we could probably simplify this by using more appropriate and more appropriately JJTree nodes
   */
  public void setNames() {
    // JJTVOID
    // JJTROOT
    // JJTPARSER_BEGIN
    // JJTJAVACC_OPTIONS
    // JJTJAVACODE_BLOCK
    // JJTBNF_PROD_JAVA_BLOCK
    // JJTBNF_PROD_EXP_BLOCK
    // JJTREG_EXPR_PROD_BLOCK
    // JJTTOKEN_MANAGER_DECLS
    // JJTREGEXPR_KIND
    // JJTIDENT_REG_EXPR_LABEL
    // JJTJAVAIDENTINCLAORINTDECL
    // JJTJAVAIDENTINENUMDECL
    // JJTJAVAIDENTINMETHODDECL
    // JJTJAVAIDENTINCONSTRDECL
    // JJTJAVAIDENTINANNOTTYPEDECL
    name = displayName = first.image;
    nameToken = first;

    if (id == JJTIDENT_IN_PARSER || id == JJTIDENT_IN_EXP_UNIT || id == JJTIDENT_IN_REG_EXPR
        || id == JJTIDENT_IN_COMP_REG_EXPR_UNIT) {
      name = displayName = first.image;
      nameToken = first;
      // add the optional no node creation flag
      final int parId = ((JJNode) parent).getId();
      if (parId == JJTBNF_PROD_EXP_BLOCK) {
        // skip arguments or last ">"
        Token f = first.next;
        final int fk = f.kind;
        if (fk == LPAREN || fk == GT) {
          f = f.next;
          if (fk == LPAREN) {
            while (f.kind != RPAREN) {
              f = f.next;
            }
            f = f.next;
          }
          if (f.kind == BANG) {
            displayName += " !"; //$NON-NLS-1$
          }
        }
      }
    }
    else if (id == JJTROOT) {
      name = displayName = "ASTroot"; //$NON-NLS-1$
      nameToken = null;
    }
    else if (id == JJTOPTION_BINDING) {
      // Options option_binding => 'option name'
      Token f = first;
      while (f != last && f.kind != IDENTIFIER && f.kind != _LOOKAHEAD && f.kind != _IGNORE_CASE) {
        f = f.next;
      }
      name = displayName = f.image;
      nameToken = f;
    }
    else if (id == JJTCLAORINTDECL || id == JJTENUMDECL) {
      // 'identifier' after "class", "interface" or "enum"
      name = displayName = first.next.image;
      nameToken = first.next;
    }
    else if (id == JJTANNOTTYPEDECL) {
      // 'identifier' after "@" "interface"
      name = displayName = first.next.next.image;
      nameToken = first.next.next;
    }
    else if (id == JJTMETHODDECL || id == JJTCONSTRDECL) {
      // Parser MethodDeclaration or ConstructorDeclation => 'method name'
      Token f = first;
      while (f != last && f.next.kind != LPAREN) {
        f = f.next;
      }
      name = displayName = f.image;
      nameToken = f;
    }
    else if (id == JJTBNF_PROD) {
      // Bnf production => 'identifier name' or 'identifier name - #node name'
      Token f = first;
      while (f != last && f.next.kind != LPAREN) {
        f = f.next;
      }
      name = displayName = f.image;
      nameToken = f;
      while (f != last && f.next.kind != COLON) {
        f = f.next;
        // add the optional no node creation flag
        if (f.kind == BANG) {
          displayName += " !"; //$NON-NLS-1$
          f = f.next;
        }
        else if (f.kind == SHARP) {
          displayName += DASH_SEP + f.image + f.next.image;
        }
      }
    }
    else if (id == JJTJAVACODE_PROD) {
      // Javacode production => 'method name' or 'method name - #node name'
      Token f = first;
      while (f != last && f.next.kind != LPAREN) {
        f = f.next;
      }
      name = displayName = f.image;
      nameToken = f;
      while (f != last && f.next.kind != LBRACE) {
        f = f.next;
        // add the optional no node creation flag
        if (f.kind == REM) {
          displayName += " %"; //$NON-NLS-1$
          f = f.next;
        }
        else if (f.kind == SHARP) {
          displayName += DASH_SEP + f.image + f.next.image;
        }
      }
    }
    else if (id == JJTREGULAR_EXPR_PROD) {
      // Regular expression => 'kind name' or 'kind name : all lexical states'
      Token f = first;
      String lexState = null;
      if (f.kind == LT) {
        // get the lexical states
        f = f.next;
        lexState = COLON_SEP;
        while (f != last && f.kind != GT) {
          lexState += f.image;
          f = f.next;
        }
        f = f.next;
      }
      name = displayName = f.image;
      nameToken = f;
      if (lexState != null) {
        displayName += lexState;
      }
    }
    else if (id == JJTREGEXPR_SPEC) {
      // Token => 'token name' or 'token name : lexical state'
      // (keep "#" plus name for private label identifier)
      boolean foundLT = false;
      Token f = first;
      while (f != last) {
        if (f.kind == LT) {
          foundLT = true;
          f = f.next;
          break;
        }
        f = f.next;
      }
      if (foundLT) {
        // found a "<"
        if (f.kind == SHARP) {
          // "<" "#" < IDENTIFIER > ":" complex_regular_expression_choices ">
          name = displayName = f.image;
          f = f.next;
          nameToken = f;
          name = displayName += f.image;
        }
        else if (f.kind == IDENTIFIER) {
          // "<" < IDENTIFIER > ":" complex_regular_expression_choices "> or "<" < IDENTIFIER > ">
          name = displayName = f.image;
          nameToken = f;
        }
        else if (f.kind == _EOF) {
          // "<" "EOF" ">"
          name = displayName = "< EOF >"; //$NON-NLS-1$
          nameToken = null;
        }
        else {
          // "<" complex_regular_expression_choices ">"
          name = displayName = "< ... >"; //$NON-NLS-1$
          nameToken = null;
        }
        // skip up to last ">"
        int lvl = 1;
        while (f != last && lvl != 0) {
          if (f.kind == LT) {
            lvl++;
          }
          else if (f.kind == GT) {
            lvl--;
          }
          f = f.next;
        }
      }
      else {
        // no "<" found, it's a single StringLiteral ; back to the beginning
        name = displayName = first.image;
        nameToken = f;
        f = first.next;
      }
      // add the optional no node creation flag
      if (f.kind == BANG) {
        displayName += " !"; //$NON-NLS-1$
        f = f.next;
      }
      // skip optional java bloc
      if (f.kind == LBRACE) {
        int lvl = 1;
        f = f.next;
        while (f != last && lvl != 0) {
          if (f.kind == LBRACE) {
            lvl++;
          }
          else if (f.kind == RBRACE) {
            lvl--;
          }
          f = f.next;
        }
      }
      // set lexical state
      String lexState = null;
      if (f.kind == COLON) {
        f = f.next;
        lexState = COLON_SEP + f.image;
      }
      if (lexState != null) {
        displayName += lexState;
      }
    }
    else if (id == JJTIDENT_REG_EXPR_PRIVATE_LABEL) {
      name = displayName = "#" + first.image; //$NON-NLS-1$
      nameToken = first;
    }
    else if (id == JJTIDENT_BNF_DECL) {
      name = first.image;
      nameToken = first;
      displayName = ((JJNode) parent).displayName;
    }
    else if (id == JJTNODE_DESC_IN_EXP || id == JJTNODE_DESC_IN_METH) {
      name = displayName = first.image + first.next.image;
      nameToken = first;
    }
    else if (id == JJTNODE_DESC_BNF_DECL) {
      name = first.image + first.next.image;
      displayName = ((JJNode) parent).displayName;
      nameToken = first;
    }
  }

  /**
   * Builds recursively the maps in JavaCC elements given the node at the root of parse tree.
   * 
   * @param aJjElements - the JavaCC elements
   */
  public void buildElements(final Elements aJjElements) {
    setNames();
    aJjElements.addNode(name, this);
    if (children != null) {
      for (final Node child : children) {
        final JJNode n = (JJNode) child;
        n.buildElements(aJjElements);
      }
    }
    return;
  }

  /**
   * Adds the node to the stack if it is not a block, or its children if is a block, recursively.<br>
   * TODO voir à ajouter les références aux classes/méthodes/... dans les blocks
   * 
   * @param aStack - the stack
   * @param aChild - a node
   */
  void addNonBlockChild(final Queue<JJNode> aStack, final Node aChild) {
    final JJNode ch = (JJNode) aChild;
    final int chId = ch.id;
    if (chId == JJTBNF_PROD_EXP_BLOCK || chId == JJTBNF_PROD_JAVA_BLOCK || chId == JJTJAVACODE_BLOCK
        || chId == JJTREG_EXPR_PROD_BLOCK) {
      if (ch.children != null) {
        for (final Node son : ch.children) {
          addNonBlockChild(aStack, son);
        }
      }
    }
    aStack.offer(ch);
  }

  /**
   * Builds the array of callers for this node.
   */
  public void buildCallers() {
    // clear callers
    callers = new JJNode[0];
    String sel = name;
    // for java code, build first elements directly from the parents
    if (id == JJTMETHODDECL //
        || id == JJTCONSTRDECL //
        || id == JJTCLAORINTDECL //
        || id == JJTCONSTRDECL //
        || id == JJTENUMDECL //
        || id == JJTANNOTTYPEDECL //
    ) {
      // record the parent
      addCaller((JJNode) parent);
    }

    // get up to the root
    JJNode root = this;
    while (root.parent != null) {
      root = (JJNode) root.parent;
    }
    // search the whole tree from the root
    final Queue<JJNode> stack = new LinkedList<JJNode>();
    stack.offer(root);
    // take the display name for node descriptors and private label identifiers for later comparison
    if (id == JJTNODE_DESC_BNF_DECL //
        || id == JJTNODE_DESC_IN_EXP //
        || id == JJTNODE_DESC_IN_METH || id == JJTIDENT_REG_EXPR_PRIVATE_LABEL //
    ) {
      sel = displayName;
    }
    // examine each element of the stack
    while (!stack.isEmpty()) {
      final JJNode nd = stack.remove();
      final int ndId = nd.getId();
      final String ndName = nd.name;
      if (sel.equals(ndName)) {
        if (ndId == JJTIDENT_REG_EXPR_LABEL //
            || ndId == JJTIDENT_REG_EXPR_PRIVATE_LABEL //
            || ndId == JJTIDENT_IN_COMP_REG_EXPR_UNIT //
            || ndId == JJTIDENT_IN_REG_EXPR // 
            || ndId == JJTIDENT_IN_EXP_UNIT //
            || ndId == JJTNODE_DESC_IN_EXP //
        ) {
          final JJNode ndGrandParent = (JJNode) (((JJNode) nd.parent)).parent;
          if (ndGrandParent.id == JJTBNF_PROD) {
            // found a caller (the grand parent), record it
            addCaller(ndGrandParent);
            // for JJTIDENT_IN_COMP_REG_EXPR_UNIT, see if it is defined in a regular expression label
            if (ndId == JJTIDENT_IN_COMP_REG_EXPR_UNIT) {
              final JJNode brother = (JJNode) ((JJNode) nd.parent).children[0];
              final int brId = brother.id;
              if (brId == JJTIDENT_REG_EXPR_LABEL || brId == JJTIDENT_REG_EXPR_PRIVATE_LABEL) {
                addCaller(brother);
              }
            }
          }
          else if (ndGrandParent.id == JJTREG_EXPR_PROD_BLOCK) {
            // Found a caller (the grand grand parent, the JJTREGEXPR_KIND), record it
            addCaller((JJNode) ndGrandParent.parent);
            // for JJTIDENT_IN_COMP_REG_EXPR_UNIT, see if it is defined in a regular expression label
            if (ndId == JJTIDENT_IN_COMP_REG_EXPR_UNIT) {
              final JJNode brother = (JJNode) ((JJNode) nd.parent).children[0];
              final int brId = brother.id;
              if (brId == JJTIDENT_REG_EXPR_LABEL || brId == JJTIDENT_REG_EXPR_PRIVATE_LABEL) {
                addCaller(brother);
              }
            }
          }
        }
        else if ((ndId == JJTBNF_PROD && id != JJTIDENT_IN_EXP_UNIT && id != JJTBNF_PROD && id != JJTIDENT_BNF_DECL)//
                 || ndId == JJTNODE_DESC_BNF_DECL //
                 || ndId == JJTNODE_DESC_IN_METH //
        ) {
          // found a caller (the node), record it
          addCaller(nd);
        }
      } // end if (sel.equals(ndName))
      // if it has children, push them if they are not blocks or push their children if they are blocks
      if (nd.children != null) {
        for (final Node child : nd.children) {
          addNonBlockChild(stack, child);
        }
      }
    } // end while
  } // end buildCallers()

  /**
   * Adds a given node at the end of the array of callers of this node only if it does not exist yet in the
   * array.<br>
   * 
   * @param aNode - the node to add
   */
  public void addCaller(final JJNode aNode) {
    for (final JJNode caller : callers) {
      if (caller == aNode) {
        return;
      }
    }
    final JJNode newCallers[] = new JJNode[callers.length + 1];
    System.arraycopy(callers, 0, newCallers, 0, callers.length);
    newCallers[callers.length] = aNode;
    callers = newCallers;
  }

  /**
   * @return the array of callers for this node
   */
  public JJNode[] getCallers() {
    return callers;
  }

  /**
   * Resets the array of callers for this node with the fake "out of hierarchy selection" node.
   */
  public void resetCallersToOohsJJNode() {
    callers = new JJNode[1];
    callers[0] = oohsJJNode;
  }

  /**
   * Builds the array of callees for this node.
   * 
   * @param aElements - the JJ elements
   */
  public void buildCallees(final Elements aElements) {
    // clear callees
    callees = new JJNode[0];
    // Get the node to which this node belongs
    String declName = name;
    if (id == JJTCONSTRDECL) {
      // TODO see if in Java 7/8 we can have something
      return;
    }
    if (id == JJTNODE_DESC_BNF_DECL) {
      declName = declName.substring(1) + DASH_SEP + declName;
    }
    final JJNode declNode = aElements.getNonIdentNorNodeDesc(declName);
    if (declNode == null) {
      return;
    }
    // search callees within the declaration node tree
    final Queue<JJNode> stack = new LinkedList<JJNode>();
    // if it has children, push them if they are not a block or push their children if they are a block
    if (declNode.children != null) {
      for (final Node child : declNode.children) {
        addNonBlockChild(stack, child);
      }
    }
    // examine each element of the stack
    while (!stack.isEmpty()) {
      final JJNode nd = stack.remove();
      // take all JJTree identifier and node descriptor nodes and declarations
      final int ndId = nd.getId();
      if (ndId == JJTIDENT_BNF_DECL //
          || ndId == JJTIDENT_REG_EXPR_LABEL //
          || ndId == JJTIDENT_REG_EXPR_PRIVATE_LABEL //
          || ndId == JJTIDENT_IN_EXP_UNIT //
          || ndId == JJTIDENT_IN_REG_EXPR //
          || ndId == JJTIDENT_IN_COMP_REG_EXPR_UNIT //
          //          || ndId == JJTNODE_DESC_BNF_DECL //
          || ndId == JJTNODE_DESC_IN_EXP //
          || ndId == JJTNODE_DESC_IN_METH //
          || ndId == JJTPARSER_BEGIN //
          || ndId == JJTCLAORINTDECL //
          || ndId == JJTMETHODDECL //
          || ndId == JJTCONSTRDECL //
          || ndId == JJTENUMDECL //
          || ndId == JJTANNOTTYPEDECL //
      ) {
        // get its parent
        final JJNode ndParent = (JJNode) nd.jjtGetParent();
        // Skip the node if it is the first child of its parent for some cases
        if ((ndParent.children[0] == nd) //
            && (ndId == JJTIDENT_BNF_DECL //
                || ndId == JJTIDENT_REG_EXPR_LABEL //
            || ndId == JJTIDENT_REG_EXPR_PRIVATE_LABEL //
            )) {
          continue;
        }
        // found a callee, record it
        addCallee(nd);
      }
    }
    return;
  } // end buildCallees()

  /**
   * Adds a given node at the end of the array of callees of this node.
   * 
   * @param aNode - the node to add
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
   * Resets the array of callees for this node with the fake "out of hierarchy selection" node.
   */
  public void resetCalleesToOohsJJNode() {
    callees = new JJNode[1];
    callees[0] = oohsJJNode;
  }

}
