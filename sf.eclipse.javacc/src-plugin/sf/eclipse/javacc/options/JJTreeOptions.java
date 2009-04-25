package sf.eclipse.javacc.options;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Composite;

import sf.eclipse.javacc.IJJConstants;

/**
 * The JJTree options Tab that enables setting the JJTree options for project or jjt file
 * 
 * @author Remi Koutcherawy 2003-2006 - CeCILL Licence http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009
 */
/*
 * ModMMa : cleanup and reordering in alphabetic order
 */
public class JJTreeOptions extends JJAbstractTab implements IJJConstants {
  /**
   * Initialize with JJTree known options
   * 
   * @param parent the parent
   * @param res the ressource
   */
  public JJTreeOptions(Composite parent, IResource res) {
    super(parent, res);
    // All options are saved in a single property
    preferenceName = JJTREE_OPTIONS;
    optionSet = new OptionSet();
    // int options & boolean options, in alphabetic order on two columns
    optionSet.add(new Option("BUILD_NODE_FILES", "true", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("NODE_USES_PARSER", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("MULTI", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("TOKEN_MANAGER_USES_PARSER", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("NODE_DEFAULT_VOID", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("TRACK_TOKENS", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("NODE_SCOPE_HOOK", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("VISITOR", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    // string options, in alphabetic order
    optionSet.add(new Option("JDK_VERSION", "1.4", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("NODE_CLASS", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("NODE_PACKAGE", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("NODE_EXTENDS", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("NODE_FACTORY", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("NODE_PREFIX", "AST", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("VISITOR_DATA_TYPE", "Object", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("VISITOR_EXCEPTION", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("VISITOR_RETURN_TYPE", "Object", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    // path option
    optionSet.add(new Option("JJTREE_OUTPUT_DIRECTORY", "", Option.PATH)); //$NON-NLS-1$ //$NON-NLS-2$
    // file option
    optionSet.add(new Option("OUTPUT_FILE", "", Option.FILE)); //$NON-NLS-1$ //$NON-NLS-2$
    // Fix values to default values
    optionSet.resetToDefaultValues();
    // Super class fills the content from property and optionSet
    createContents();
  }

  /**
   * Set defaults in Eclipse
   */
  public void performDefaults() {
    super.performDefaults();
    // For Eclipse
    // pathField[0].setStringValue(Activator.getString("JJTreeOptions.outputdir")); //$NON-NLS-1$
  }
}