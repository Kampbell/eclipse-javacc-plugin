package sf.eclipse.javacc.options;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Composite;

import sf.eclipse.javacc.IJJConstants;

/**
 * The JavaCC options Tab that enables setting the JavaCC options.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class JavaCCOptions extends JJAbstractTab implements IJJConstants {

  // MMa 02/2010 : formatting and javadoc revision
  // MMa 03/2010 : adapted boolean options sort to 3 columns display

  //	The integer valued options are:
  //	    LOOKAHEAD              (default 1)
  //	    CHOICE_AMBIGUITY_CHECK (default 2)
  //	    OTHER_AMBIGUITY_CHECK  (default 1)
  //	The boolean valued options are:
  //	    STATIC                 (default true)
  //	    SUPPORT_CLASS_VISIBILITY_PUBLIC (default true)
  //	    DEBUG_PARSER           (default false)
  //	    DEBUG_LOOKAHEAD        (default false)
  //	    DEBUG_TOKEN_MANAGER    (default false)
  //	    ERROR_REPORTING        (default true)
  //	    JAVA_UNICODE_ESCAPE    (default false)
  //	    UNICODE_INPUT          (default false)
  //	    IGNORE_CASE            (default false)
  //	    COMMON_TOKEN_ACTION    (default false)
  //	    USER_TOKEN_MANAGER     (default false)
  //	    USER_CHAR_STREAM       (default false)
  //	    BUILD_PARSER           (default true)
  //	    BUILD_TOKEN_MANAGER    (default true)
  //	    TOKEN_MANAGER_USES_PARSER (default false)
  //	    SANITY_CHECK           (default true)
  //	    FORCE_LA_CHECK         (default false)
  //	    CACHE_TOKENS           (default false)
  //	    KEEP_LINE_COLUMN       (default true)
  //	GENERATE_CHAINED_EXCEPTION (default false)
  //	GENERATE_GENERICS          (default false)
  //	The string valued options are:
  //	    OUTPUT_DIRECTORY       (default Current Directory)
  //	    TOKEN_EXTENDS          (Object)
  //	    TOKEN_FACTORY          (Object)
  //	JDK_VERSION            (1.5)
  //	GRAMMAR_ENCODING       (default file.encoding)

  /**
   * Initializes with JavaCC default options.
   * 
   * @param aParent the parent
   * @param aRes the resource
   */
  public JavaCCOptions(final Composite aParent, final IResource aRes) {
    super(aParent, aRes);

    // All options are saved in a single property
    fPreferenceName = JAVACC_OPTIONS;

    fOptionSet = new OptionSet(true);

    // int options
    fOptionSet.add(new Option("LOOKAHEAD", "1", Option.INT)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("CHOICE_AMBIGUITY_CHECK", "2", Option.INT)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("OTHER_AMBIGUITY_CHECK", "1", Option.INT)); //$NON-NLS-1$ //$NON-NLS-2$

    // boolean options
    fNbColBooleans = 3;
    fOptionSet.add(new Option("BUILD_PARSER", "true", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("FORCE_LA_CHECK", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("SANITY_CHECK", "true", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("BUILD_TOKEN_MANAGER", "true", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("GENERATE_ANNOTATIONS", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("STATIC", "true", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("CACHE_TOKENS", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("GENERATE_CHAINED_EXCEPTION", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("SUPPORT_CLASS_VISIBILITY_PUBLIC", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("COMMON_TOKEN_ACTION", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("GENERATE_GENERICS", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("TOKEN_MANAGER_USES_PARSER", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("DEBUG_LOOKAHEAD", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("GENERATE_STRING_BUILDER", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("UNICODE_INPUT", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("DEBUG_PARSER", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("IGNORE_CASE", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("USER_CHAR_STREAM", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("DEBUG_TOKEN_MANAGER", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("JAVA_UNICODE_ESCAPE", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("USER_TOKEN_MANAGER", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("ERROR_REPORTING", "true", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("KEEP_LINE_COLUMN", "true", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$

    // string options
    fOptionSet.add(new Option("JDK_VERSION", "1.5", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("TOKEN_EXTENDS", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("TOKEN_FACTORY", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("GRAMMAR_ENCODING", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$

    // path option
    fOptionSet.add(new Option("OUTPUT_DIRECTORY", "", Option.PATH)); //$NON-NLS-1$ //$NON-NLS-2$

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