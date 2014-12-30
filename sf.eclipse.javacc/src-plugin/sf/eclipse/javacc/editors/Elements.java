package sf.eclipse.javacc.editors;

import static sf.eclipse.javacc.parser.JavaCCParserTreeConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sf.eclipse.javacc.handlers.GotoRule;
import sf.eclipse.javacc.handlers.ShowCallHierarchy;
import sf.eclipse.javacc.parser.JJNode;
import sf.eclipse.javacc.parser.JavaCCParserTreeConstants;
import sf.eclipse.javacc.parser.Node;
import sf.eclipse.javacc.parser.Token;

/**
 * Maps of JavaCC elements for one Editor. Used to navigate.
 * <p>
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014
 */
public class Elements {

  // MMa 11/2009 : javadoc and formatting and renaming revision
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 08/2011 : enhanced Call Hierarchy View (changed selection key) + refactoring
  // MMa 10/2012 : added compPropsMap for completion proposals, used static import ;
  //               adapted to modifications in grammar nodes ; renamed
  // MMa 11/2014 : removed old commented code ; added / removed some final modifiers

  /**
   * The map of node keys (line number + image) to nodes for identifier or node descriptor nodes<br>
   * Used by {@link ShowCallHierarchy}
   */
  protected final Map<String, JJNode> identOrNodeDescMap     = new HashMap<String, JJNode>(30);

  /**
   * The map of node keys (image) to nodes for non identifier nor node descriptor nodes<br>
   * Used by {@link GotoRule}, {@link TextHover}, {@link JJNode}
   */
  protected final Map<String, JJNode> nonIdentNorNodeDescMap = new HashMap<String, JJNode>();

  /**
   * The map of node keys (image) to nodes for the completion proposals<br>
   * Used by {@link CompletionProcessor}
   */
  protected final Map<String, JJNode> compPropsIdentMap      = new HashMap<String, JJNode>(30);

  /** The list of nodes for completion proposals ; will be naturally sorted by line numbers */
  protected final List<JJNode>        compPropsRangeList     = new ArrayList<JJNode>(30);

  /**
   * The list of nodes for the Outline Page and Call Hierarchy View ; will be naturally sorted by line numbers
   */
  protected final List<JJNode>        opchvList              = new ArrayList<JJNode>(30);

  /**
   * The map of node keys (image) to nodes for hyperlinks<br>
   * Used by {@link HyperlinkDetector}
   */
  protected final Map<String, JJNode> hyperlinksMap          = new HashMap<String, JJNode>(30);

  /** True if options node is present, false otherwise */
  protected boolean                   isOptionsThere         = false;
  /** True if parser_begin node is present, false otherwise */
  protected boolean                   isParserBeginThere     = false;

  /**
   * Empty constructor, as it should be constructed for each Editor.
   */
  public Elements() {
    // nothing done here
  }

