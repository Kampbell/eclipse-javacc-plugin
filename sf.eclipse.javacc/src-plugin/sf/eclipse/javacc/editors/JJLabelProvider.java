package sf.eclipse.javacc.editors;


import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import sf.eclipse.javacc.head.Activator;
import sf.eclipse.javacc.parser.JJNode;
import sf.eclipse.javacc.parser.JavaCCParserTreeConstants;

/**
 * LabelProvider for JJOutline.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class JJLabelProvider extends LabelProvider {

  // MMa 11/2009 : javadoc and formatting revision ; added javacode and token_mgr_decls entries
  // MMa 02/2010 : formatting and javadoc revision

  /** The images HashMap */
  private final HashMap<ImageDescriptor, Image> imgHashMap = new HashMap<ImageDescriptor, Image>(16);
  /** The option image descriptor */
  ImageDescriptor                               fDesc_option;
  /** The parser image descriptor */
  ImageDescriptor                               fDesc_parser;
  /** The token image descriptor */
  ImageDescriptor                               fDesc_token;
  /** The rule image descriptor */
  ImageDescriptor                               fDesc_rule;
  /** The expression image descriptor */
  ImageDescriptor                               fDesc_expr;
  /** The class image descriptor */
  ImageDescriptor                               fDesc_class;
  /** The method image descriptor */
  ImageDescriptor                               fDesc_method;
  /** The javacode image descriptor */
  ImageDescriptor                               fDesc_javacode;
  /** The token_mgr_decls image descriptor */
  ImageDescriptor                               fDesc_tmdecl;

  /**
   * To Decorate the Outline View, simply Text and Image
   */
  public JJLabelProvider() {
    super();
    fDesc_option = Activator.getImageDescriptor("jj_option.gif"); //$NON-NLS-1$
    fDesc_parser = Activator.getImageDescriptor("jj_parser.gif"); //$NON-NLS-1$
    fDesc_token = Activator.getImageDescriptor("jj_token.gif"); //$NON-NLS-1$
    fDesc_rule = Activator.getImageDescriptor("jj_rule.gif"); //$NON-NLS-1$
    fDesc_expr = Activator.getImageDescriptor("jj_expr.gif"); //$NON-NLS-1$
    fDesc_class = Activator.getImageDescriptor("jj_class.gif"); //$NON-NLS-1$
    fDesc_method = Activator.getImageDescriptor("jj_method.gif"); //$NON-NLS-1$
    fDesc_javacode = Activator.getImageDescriptor("jj_javacode.gif"); //$NON-NLS-1$
    fDesc_tmdecl = Activator.getImageDescriptor("jj_tmd.gif"); //$NON-NLS-1$
  }

  /**
   * @see ILabelProvider#getImage(Object)
   */
  @Override
  public Image getImage(final Object anElement) {
    ImageDescriptor desc = fDesc_expr;
    final JJNode node = (JJNode) anElement;
    if (node.getId() == JavaCCParserTreeConstants.JJTJAVACC_OPTIONS) {
      desc = fDesc_option;
    }
    else if (node.getId() == JavaCCParserTreeConstants.JJTOPTION_BINDING) {
      desc = fDesc_option;
    }
    else if (node.getId() == JavaCCParserTreeConstants.JJTPARSER_BEGIN) {
      desc = fDesc_parser;
    }
    else if (node.getId() == JavaCCParserTreeConstants.JJTREGULAR_EXPR_PRODUCTION) {
      desc = fDesc_token;
    }
    else if (node.getId() == JavaCCParserTreeConstants.JJTBNF_PRODUCTION) {
      desc = fDesc_rule;
    }
    else if (node.getId() == JavaCCParserTreeConstants.JJTREGEXPR_SPEC) {
      desc = fDesc_expr;
    }
    else if (node.getId() == JavaCCParserTreeConstants.JJTCLASSORINTERFACEDECLARATION) {
      desc = fDesc_class;
    }
    else if (node.getId() == JavaCCParserTreeConstants.JJTMETHODDECLARATION) {
      desc = fDesc_method;
    }
    else if (node.getId() == JavaCCParserTreeConstants.JJTTOKEN_MANAGER_DECLS) {
      desc = fDesc_tmdecl;
    }
    else if (node.getId() == JavaCCParserTreeConstants.JJTJAVACODE_PRODUCTION) {
      desc = fDesc_javacode;
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
  public String getText(final Object aElement) {
    if (aElement instanceof JJNode) {
      return ((JJNode) aElement).getLabeledName();
    }
    return aElement.toString();
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
