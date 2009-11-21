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
 * LabelProvider for JJOutline.
 * 
 * @author Remi Koutcherawy 2003-2006 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009
 */
public class JJLabelProvider extends LabelProvider {

  /*
   * MMa 11/09 : javadoc and formatting revision ; added javacode and token_mgr_decls entries
   */
  /** the images hashmap */
  private final HashMap<ImageDescriptor, Image> imgHashMap = new HashMap<ImageDescriptor, Image>(16);
  /** the option image descriptor */
  ImageDescriptor                               desc_option;
  /** the parser image descriptor */
  ImageDescriptor                               desc_parser;
  /** the token image descriptor */
  ImageDescriptor                               desc_token;
  /** the rule image descriptor */
  ImageDescriptor                               desc_rule;
  /** the expression image descriptor */
  ImageDescriptor                               desc_expr;
  /** the class image descriptor */
  ImageDescriptor                               desc_class;
  /** the method image descriptor */
  ImageDescriptor                               desc_method;
  /** the javacode image descriptor */
  ImageDescriptor                               desc_javacode;
  /** the token_mgr_decls image descriptor */
  ImageDescriptor                               desc_tmdecl;

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
    desc_javacode = Activator.getImageDescriptor("jj_javacode.gif"); //$NON-NLS-1$
    desc_tmdecl = Activator.getImageDescriptor("jj_tmd.gif"); //$NON-NLS-1$
  }

  /**
   * @see ILabelProvider#getImage(Object)
   */
  @Override
  public Image getImage(final Object anElement) {
    ImageDescriptor desc = desc_expr;
    final JJNode node = (JJNode) anElement;
    if (node.getId() == JavaCCParserTreeConstants.JJTJAVACC_OPTIONS) {
      desc = desc_option;
    }
    else if (node.getId() == JavaCCParserTreeConstants.JJTOPTION_BINDING) {
      desc = desc_option;
    }
    else if (node.getId() == JavaCCParserTreeConstants.JJTPARSER_BEGIN) {
      desc = desc_parser;
    }
    else if (node.getId() == JavaCCParserTreeConstants.JJTREGULAR_EXPR_PRODUCTION) {
      desc = desc_token;
    }
    else if (node.getId() == JavaCCParserTreeConstants.JJTBNF_PRODUCTION) {
      desc = desc_rule;
    }
    else if (node.getId() == JavaCCParserTreeConstants.JJTREGEXPR_SPEC) {
      desc = desc_expr;
    }
    else if (node.getId() == JavaCCParserTreeConstants.JJTCLASSORINTERFACEDECLARATION) {
      desc = desc_class;
    }
    else if (node.getId() == JavaCCParserTreeConstants.JJTMETHODDECLARATION) {
      desc = desc_method;
    }
    else if (node.getId() == JavaCCParserTreeConstants.JJTTOKEN_MANAGER_DECLS) {
      desc = desc_tmdecl;
    }
    else if (node.getId() == JavaCCParserTreeConstants.JJTJAVACODE_PRODUCTION) {
      desc = desc_javacode;
    }
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
   * @see sf.eclipse.javacc.parser.JJNode#getLabeledName()
   * @see sf.eclipse.javacc.parser.JJNode#addCaller(JJNode)
   */
  @Override
  public String getText(final Object anElement) {
    if (anElement instanceof JJNode) {
      return ((JJNode) anElement).getLabeledName();
    }
    return anElement.toString();
  }

  /**
   * @see IBaseLabelProvider#dispose()
   */
  @Override
  public void dispose() {
    super.dispose();
    for (final Iterator<Image> it = imgHashMap.values().iterator(); it.hasNext();) {
      it.next().dispose();
    }
    imgHashMap.clear();
  }
}
