package sf.eclipse.javacc.editors;

import java.util.HashMap;

import sf.eclipse.javacc.parser.JJNode;

/**
 * A map of JavaCC elements.
 * Used to navigate between declarations.
 * 
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */
public class JJElements {
  private static final long serialVersionUID = 1L;
  private static final HashMap<String, JJNode> map = new HashMap<String, JJNode>();

  public static final Object put(String arg0, JJNode arg1) {
    return map.put(arg0, arg1);
  }

  public static final boolean isElement(String key) {
    return map.containsKey(key);
  }

  public static final HashMap<String, JJNode> getMap() {
    return map;
  }

  public static JJNode getNode(String key) {
    return map.get(key);
  }
}
