package sf.eclipse.javacc.editors;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import sf.eclipse.javacc.Activator;
import sf.eclipse.javacc.parser.JJNode;
import sf.eclipse.javacc.parser.JavaCCParserTreeConstants;

/**
 * LabelProvider for JJOutline
 * 
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */
public class JJLabelProvider extends LabelProvider {
  private HashMap<ImageDescriptor, Image> imgHashMap = new HashMap<ImageDescriptor, Image>();
  ImageDescriptor desc_option;
  ImageDescriptor desc_parser;
  ImageDescriptor desc_token;
  ImageDescriptor desc_rule;
  ImageDescriptor desc_expr;
  ImageDescriptor desc_class;
  ImageDescriptor desc_method;

  /**
   * To Decorate the Outline View, simply Text and Image
   */
  public JJLabelProvider() {
    super();
    desc_option = Activator.getImageDescriptor("jj_option.gif"); //$NON-NLS-1$
    desc_parser = Activator.getImageDescriptor("jj_parser.gif"); //$NON-NLS-1$
    desc_token = Activator.getImageDescriptor("jj_token.gif"); //$NON-NLS-1$
    desc_rule = Activator.getImageDescriptor("jj_rule.gif"); //$NON-NLS-1$
    desc_expr = Activator.getImageDescriptor("jj_expr.gif"); //$NON-NLS-1$
    desc_class = Activator.getImageDescriptor("jj_class.gif"); //$NON-NLS-1$
    desc_method = Activator.getImageDescriptor("jj_method.gif"); //$NON-NLS-1$
  }

  /**
   * @see ILabelProvider#getImage(Object)
   */
  public Image getImage(Object anElement) {
    ImageDescriptor desc = desc_expr;
    JJNode node = (JJNode)anElement;
    if (node.getId() == JavaCCParserTreeConstants.JJTJAVACC_OPTIONS)
      desc = desc_option;
    else if (node.getId() == JavaCCParserTreeConstants.JJTOPTION_BINDING)
      desc = desc_option;    
    else if (node.getId() == JavaCCParserTreeConstants.JJTPARSER_BEGIN)
      desc = desc_parser;
    else if (node.getId() == JavaCCParserTreeConstants.JJTREGULAR_EXPR_PRODUCTION)
      desc = desc_token;
    else if (node.getId() == JavaCCParserTreeConstants.JJTBNF_PRODUCTION)
      desc = desc_rule;
    else if(node.getId() == JavaCCParserTreeConstants.JJTREGEXPR_SPEC)
      desc = desc_expr;
    else if(node.getId() == JavaCCParserTreeConstants.JJTCLASSORINTERFACEDECLARATION)
      desc = desc_class;
    else if(node.getId() == JavaCCParserTreeConstants.JJTMETHODDECLARATION)
      desc = desc_method;
    else {
//      System.out.println("JJLabelProvider Id "+node.getId());
      return null;
    }
    // obtain the cached image corresponding to the descriptor
    Image image = imgHashMap.get(desc);
    if (image == null) {
      image = desc.createImage();
      imgHashMap.put(desc, image);
    }
    return image;
  }

  /**
   * @see ILabelProvider#getText(Object)
   * See also sf.eclipse.javacc.parser.JJNode#toString() !
   */
  public String getText(Object anElement) {
    return anElement.toString();
  }

  /**
   * @see IBaseLabelProvider#dispose()
   */
  public void dispose() {
    super.dispose();
    for (Iterator<Image> it = imgHashMap.values().iterator(); it.hasNext();) {
      it.next().dispose();
    }
    imgHashMap.clear();
  }
}
