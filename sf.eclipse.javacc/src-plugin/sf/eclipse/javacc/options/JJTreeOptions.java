package sf.eclipse.javacc.options;

import static sf.eclipse.javacc.base.IConstants.JJTREE_OPTIONS;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Composite;

import sf.eclipse.javacc.base.Option;
import sf.eclipse.javacc.base.OptionSet;

/**
 * The JJTree options Tab that enables setting the JJTree options.
 *
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 * @author Bill Fenlason 2012
 */
class JJTreeOptions extends OptionsAbstractTab {

  // MMa 04/2009 : cleanup and reordering in alphabetic order
  // MMa 02/2010 : formatting and javadoc revision
  // BF  06/2012 : removed redundant superinterface to prevent warning
  // MMa 11/2014 : added OUTPUT_LANGUAGE option

  /**
   * Initializes with JJTree default options.
   *
   * @param aParent - the parent
   * @param aRes - the resource
   */
  public JJTreeOptions(final Composite aParent, final IResource aRes) {
    super(aParent, aRes);

    // all options are saved in a single property
    jPreferenceName = JJTREE_OPTIONS;

    jOptionSet = new OptionSet(true);

    // int options & boolean options, in alphabetic order on two columns
    jNbColBooleans = 2;
    // TODO add NODE_STACK_SIZE, CHECK_DEFINITE_NODE, VISITOR_METHOD_NAME_INCLUDES_TYPE_NAME, NODE_INCLUDES
    jOptionSet.add(new Option("BUILD_NODE_FILES", "true", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    jOptionSet.add(new Option("MULTI", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    jOptionSet.add(new Option("NODE_DEFAULT_VOID", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    jOptionSet.add(new Option("NODE_SCOPE_HOOK", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    jOptionSet.add(new Option("NODE_USES_PARSER", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    jOptionSet.add(new Option("TRACK_TOKENS", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    jOptionSet.add(new Option("VISITOR", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$

    // string options, in alphabetic order
    jOptionSet.add(new Option("NODE_CLASS", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    jOptionSet.add(new Option("NODE_EXTENDS", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    jOptionSet.add(new Option("NODE_FACTORY", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    jOptionSet.add(new Option("NODE_PACKAGE", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    jOptionSet.add(new Option("NODE_PREFIX", "AST", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    jOptionSet.add(new Option("OUTPUT_LANGUAGE", "Java", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    jOptionSet.add(new Option("VISITOR_DATA_TYPE", "Object", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    jOptionSet.add(new Option("VISITOR_EXCEPTION", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    jOptionSet.add(new Option("VISITOR_RETURN_TYPE", "Object", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$

    // path option
    jOptionSet.add(new Option("JJTREE_OUTPUT_DIRECTORY", "", Option.PATH)); //$NON-NLS-1$ //$NON-NLS-2$

    // file option
    jOptionSet.add(new Option("OUTPUT_FILE", "", Option.FILE)); //$NON-NLS-1$ //$NON-NLS-2$

    // set values to default values
    jOptionSet.resetToDefaultValues();

    // super class fills the content from property and optionSet
    createContents();
  }

  /**
   * Sets defaults in Eclipse.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public void performDefaults() {
    super.performDefaults();
  }
}