  // The nodes tree is the following :
  //..root
  //....javacc_options (opt)
  //......option_binding (ListOpt)
  //....parser_begin
  //......ident_in_parser
  //......ClaOrIntDecl (ListOpt)
  //........JavaIdentInClaOrIntDecl
  //........MethodDecl (ListOpt)
  //..........JavaIdentInMethodDecl
  //..........node_desc_in_meth (opt)
  //........ConstrDecl
  //..........JavaIdentInConstrDecl
  //......EnumDecl (ListOpt)
  //........JavaIdentInEnumDecl
  //........MethodDecl (ListOpt)
  //..........JavaIdentInMethodDecl
  //..........node_desc_in_meth (opt)
  //........ConstrDecl
  //..........JavaIdentInConstrDecl
  //......AnnotTypeDecl (ListOpt)
  //........JavaIdentInAnnotTypeDecl
  //........MethodDecl (ListOpt)
  //..........JavaIdentInMethodDecl
  //..........node_desc_in_meth (opt)
  //....javacode_prod (ListOpt)
  //......MethodDecl
  //........JavaIdentInMethodDecl
  //........node_desc_in_meth (opt)
  //........javacode_block
  //....bnf_prod (ListOpt)
  //......ident_bnf_decl
  //......node_desc_bnf_decl (opt)
  //......bnf_prod_java_block
  //......bnf_prod_exp_block
  //........ident_reg_expr_label..or..ident_reg_expr_private_label (opt) then
  //........ident_in_comp_reg_expr_unit
  //......or
  //........ident_in_reg_expr
  //......or
  //........ident_in_exp_unit..or..exp_unit_java_block
  //........node_desc_in_exp (opt)
  //....regular_expr_prod (ListOpt)
  //......regexpr_kind
  //......reg_expr_prod_block
  //........regexpr_spec
  //..........ident_reg_expr_label..or..ident_reg_expr_private_label (opt) then
  //..........ident_in_comp_reg_expr_unit
  //..........regexpr_spec_java_block
  //........or
  //..........ident_in_reg_expr
  //..........regexpr_spec_java_block
  //........or
  //........ident_in_exp_unit..or..exp_unit_java_block
  //..........node_desc_in_exp (opt)
  //..........regexpr_spec_java_block
  //....token_manager_decls (ListOpt)
  //......MethodDecl (ListOpt)
  //........JavaIdentInMethodDecl
  //........node_desc_in_meth (opt)

