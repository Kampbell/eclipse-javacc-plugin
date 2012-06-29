package sf.eclipse.javacc.editors;

import java.util.HashMap;

import sf.eclipse.javacc.parser.JJNode;
import sf.eclipse.javacc.parser.JavaCCParserTreeConstants;
import sf.eclipse.javacc.parser.Token;

/**
 * Maps of JavaCC elements for one Editor. Used to navigate between declarations.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012
 */
public class JJElements implements JavaCCParserTreeConstants {

  // MMa 11/2009 : javadoc and formatting and renaming revision
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 08/2011 : enhanced Call Hierarchy view (changed selection key) + refactoring

  /** The map of node keys (image) to nodes for non identifier nor node descriptor nodes */
  private final HashMap<String, JJNode> nonIdentNorNodeDescMap = new HashMap<String, JJNode>();
  /** The map of node keys (line number + image) to nodes for identifier or node descriptor nodes */
  private final HashMap<String, JJNode> identOrNodeDescMap     = new HashMap<String, JJNode>();
  /** The map of node keys (image) to nodes for hyperlinks */
  private final HashMap<String, JJNode> hyperlinksMap          = new HashMap<String, JJNode>();

  /**
   * Empty constructor, as it should be constructed for each Editor.
   */
  public JJElements() {
    // nothing done here
  }

  /**
   * Adds the node either to the identifier or node descriptor nodes or non identifier nor node descriptor
   * nodes maps.
   * 
   * @param aImage - the node's image
   * @param aJJNode - the node
   */
  public final void put(final String aImage, final JJNode aJJNode) {
    final int ndId = aJJNode.getId();

    // the line is prepended to the text to distinguish between multiples occurrences of the node
    // TODO this does not work for cases where 2 or more occurrences of the same node appear on the same line
    if (ndId == JJTIDENT_USE || ndId == JJTIDENT_BNF_DECL || ndId == JJTIDENT_REG_EXPR_LABEL
        || ndId == JJTNODE_DESC_IN_EXP || ndId == JJTNODE_DESC_IN_METH) {
      identOrNodeDescMap.put(aJJNode.getBeginLine() + aImage, aJJNode);
    }
    else if (ndId == JJTIDENT_REG_EXPR_PRIVATE_LABEL) {
      identOrNodeDescMap.put(aJJNode.getBeginLine() + aImage, aJJNode);
      // take also the identifier alone in case the "#" will not be part of the selection
      identOrNodeDescMap.put(aJJNode.getBeginLine() + aImage.substring(1), aJJNode);
    }
    else if (ndId == JJTNODE_DESC_BNF_DECL) {
      identOrNodeDescMap.put(aJJNode.getBeginLine() + aImage, aJJNode);
      // take also the identifier alone on its line in case the "#" will not be part of the selection
      final Token t = aJJNode.getFirstToken().next;
      identOrNodeDescMap.put(t.beginLine + t.image, aJJNode.getParent());
      // if node descriptor name is different that the identifier name, put an extra bnf production mapping
      nonIdentNorNodeDescMap.put(aImage.substring(1) + JJNode.DASH_SEP + aImage, aJJNode.getParent());
    }
    else {
      nonIdentNorNodeDescMap.put(aImage, aJJNode);
    }

    if (ndId == JJTIDENT_BNF_DECL || ndId == JJTIDENT_REG_EXPR_LABEL
        || ndId == JJTJAVAIDENTIFIERINCLASSORINTERFACEDECLARATION
        || ndId == JJTJAVAIDENTIFIERINMETHODDECLARATOR) {
      hyperlinksMap.put(aImage, aJJNode);
    }
    else if (ndId == JJTIDENT_REG_EXPR_PRIVATE_LABEL) {
      hyperlinksMap.put(aImage, aJJNode);
      hyperlinksMap.put(aImage.substring(1), aJJNode);
    }
    else if (ndId == JJTNODE_DESC_BNF_DECL) {
      hyperlinksMap.put(aImage, (JJNode) aJJNode.getParent().jjtGetChild(0));
      // if node descriptor name is different that the identifier name, put an extra bnf production mapping
      hyperlinksMap.put(aImage.substring(1), aJJNode.getParent());
    }
  }

  /**
   * Clears both maps.
   */
  public final void clear() {
    nonIdentNorNodeDescMap.clear();
    identOrNodeDescMap.clear();
  }

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
   * Returns the node from the identifier or node descriptor nodes map given its key (must be line + image or
   * line + '#' + image).<br>
   * These nodes are identified in JavaCC15.jjt with a node descriptor #identifier or #node_descriptor.
   * 
   * @param aKey - a key (must be line + image or line + '#' + image)
   * @return the JJNode
   */
  public JJNode getIdentOrNodeDesc(final String aKey) {
    return identOrNodeDescMap.get(aKey);
  }

  /**
   * Returns the hyperlink target for a key.
   * 
   * @param aKey - a key (must be image without "#")
   * @return the JJNode
   */
  public JJNode getHyperlinkTarget(final String aKey) {
    return hyperlinksMap.get(aKey);
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
   * Tells whether the given key appears in the identifier or node descriptor nodes map.
   * 
   * @param aKey - a key (must be line + image or line + '#' + image)
   * @return true / false
   */
  public final boolean isIdentOrNodeDescElement(final String aKey) {
    return identOrNodeDescMap.containsKey(aKey);
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
   * @return the non identifier nor node descriptor nodes map
   */
  public final HashMap<String, JJNode> getNonIdentNorNodeDescMap() {
    return nonIdentNorNodeDescMap;
  }

  /**
   * @return the identifier or node descriptor nodes map
   */
  public final HashMap<String, JJNode> getIdentOrNodeDescMap() {
    return identOrNodeDescMap;
  }

}
