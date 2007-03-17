package sf.eclipse.javacc.options;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Composite;

import sf.eclipse.javacc.IJJConstants;

/**
 * The Tab for JJTree options
 * Enables setting of JJTree options for project or jjt file
 * 
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */
public class JJTreeOptions extends JJAbstractTab implements IJJConstants {
//    Memo
//    STATIC                 (default true)
//    MULTI                  (default false)
//    NODE_DEFAULT_VOID      (default false)
//    NODE_SCOPE_HOOK        (default false)
//    NODE_FACTORY           (default false)
//    NODE_USES_PARSER       (default false)
//    BUILD_NODE_FILES       (default true)
//    VISITOR                (default false)
//    NODE_PREFIX            (default "AST")
//    NODE_PACKAGE           (default "")
//    OUTPUT_FILE            (default remove input file suffix, add .jj)
//    OUTPUT_DIRECTORY       (default "")
//    VISITOR_EXCEPTION      (default "")
//  JDK_VERSION               (default "1.4")
//  NODE_EXTENDS              (default "")
//  TOKEN_MANAGER_USES_PARSER (default false)
// JJTREE_OUTPUT_DIRECTORY (default: use value of OUTPUT_DIRECTORY)

  /**
   * Initialize with JJTree known options
   */
  public JJTreeOptions(Composite parent, IResource res) {
    super(parent, res);

    // All options are saved in a single property
    qualifiedName = QN_JJTREE_OPTIONS;
    optionSet = new OptionSet();

    // int options
    // boolean options
    optionSet.add(new Option("STATIC", "true", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("MULTI", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("NODE_DEFAULT_VOID", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("NODE_SCOPE_HOOK", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("NODE_FACTORY", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("NODE_USES_PARSER", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("BUILD_NODE_FILES", "true", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("VISITOR", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("TOKEN_MANAGER_USES_PARSER", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    
    // string options
    optionSet.add(new Option("NODE_PREFIX", "AST", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("NODE_PACKAGE", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("VISITOR_EXCEPTION", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("JDK_VERSION", "1.4", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("NODE_EXTENDS", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$

    // path option
    optionSet.add(new Option("OUTPUT_DIRECTORY", "", Option.PATH)); //$NON-NLS-1$ //$NON-NLS-2$
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