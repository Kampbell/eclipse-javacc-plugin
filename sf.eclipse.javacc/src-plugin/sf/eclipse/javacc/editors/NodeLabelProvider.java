package sf.eclipse.javacc.editors;

import static sf.eclipse.javacc.parser.JavaCCParserTreeConstants.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import sf.eclipse.javacc.base.AbstractActivator;
import sf.eclipse.javacc.parser.JJNode;

/**
 * LabelProvider for Content Outline and Call Hierarchy.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 */
class NodeLabelProvider extends LabelProvider {

  // MMa 11/2009 : javadoc and formatting revision ; added javacode and token_mgr_decls entries
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 08/2011 : added identifier and node_descriptor entries
  // MMa 10/2012 : used static import  ; adapted to modifications in grammar nodes and new nodes
  // MMa 11/2012 : moved to new package
  // MMa 11/2014 : fixed image for JJTIDENT_IN_EXP_UNIT ; changed package

  /** The images HashMap */
  protected final Map<ImageDescriptor, Image> imgMap = new HashMap<ImageDescriptor, Image>(16);
  /** The option image descriptor */
  protected ImageDescriptor                   jDesc_option;
  /** The parser image descriptor */
  protected ImageDescriptor                   jDesc_parser;
  /** The token image descriptor */
  protected ImageDescriptor                   jDesc_token;
  /** The rule image descriptor */
  protected ImageDescriptor                   jDesc_rule;
  /** The expression image descriptor */
  protected ImageDescriptor                   jDesc_expr;
  /** The class image descriptor */
  protected ImageDescriptor                   jDesc_class;
  /** The method image descriptor */
  protected ImageDescriptor                   jDesc_method;
  /** The javacode image descriptor */
  protected ImageDescriptor                   jDesc_javacode;
  /** The token_mgr_decls image descriptor */
  protected ImageDescriptor                   jDesc_tmdecl;
  /** The identifier or pounded identifier image descriptor */
  protected ImageDescriptor                   jDesc_identifier;
  /** The node_descriptor image descriptor */
  protected ImageDescriptor                   jDesc_node_desc;

  /**
   * Constructor, which loads image descriptors.
   */
  public NodeLabelProvider() {
    super();
    jDesc_option = AbstractActivator.getImageDescriptor("jj_option.gif"); //$NON-NLS-1$
    jDesc_parser = AbstractActivator.getImageDescriptor("jj_parser.gif"); //$NON-NLS-1$
    jDesc_token = AbstractActivator.getImageDescriptor("jj_token.gif"); //$NON-NLS-1$
    jDesc_rule = AbstractActivator.getImageDescriptor("jj_rule.gif"); //$NON-NLS-1$
    jDesc_expr = AbstractActivator.getImageDescriptor("jj_expr.gif"); //$NON-NLS-1$
    jDesc_class = AbstractActivator.getImageDescriptor("jj_class.gif"); //$NON-NLS-1$
    jDesc_method = AbstractActivator.getImageDescriptor("jj_method.gif"); //$NON-NLS-1$
    jDesc_javacode = AbstractActivator.getImageDescriptor("jj_javacode.gif"); //$NON-NLS-1$
    jDesc_tmdecl = AbstractActivator.getImageDescriptor("jj_tmd.gif"); //$NON-NLS-1$
    jDesc_identifier = AbstractActivator.getImageDescriptor("jj_identifier.gif"); //$NON-NLS-1$
    jDesc_node_desc = AbstractActivator.getImageDescriptor("jj_node.gif"); //$NON-NLS-1$
  }

  /** {@inheritDoc} */
  @Override
  public Image getImage(final Object aElement) {
    ImageDescriptor desc = jDesc_expr;
    final JJNode node = (JJNode) aElement;
    final int id = node.getId();
    switch (id) {
      case JJTPARSER_BEGIN:
        desc = jDesc_parser;
        break;
      case JJTJAVACC_OPTIONS:
      case JJTOPTION_BINDING:
        desc = jDesc_option;
        break;
      case JJTJAVACODE_PROD:
        desc = jDesc_javacode;
        break;
      case JJTNODE_DESC_IN_METH:
      case JJTNODE_DESC_BNF_DECL:
      case JJTNODE_DESC_IN_EXP:
        desc = jDesc_node_desc;
        break;
      case JJTMETHODDECL:
      case JJTCONSTRDECL:
      case JJTJAVAIDENTINMETHODDECL:
        desc = jDesc_method;
        break;
      case JJTBNF_PROD:
      case JJTIDENT_IN_EXP_UNIT:
        desc = jDesc_rule;
        break;
      case JJTREGULAR_EXPR_PROD:
        desc = jDesc_token;
        break;
      case JJTREGEXPR_SPEC:
        desc = jDesc_expr;
        break;
      case JJTTOKEN_MANAGER_DECLS:
        desc = jDesc_tmdecl;
        break;
      case JJTCLAORINTDECL:
      case JJTENUMDECL:
      case JJTANNOTTYPEDECL:
        desc = jDesc_class;
        break;
      case JJTIDENT_IN_PARSER:
      case JJTIDENT_BNF_DECL:
      case JJTIDENT_REG_EXPR_LABEL:
      case JJTIDENT_REG_EXPR_PRIVATE_LABEL:
      case JJTIDENT_IN_REG_EXPR:
      case JJTIDENT_IN_COMP_REG_EXPR_UNIT:
        desc = jDesc_identifier;
        break;
      default:
        return null;
    }
    // obtain the cached image corresponding to the descriptor
    Image image = imgMap.get(desc);
    if (image == null) {
      image = desc.createImage();
      imgMap.put(desc, image);
    }
    return image;
  }

  /** {@inheritDoc} */
  @Override
  public String getText(final Object aElement) {
    if (aElement instanceof JJNode) {
      return ((JJNode) aElement).getDisplayName();
    }
    return aElement.toString();
  }

  /** {@inheritDoc} */
  @Override
  public void dispose() {
    super.dispose();
    for (final Iterator<Image> it = imgMap.values().iterator(); it.hasNext();) {
      it.next().dispose();
    }
    imgMap.clear();
  }
}
