package sf.eclipse.javacc.editors;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import sf.eclipse.javacc.JavaccPlugin;
import sf.eclipse.javacc.parser.JavaCCParserTreeConstants;
import sf.eclipse.javacc.parser.SimpleNode;

/**
 * LabelProvider for JJOutline
 * 
 * @author Remi Koutcherawy 2003 
 * GPL Licence www.gnu.org/licenses/gpl.txt
 */
public class JJLabelProvider extends LabelProvider {
  private HashMap imgHashMap = new HashMap();
  ImageDescriptor desc_option;
  ImageDescriptor desc_parser;
  ImageDescriptor desc_token;
  ImageDescriptor desc_rule;
  ImageDescriptor desc_expr;

  /**
   * To Decorate the Outline View, simply Text and Image
   */
  public JJLabelProvider() {
    super();
    JavaccPlugin jpg = JavaccPlugin.getDefault();
    desc_option = jpg.getResourceImageDescriptor("jj_option.gif");
    desc_parser = jpg.getResourceImageDescriptor("jj_parser.gif");
    desc_token = jpg.getResourceImageDescriptor("jj_token.gif");
    desc_rule = jpg.getResourceImageDescriptor("jj_rule.gif");
    desc_expr = jpg.getResourceImageDescriptor("jj_expr.gif");
  }

  /**
   * @see ILabelProvider#getImage(Object)
   */
  public Image getImage(Object anElement) {
    ImageDescriptor desc = desc_expr;
    SimpleNode node = (SimpleNode)anElement;
    if (node.getId() == JavaCCParserTreeConstants.JJTOPTIONSS)
      desc = desc_option;
    else if (node.getId() == JavaCCParserTreeConstants.JJTPARSER_BEGIN)
      desc = desc_parser;
    else if (node.getId() == JavaCCParserTreeConstants.JJTTOKEN)
      desc = desc_token;
    else if (node.getId() == JavaCCParserTreeConstants.JJTRULE)
      desc = desc_rule;
    else if(node.getId() == JavaCCParserTreeConstants.JJTIDENTIFIER)
      desc = desc_expr;
    else {
      System.out.println("JJLabelProvider Id "+node.getId());
    }
    // obtain the cached image corresponding to the descriptor
    Image image = (Image) imgHashMap.get(desc);
    if (image == null) {
      image = desc.createImage();
      imgHashMap.put(desc, image);
    }
    return image;
  }

  /**
   * @see ILabelProvider#getText(Object)
   */
  public String getText(Object anElement) {
    return anElement.toString();
  }

  /**
   * @see IBaseLabelProvider#dispose()
   */
  public void dispose() {
    super.dispose();
    for (Iterator it = imgHashMap.values().iterator(); it.hasNext();) {
      ((Image) it.next()).dispose();
    }
    imgHashMap.clear();
  }
}