  /**
   * Adds the node to the appropriate maps.
   * 
   * @param aImage - the node's image
   * @param aJJNode - the node
   */
  public void addNode(final String aImage, final JJNode aJJNode) {
    final int ndId = aJJNode.getId();

    // the line number is prepended to the text to distinguish between multiples occurrences of the node

    switch (ndId) {
      case JJTPARSER_BEGIN:
        nonIdentNorNodeDescMap.put(aImage, aJJNode);
        compPropsRangeList.add(aJJNode);
        isParserBeginThere = true;
        break;
      case JJTJAVACC_OPTIONS:
        nonIdentNorNodeDescMap.put(aImage, aJJNode);
        compPropsRangeList.add(aJJNode);
        isOptionsThere = true;
        break;
      case JJTOPTION_BINDING:
      case JJTREGEXPR_KIND:
      case JJTREGEXPR_SPEC:
        nonIdentNorNodeDescMap.put(aImage, aJJNode);
        break;
      case JJTIDENT_IN_PARSER:
      case JJTNODE_DESC_IN_METH:
      case JJTNODE_DESC_IN_EXP:
      case JJTIDENT_IN_EXP_UNIT:
      case JJTIDENT_IN_REG_EXPR:
      case JJTIDENT_IN_COMP_REG_EXPR_UNIT:
        //        identOrNodeDescMap.put(getNodeBeginOffset(aJJNode) + aImage, aJJNode);
        identOrNodeDescMap.put(getNodeBeginLine(aJJNode) + aImage, aJJNode);
        break;
      case JJTJAVACODE_PROD:
      case JJTREGULAR_EXPR_PROD:
        nonIdentNorNodeDescMap.put(aImage, aJJNode);
        opchvList.add(aJJNode);
        compPropsRangeList.add(aJJNode);
        break;
      case JJTMETHODDECL:
      case JJTCLAORINTDECL:
      case JJTENUMDECL:
      case JJTCONSTRDECL:
      case JJTANNOTTYPEDECL:
        nonIdentNorNodeDescMap.put(aImage, aJJNode);
        compPropsIdentMap.put(aImage, aJJNode);
        break;
      case JJTBNF_PROD:
        nonIdentNorNodeDescMap.put(aImage, aJJNode);
        opchvList.add(aJJNode);
        compPropsIdentMap.put(aImage, aJJNode);
        compPropsRangeList.add(aJJNode);
        break;
      case JJTIDENT_BNF_DECL:
        //      identOrNodeDescMap.put(getNodeBeginOffset(aJJNode) + aImage, aJJNode);
        identOrNodeDescMap.put(getNodeBeginLine(aJJNode) + aImage, aJJNode);
        hyperlinksMap.put(aImage, aJJNode);
        break;
      case JJTNODE_DESC_BNF_DECL:
        String subImage = aImage.substring(1);
        //      identOrNodeDescMap.put(getNodeBeginOffset(aJJNode) + aImage, aJJNode);
        identOrNodeDescMap.put(getNodeBeginLine(aJJNode) + aImage, aJJNode);
        // take also the identifier alone on its line in case the "#" will not be part of the selection
        final Token t = aJJNode.getFirstToken().next;
        identOrNodeDescMap.put(t.beginLine + t.image, aJJNode.getParent());
        // for when node descriptor name is different that the identifier name, put an extra bnf production mapping
        nonIdentNorNodeDescMap.put(subImage + JJNode.DASH_SEP + aImage, aJJNode.getParent());
        hyperlinksMap.put(aImage, (JJNode) aJJNode.getParent().jjtGetChild(0));
        // if node descriptor name is different that the identifier name, put an extra bnf production mapping
        hyperlinksMap.put(subImage, aJJNode.getParent());
        break;
      case JJTBNF_PROD_JAVA_BLOCK:
      case JJTBNF_PROD_EXP_BLOCK:
        compPropsRangeList.add(aJJNode);
        break;
      case JJTIDENT_REG_EXPR_LABEL:
        //      identOrNodeDescMap.put(getNodeBeginOffset(aJJNode) + aImage, aJJNode);
        identOrNodeDescMap.put(getNodeBeginLine(aJJNode) + aImage, aJJNode);
        compPropsIdentMap.put(aImage, aJJNode);
        hyperlinksMap.put(aImage, aJJNode);
        break;
      case JJTIDENT_REG_EXPR_PRIVATE_LABEL:
        subImage = aImage.substring(1);
        //      identOrNodeDescMap.put(getNodeBeginOffset(aJJNode) + aImage, aJJNode);
        identOrNodeDescMap.put(getNodeBeginLine(aJJNode) + aImage, aJJNode);
        // take also the identifier alone in case the "#" will not be part of the selection
        //      identOrNodeDescMap.put(getNodeBeginOffset(aJJNode) + subImage, aJJNode);
        identOrNodeDescMap.put(getNodeBeginLine(aJJNode) + subImage, aJJNode);
        if (aImage.charAt(0) == '#') {
        }
        else {
          compPropsIdentMap.put(aImage, aJJNode);
        }
        hyperlinksMap.put(aImage, aJJNode);
        hyperlinksMap.put(subImage, aJJNode);
        break;
      case JJTJAVACODE_BLOCK:
      case JJTREG_EXPR_PROD_BLOCK:
      case JJTTOKEN_MANAGER_DECLS:
      case JJTREGEXPR_SPEC_JAVA_BLOCK:
      case JJTEXP_UNIT_JAVA_BLOCK:
        compPropsRangeList.add(aJJNode);
        break;
      case JJTJAVAIDENTINCLAORINTDECL:
      case JJTJAVAIDENTINENUMDECL:
      case JJTJAVAIDENTINMETHODDECL:
      case JJTJAVAIDENTINCONSTRDECL:
      case JJTJAVAIDENTINANNOTTYPEDECL:
        nonIdentNorNodeDescMap.put(aImage, aJJNode);
        hyperlinksMap.put(aImage, aJJNode);
        break;
      default: // (JJTVOID) && JJTROOT
        break;
    }
  }

  /**
   * Clears maps.
   */
  public void clear() {
    identOrNodeDescMap.clear();
    nonIdentNorNodeDescMap.clear();
    compPropsIdentMap.clear();
    compPropsRangeList.clear();
    opchvList.clear();
    hyperlinksMap.clear();
    isOptionsThere = isParserBeginThere = false;
  }

  //  /**
  //   * @return the identifier or node descriptor nodes map
  //   */
  //  public final HashMap<String, JJNode> getIdentOrNodeDescMap() {
  //    return identOrNodeDescMap;
  //  }

