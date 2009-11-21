package sf.eclipse.javacc.editors;

import java.util.HashMap;

import sf.eclipse.javacc.parser.JJNode;
import sf.eclipse.javacc.parser.JavaCCParserTreeConstants;

/**
 * Maps of JavaCC elements for one Editor. Used to navigate between declarations.
 * 
 * @author Remi Koutcherawy 2003-2009 - CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009
 */
public class JJElements implements JavaCCParserTreeConstants {

  // MMa 11/09 : javadoc and formatting and renaming revision

  /** the map of node images to nodes for non identifier nodes */
  private final HashMap<String, JJNode> nonIdentifiersMap = new HashMap<String, JJNode>();
  /** the map of node identifiers (images + line number) to nodes for identifier nodes */
  private final HashMap<String, JJNode> identifiersMap    = new HashMap<String, JJNode>();

  /**
   * Empty constructor, as it should be constructed for each Editor.
   */
  public JJElements() {
    // nothing done here
  }

  /**
   * Adds the node either to the identifier or non identifier maps.
   * 
   * @param aImage the node's image
   * @param aNode the node
   * @return the identifier or non identifier map
   */
  public final Object put(final String aImage, final JJNode aNode) {
    if (aNode.getId() == JJTIDENTIFIER) {
      // The line is added to the text to distinguish between multiples occurrences of the identifier
      // TODO this does not work for cases where 2 or more occurrences of the same production appear on the same line
      return identifiersMap.put(aImage + aNode.getBeginLine(), aNode);
    }
    return nonIdentifiersMap.put(aImage, aNode);
  }

  /**
   * Clears both maps.
   */
  public final void clear() {
    nonIdentifiersMap.clear();
    identifiersMap.clear();
  }

  /**
   * Returns the node from the non identifiers map given its image.<br>
   * These nodes are identified in JavaCC15.jjt with a node_descriptor '#xxx', except the #identifier nodes
   * which are not in this map, they are filtered and added in the other map.
   * 
   * @param aKey the node image
   * @return the JJNode
   */
  public final JJNode getNonIdentifierNode(final String aKey) {
    return nonIdentifiersMap.get(aKey);
  }

  /**
   * Returns the node from the identifiers map given its identifier (image + line).<br>
   * These nodes are identified in JavaCC15.jjt with a node_descriptor #identifier.
   * 
   * @param aKey the node identifier
   * @return the JJNode
   */
  public JJNode getIdentifierNode(final String aKey) {
    return identifiersMap.get(aKey);
  }

  /**
   * Tells whether the given key appears in the non identifiers map.
   * 
   * @param aKey a key
   * @return true / false
   */
  public final boolean isNonIdentifierElement(final String aKey) {
    return nonIdentifiersMap.containsKey(aKey);
  }

  /**
   * Tells whether the given key appears in the identifiers map.
   * 
   * @param aKey a key
   * @return true / false
   */
  public final boolean isIdentifierElement(final String aKey) {
    return identifiersMap.containsKey(aKey);
  }

  /**
   * @return the non identifiers map
   */
  public final HashMap<String, JJNode> getNonIdentifiersMap() {
    return nonIdentifiersMap;
  }

  /**
   * @return the identifiers map
   */
  public final HashMap<String, JJNode> getIdentifiersMap() {
    return identifiersMap;
  }
}
