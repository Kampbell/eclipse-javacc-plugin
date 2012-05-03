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
 * @author Marc Mazas 2009-2010-2011
 */
public class JJLabelProvider extends LabelProvider {

  // MMa 11/2009 : javadoc and formatting revision ; added javacode and token_mgr_decls entries
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 08/2011 : added identifier and node_descriptor entries

  /** The images HashMap */
  private final HashMap<ImageDescriptor, Image> imgHashMap = new HashMap<ImageDescriptor, Image>(16);
  /** The option image descriptor */
  ImageDescriptor                               jDesc_option;
  /** The parser image descriptor */
  ImageDescriptor                               jDesc_parser;
  /** The token image descriptor */
  ImageDescriptor                               jDesc_token;
  /** The rule image descriptor */
  ImageDescriptor                               jDesc_rule;
  /** The expression image descriptor */
  ImageDescriptor                               jDesc_expr;
  /** The class image descriptor */
  ImageDescriptor                               jDesc_class;
  /** The method image descriptor */
  ImageDescriptor                               jDesc_method;
  /** The javacode image descriptor */
  ImageDescriptor                               jDesc_javacode;
  /** The token_mgr_decls image descriptor */
  ImageDescriptor                               jDesc_tmdecl;
  /** The identifier or pounded identifier image descriptor */
  ImageDescriptor                               jDesc_identifier;
  /** The node_descriptor image descriptor */
  ImageDescriptor                               jDesc_node_desc;

  /**
   * To Decorate the Outline View, simply Text and Image
   */
  public JJLabelProvider() {
    super();
    jDesc_option = Activator.getImageDescriptor("jj_option.gif"); //$NON-NLS-1$
    jDesc_parser = Activator.getImageDescriptor("jj_parser.gif"); //$NON-NLS-1$
    jDesc_token = Activator.getImageDescriptor("jj_token.gif"); //$NON-NLS-1$
    jDesc_rule = Activator.getImageDescriptor("jj_rule.gif"); //$NON-NLS-1$
    jDesc_expr = Activator.getImageDescriptor("jj_expr.gif"); //$NON-NLS-1$
    jDesc_class = Activator.getImageDescriptor("jj_class.gif"); //$NON-NLS-1$
    jDesc_method = Activator.getImageDescriptor("jj_method.gif"); //$NON-NLS-1$
    jDesc_javacode = Activator.getImageDescriptor("jj_javacode.gif"); //$NON-NLS-1$
    jDesc_tmdecl = Activator.getImageDescriptor("jj_tmd.gif"); //$NON-NLS-1$
    jDesc_identifier = Activator.getImageDescriptor("jj_identifier.gif"); //$NON-NLS-1$
    jDesc_node_desc = Activator.getImageDescriptor("jj_node.gif"); //$NON-NLS-1$
  }

  /**
   * @see ILabelProvider#getImage(Object)
   */
  @Override
  public Image getImage(final Object aElement) {
    ImageDescriptor desc = jDesc_expr;
    final JJNode node = (JJNode) aElement;
    final int id = node.getId();
    if (id == JavaCCParserTreeConstants.JJTJAVACC_OPTIONS
        || id == JavaCCParserTreeConstants.JJTOPTION_BINDING) {
      desc = jDesc_option;
    }
    else if (id == JavaCCParserTreeConstants.JJTPARSER_BEGIN) {
      desc = jDesc_parser;
    }
    else if (id == JavaCCParserTreeConstants.JJTREGULAR_EXPR_PRODUCTION) {
      desc = jDesc_token;
    }
    else if (id == JavaCCParserTreeConstants.JJTBNF_PRODUCTION) {
      desc = jDesc_rule;
    }
    else if (id == JavaCCParserTreeConstants.JJTREGEXPR_SPEC) {
      desc = jDesc_expr;
    }
    else if (id == JavaCCParserTreeConstants.JJTCLASSORINTERFACEDECLARATION) {
      desc = jDesc_class;
    }
    else if (id == JavaCCParserTreeConstants.JJTMETHODDECLARATION) {
      desc = jDesc_method;
    }
    else if (id == JavaCCParserTreeConstants.JJTTOKEN_MANAGER_DECLS) {
      desc = jDesc_tmdecl;
    }
    else if (id == JavaCCParserTreeConstants.JJTJAVACODE_PRODUCTION) {
      desc = jDesc_javacode;
    }
    else if (id == JavaCCParserTreeConstants.JJTIDENT_BNF_DECL
             || id == JavaCCParserTreeConstants.JJTIDENT_REG_EXPR_LABEL
             || id == JavaCCParserTreeConstants.JJTIDENT_REG_EXPR_PRIVATE_LABEL
             || id == JavaCCParserTreeConstants.JJTIDENT_USE) {
      desc = jDesc_identifier;
    }
    else if (id == JavaCCParserTreeConstants.JJTNODE_DESC_BNF_DECL
             || id == JavaCCParserTreeConstants.JJTNODE_DESC_IN_EXP
             || id == JavaCCParserTreeConstants.JJTNODE_DESC_IN_METH) {
      desc = jDesc_node_desc;
    }
    else {
      //      System.out.println("JJLabelProvider Id "+id);
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
   * @see sf.eclipse.javacc.parser.JJNode#getDisplayName()
   * @see sf.eclipse.javacc.parser.JJNode#addCaller(JJNode, boolean)
   */
  @Override
  public String getText(final Object aElement) {
    if (aElement instanceof JJNode) {
      return ((JJNode) aElement).getDisplayName();
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