  /**
   * Returns the node from the identifier or node descriptor nodes map given its key (must be line + image or
   * line + '#' + image).<br>
   * These nodes are identified in JavaCC15.jjt with a node descriptor #identifier or #node_descriptor.
   * 
   * @param aKey - a key (must be line + image or line + '#' + image)
   * @return the JJNode
   */
  public final JJNode getIdentOrNodeDesc(final String aKey) {
    return identOrNodeDescMap.get(aKey);
  }

  //  /**
  //   * Tells whether the given key appears in the identifier or node descriptor nodes map.
  //   * 
  //   * @param aKey - a key (must be line + image or line + '#' + image)
  //   * @return true / false
  //   */
  //  public final boolean isIdentOrNodeDescElement(final String aKey) {
  //    return identOrNodeDescMap.containsKey(aKey);
  //  }

  //  /**
  //   * @return the non identifier nor node descriptor nodes map
  //   */
  //  public final HashMap<String, JJNode> getNonIdentNorNodeDescMap() {
  //    return nonIdentNorNodeDescMap;
  //  }

  /**
   * Returns the node from the non identifier nor node descriptor nodes map given its key (image).<br>
   * These nodes are identified in JavaCC15.jjt with a node descriptor '#xxx', except the #identifier and
   * #node_descriptor nodes which are not in this map (they are filtered and added in the other map).
   * 
   * @param aKey - a key
   * @return the JJNode
   */
  public final JJNode getNonIdentNorNodeDesc(final String aKey) {
    return nonIdentNorNodeDescMap.get(aKey);
  }

  /**
   * Tells whether the given key appears in the non identifier nor node descriptor nodes map.
   * 
   * @param aKey - a key
   * @return true / false
   */
  public final boolean isNonIdentNorNodeDescElement(final String aKey) {
    return nonIdentNorNodeDescMap.containsKey(aKey);
  }

  /**
   * @return the map of node keys (image) to nodes for the completion proposals
   */
  public final Map<String, JJNode> getCompPropsIdentMap() {
    return compPropsIdentMap;
  }

  /**
   * Returns the node from the map of node keys (image) to nodes for the completion proposals given its key
   * (image).<br>
   * 
   * @param aKey - a key
   * @return the JJNode
   */
  public final JJNode getCompProps(final String aKey) {
    return compPropsIdentMap.get(aKey);
  }

  /**
   * Tells whether the given key appears in the map of node keys (image) to nodes for the completion
   * proposals.
   * 
   * @param aKey - a key
   * @return true / false
   */
  public final boolean isCompPropsElement(final String aKey) {
    return compPropsIdentMap.containsKey(aKey);
  }

  /**
   * Returns the hyperlink target for a key.
   * 
   * @param aKey - a key (must be image without "#")
   * @return the JJNode
   */
  public final JJNode getHyperlinkTarget(final String aKey) {
    return hyperlinksMap.get(aKey);
  }

  /**
   * Tells whether the given key appears in the hyperlinks map.
   * 
   * @param aKey - a key (must be image without "#")
   * @return true / false
   */
  public final boolean isHyperlinkTarget(final String aKey) {
    return hyperlinksMap.containsKey(aKey);
  }

  /**
   * @param aLine - a line number
   * @return the top node in the outline list around the line number, or null if none
   */
  public JJNode getOpchvTopNodeFromLine(final int aLine) {
    // Eclipse numbers are 0-relative, JavaCC are 1-relative
    final int line = aLine + 1;
    for (final JJNode nd : opchvList) {
      final int bl = getNodeBeginLine(nd);
      final int el = getNodeEndLine(nd);
      if (line < bl) {
        // already passed
        return null;
      }
      else if (line > el) {
        // not yet
      }
      else {
        // between
        return nd;
      }
    }
    return null;
  }

