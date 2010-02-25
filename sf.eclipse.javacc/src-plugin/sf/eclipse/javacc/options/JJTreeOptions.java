package sf.eclipse.javacc.options;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Composite;

import sf.eclipse.javacc.IJJConstants;

/**
 * The JJTree options Tab that enables setting the JJTree options.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class JJTreeOptions extends JJAbstractProjectTab implements IJJConstants {

  // MMa 04/2009 : cleanup and reordering in alphabetic order
  // MMa 02/2010 : formatting and javadoc revision

  /**
   * Initializes with JJTree default options.
   * 
   * @param aParent the parent
   * @param aRes the resource
   */
  public JJTreeOptions(final Composite aParent, final IResource aRes) {
    super(aParent, aRes);

    // all options are saved in a single property
    fPreferenceName = JJTREE_OPTIONS;

    fOptionSet = new OptionSet(true);

    // int options & boolean options, in alphabetic order on two columns
    fOptionSet.add(new Option("BUILD_NODE_FILES", "true", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("NODE_USES_PARSER", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("MULTI", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("NODE_DEFAULT_VOID", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("TRACK_TOKENS", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("NODE_SCOPE_HOOK", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("VISITOR", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$

    // string options, in alphabetic order
    fOptionSet.add(new Option("NODE_CLASS", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("NODE_PACKAGE", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("NODE_EXTENDS", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("NODE_FACTORY", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("NODE_PREFIX", "AST", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("VISITOR_DATA_TYPE", "Object", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("VISITOR_EXCEPTION", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("VISITOR_RETURN_TYPE", "Object", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$

    // path option
    fOptionSet.add(new Option("JJTREE_OUTPUT_DIRECTORY", "", Option.PATH)); //$NON-NLS-1$ //$NON-NLS-2$

    // file option
    fOptionSet.add(new Option("OUTPUT_FILE", "", Option.FILE)); //$NON-NLS-1$ //$NON-NLS-2$

    // set values to default values
    fOptionSet.resetToDefaultValues();

    // super class fills the content from property and optionSet
    createContents();
  }

  /**
   * Sets defaults in Eclipse.
   */
  @Override
  public void performDefaults() {
    super.performDefaults();
  }
}