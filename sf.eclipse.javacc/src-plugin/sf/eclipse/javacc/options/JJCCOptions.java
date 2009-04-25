package sf.eclipse.javacc.options;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Composite;

import sf.eclipse.javacc.IJJConstants;

/**
 * The Tab for JavaCC options
 * 
 * @author Remi Koutcherawy 2003-2006 - CeCILL Licence http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009
 */
/*
 * ModMMa : cleanup, additions, reordering
 */
public class JJCCOptions extends JJAbstractTab implements IJJConstants {
  /**
   * Initialize with JavaCC known options
   * @param parent the parent
   * @param res the ressource
   */
  public JJCCOptions(Composite parent, IResource res) {
    super(parent, res);
    
    // All options are saved in a single property
    preferenceName = JAVACC_OPTIONS;
 
    optionSet = new OptionSet();

    // int options
    optionSet.add(new Option("LOOKAHEAD", "1", Option.INT)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("CHOICE_AMBIGUITY_CHECK", "2", Option.INT)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("OTHER_AMBIGUITY_CHECK", "1", Option.INT)); //$NON-NLS-1$ //$NON-NLS-2$
    
    // boolean options
    optionSet.add(new Option("BUILD_PARSER", "true", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("GENERATE_STRING_BUILDER", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("BUILD_TOKEN_MANAGER", "true", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("IGNORE_CASE", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("CACHE_TOKENS", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("JAVA_UNICODE_ESCAPE", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("COMMON_TOKEN_ACTION", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("KEEP_LINE_COLUMN", "true", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("DEBUG_LOOKAHEAD", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("SANITY_CHECK", "true", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("DEBUG_PARSER", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("STATIC", "true", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("DEBUG_TOKEN_MANAGER", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("SUPPORT_CLASS_VISIBILITY_PUBLIC", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("ERROR_REPORTING", "true", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("TOKEN_MANAGER_USES_PARSER", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("FORCE_LA_CHECK", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("UNICODE_INPUT", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("GENERATE_ANNOTATIONS", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("USER_CHAR_STREAM", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("GENERATE_CHAINED_EXCEPTION", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("USER_TOKEN_MANAGER", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("GENERATE_GENERICS", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    
    // string options
    optionSet.add(new Option("JDK_VERSION", "1.4", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("TOKEN_EXTENDS", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("TOKEN_FACTORY", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$

    // path option
    optionSet.add(new Option("OUTPUT_DIRECTORY", "", Option.PATH)); //$NON-NLS-1$ //$NON-NLS-2$
    

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
    // pathField[0].setStringValue(Activator.getString("JJCCOptions.outputdir")); //$NON-NLS-1$
  }
}