  //  /**
  //   * @param aOffset - an offset
  //   * @return the node id of the node around the line & column numbers, or
  //   *         {@link JavaCCParserTreeConstants#JJTVOID} if none
  //   */
  //  public final int getCompPropsAroundNodeId(final int aOffset) {
  //    // Eclipse numbers are 0-relative, JavaCC are 1-relative
  //    final int offset = aOffset + 1;
  //    // bnf_prod are around bnf_prod_java_block and bnf_prod_exp_block
  //    // regular_expr_prod are around reg_expr_prod_block
  //    // so we have to find if we are in the inner or outer nodes
  //    boolean inOuter = false;
  //    int outerId = JJTVOID;
  //    for (final JJNode nd : compPropsRangeList) {
  //      final int ndId = nd.getId();
  //      if (!inOuter && ndId != JJTPARSER_BEGIN && ndId != JJTJAVACC_OPTIONS && ndId != JJTJAVACODE_PROD
  //          && ndId != JJTBNF_PROD && ndId != JJTREGULAR_EXPR_PROD && ndId != JJTTOKEN_MANAGER_DECLS) {
  //        // skip inner nodes if not already in a top level outer node
  //        continue;
  //      }
  //      final int bo = getNodeBeginOffset(nd);
  //      final int eo = getNodeEndOffset(nd);
  //      if (offset <= bo) {
  //        // the node has already passed us
  //        // if == we are just before the beginning of the node but outside
  //        return outerId;
  //      }
  //      else {
  //        // we are after the first beginning of the node
  //        // if < we are clearly between the beginning and end of the node
  //        // if == we are just at the end of the node but inside
  //        if (offset <= eo) {
  //          outerId = ndId;
  //          if (isInEnglobingNode(outerId)) {
  //            inOuter = true;
  //          }
  //          else {
  //            return outerId;
  //          }
  //        }
  //        else {
  //          // the node line is before us, see the next node
  //        }
  //      }
  //    }
  //    return outerId;
  //  }

  /**
   * @param aLine - a line number
   * @param aColumn - a column number
   * @return the node id of the node around the line & column numbers, or
   *         {@link JavaCCParserTreeConstants#JJTVOID} if none
   */
  public int getEnclosingNodeId(final int aLine, final int aColumn) {
    // Eclipse numbers are 0-relative, JavaCC are 1-relative
    final int line = aLine + 1;
    final int col = aColumn + 1;
    // bnf_prod are around bnf_prod_java_block and bnf_prod_exp_block
    // regular_expr_prod are around reg_expr_prod_block
    // so we have to find if we are in the inner or outer nodes
    boolean inOuter = false;
    int outerId = JJTVOID;
    for (final JJNode nd : compPropsRangeList) {
      final int ndId = nd.getId();
      if (!inOuter && ndId != JJTPARSER_BEGIN && ndId != JJTJAVACC_OPTIONS && ndId != JJTJAVACODE_PROD
          && ndId != JJTBNF_PROD && ndId != JJTREGULAR_EXPR_PROD && ndId != JJTTOKEN_MANAGER_DECLS) {
        // skip inner nodes if not already in a top level outer node
        continue;
      }
      final int bl = getNodeBeginLine(nd);
      final int el = getNodeEndLine(nd);
      if (bl == el) {
        // node on one line
        if (line < bl) {
          // the node on one line has already passed us
          return outerId;
        }
        else if (line == bl) {
          // the node on one line is on our line
          final int bc = getNodeBeginColumn(nd);
          if (col >= bc && col <= getNodeEndColumn(nd)) {
            // we are within the columns of the node on one line
            outerId = ndId;
            if (isInEnglobingNode(outerId)) {
              // see the next node
              inOuter = true;
            }
            else {
              return outerId;
            }
          }
          else {
            // we are outside the columns of the node on one line
            if (!inOuter) {
              return JJTVOID;
            }
          }
        }
        else {
          // the node on one line is before us, see the next node
        }
      }
      else {
        // node on more than one line
        if (line < bl) {
          // the node on more than one line has already passed us
          return outerId;
        }
        else if (line == bl) {
          // we are on the first line of the node on more than one line
          final int bc = getNodeBeginColumn(nd);
          if (col > bc) {
            // we are just after the beginning of the node on more than one line
            outerId = ndId;
            if (isInEnglobingNode(outerId)) {
              inOuter = true;
            }
            else {
              return outerId;
            }
          }
          else {
            // we are just before the beginning of the node on more than one line
            return outerId;
          }
        }
        else {
          // we are after the first line of the node on more than one line
          if (line < el) {
            // we are clearly between the first and last line of the node on more than one line
            outerId = ndId;
            if (isInEnglobingNode(outerId)) {
              inOuter = true;
            }
            else {
              return outerId;
            }
          }
          else if (line == el) {
            // we are on the last line of the node on more than one line
            final int ndCol = getNodeEndColumn(nd);
            if (col <= ndCol) {
              // we are just before the end of the node on more than one line
              outerId = ndId;
              if (isInEnglobingNode(outerId)) {
                inOuter = true;
              }
              else {
                return outerId;
              }
            }
            else {
              // we are just after the end of the node on more than one line
              if (!inOuter) {
                return JJTVOID;
              }
            }
          }
          else {
            // the node on more than one line is before us, see the next node
          }
        }
      }
    }
    return outerId;
  }

