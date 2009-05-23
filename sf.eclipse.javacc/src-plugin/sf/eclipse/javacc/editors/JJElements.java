package sf.eclipse.javacc.editors;

import java.util.HashMap;

import sf.eclipse.javacc.parser.JJNode;
import sf.eclipse.javacc.parser.JavaCCParserTreeConstants;

/**
 * A map of JavaCC elements for one Editor
 * Used to navigate between declarations.
 * 
 * @author Remi Koutcherawy 2003-2009
 * CeCILL license http://www.cecill.info/index.en.html
 */
public class JJElements implements JavaCCParserTreeConstants {
  private static final long serialVersionUID = 1L;
  private final HashMap<String, JJNode> map = new HashMap<String, JJNode>();
  private final HashMap<String, JJNode> mapIdentifier = new HashMap<String, JJNode>();
  
  /**
   * The JJElements should be constructed for one Editor.
   */
  public JJElements() {}
  
  public final Object put(String image, JJNode node) {
    if (node.getId() == JJTIDENTIFIER) {
// The line is added to the text to distinguish between multiples occurrences of the identifier
      return mapIdentifier.put(image+node.getBeginLine(), node);
    }
    else
      return map.put(image, node);
  }
  public final void clear() {
    map.clear();
    mapIdentifier.clear();
  }
  public final boolean isElement(String key) {
    return map.containsKey(key);
  }
  public final HashMap<String, JJNode> getMap() {
    return map;
  }
  /** 
   * The node returned is the declaration node.
   * The declaration nodes are identified in JavaCC15.jjt with a node_descriptor # :
   * #parser_begin #javacc_options #option_binding #javacode_production 
   * #bnf_production #regular_expr_production #regexpr_spec
   * #ClassOrInterfaceDeclaration #MethodDeclaration
   * Identifiers #identifier are not in this map, they are filtered and added as identifiers
   * */
  public final JJNode getNode(String key) {
    return map.get(key);
  }

  public JJNode getIdentifierNode(String key) {
    return mapIdentifier.get(key);
  }
}