  /**
   * @param aNodeId - a node id
   * @return true if the node id is one of an englobing node (a node that contains others), false otherwise
   */
  private boolean isInEnglobingNode(final int aNodeId) {
    switch (aNodeId) {
      case JJTBNF_PROD:
      case JJTBNF_PROD_EXP_BLOCK:
      case JJTREGULAR_EXPR_PROD:
      case JJTREGEXPR_SPEC_JAVA_BLOCK:
        return true;
      default:
        return false;
    }
  }

  /**
   * @param aNode - a node
   * @return the node's begin line, fixed for bnf_prod_exp_block
   */
  private int getNodeBeginLine(final JJNode aNode) {
    if (aNode.getId() != JJTBNF_PROD_EXP_BLOCK) {
      return aNode.getBeginLine();
    }
    else {
      // get previous "{", which is the token after the brother bnf_prod_java_block
      final Node[] brothers = aNode.getParent().getChildren();
      final JJNode bpjb = (JJNode) brothers[brothers.length - 2];
      final Token lastOfBpjb = bpjb.getLastToken();
      return lastOfBpjb.next.beginLine;
    }
  }

  /**
   * @param aNode - a node
   * @return the node's begin column, fixed for bnf_prod_exp_block
   */
  private int getNodeBeginColumn(final JJNode aNode) {
    if (aNode.getId() != JJTBNF_PROD_EXP_BLOCK) {
      return aNode.getBeginColumn();
    }
    else {
      // get previous "{", which is the token after the brother bnf_prod_java_block
      final Node[] brothers = aNode.getParent().getChildren();
      final JJNode bpjb = (JJNode) brothers[brothers.length - 2];
      final Token lastOfBpjb = bpjb.getLastToken();
      return lastOfBpjb.next.beginColumn;
    }
  }

  /**
   * @param aNode - a node
   * @return the node's end line, fixed for bnf_prod_exp_block
   */
  private int getNodeEndLine(final JJNode aNode) {
    if (aNode.getId() != JJTBNF_PROD_EXP_BLOCK) {
      return aNode.getEndLine();
    }
    else {
      // get next "}", which is the last token of the parent bnf_prod
      return aNode.getParent().getEndLine();
    }
  }

  /**
   * @param aNode - a node
   * @return the node's end column, fixed for bnf_prod_exp_block
   */
  private int getNodeEndColumn(final JJNode aNode) {
    if (aNode.getId() != JJTBNF_PROD_EXP_BLOCK) {
      return aNode.getEndColumn();
    }
    else {
      // get next "}", which is the last token of the parent bnf_prod
      return aNode.getParent().getEndColumn();
    }
  }

}
