package sf.eclipse.javacc.options;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import sf.eclipse.javacc.IConstants;
import sf.eclipse.javacc.JavaccPlugin;

/**
 * The Tab for JavaCC options
 * 
 * @author Remi Koutcherawy 2003 
 * GPL Licence www.gnu.org/licenses/gpl.txt
 */
public class JJCCOptions extends JJAbstractTab implements IConstants {
//    Memo
//    LOOKAHEAD              (default 1)
//    CHOICE_AMBIGUITY_CHECK (default 2)
//    OTHER_AMBIGUITY_CHECK  (default 1)
//    STATIC                 (default true)
//    DEBUG_PARSER           (default false)
//    DEBUG_LOOKAHEAD        (default false)
//    DEBUG_TOKEN_MANAGER    (default false)
//    OPTIMIZE_TOKEN_MANAGER (default true)
//    ERROR_REPORTING        (default true)
//    JAVA_UNICODE_ESCAPE    (default false)
//    UNICODE_INPUT          (default false)
//    IGNORE_CASE            (default false)
//    COMMON_TOKEN_ACTION    (default false)
//    USER_TOKEN_MANAGER     (default false)
//    USER_CHAR_STREAM       (default false)
//    BUILD_PARSER           (default true)
//    BUILD_TOKEN_MANAGER    (default true)
//    SANITY_CHECK           (default true)
//    FORCE_LA_CHECK         (default false)
//    CACHE_TOKENS           (default false)
//    KEEP_LINE_COLUMN       (default true)
//    OUTPUT_DIRECTORY       (default Current Directory)

  /**
   * Initialize with JavaCC known options
   */
  public JJCCOptions(Composite parent, IResource res) {
    super(parent, res);
    
    // All options are saved in a single property
    qualifiedName = QN_JAVACC_OPTIONS;
 
    optionSet = new OptionSet();

    // int options
    optionSet.add(new Option("LOOKAHEAD", "1", Option.INT));
    optionSet.add(new Option("CHOICE_AMBIGUITY_CHECK", "2", Option.INT));
    optionSet.add(new Option("OTHER_AMBIGUITY_CHECK", "1", Option.INT));
    
    // boolean options
    optionSet.add(new Option("STATIC", "true", Option.BOOLEAN));
    optionSet.add(new Option("DEBUG_PARSER", "false", Option.BOOLEAN));
    optionSet.add(new Option("DEBUG_LOOKAHEAD", "false", Option.BOOLEAN));
    optionSet.add(new Option("DEBUG_TOKEN_MANAGER", "false", Option.BOOLEAN));
    optionSet.add(new Option("OPTIMIZE_TOKEN_MANAGER", "true", Option.BOOLEAN));
    optionSet.add(new Option("ERROR_REPORTING", "true", Option.BOOLEAN));
    optionSet.add(new Option("JAVA_UNICODE_ESCAPE", "false", Option.BOOLEAN));
    optionSet.add(new Option("UNICODE_INPUT", "false", Option.BOOLEAN));
    optionSet.add(new Option("IGNORE_CASE", "false", Option.BOOLEAN));
    optionSet.add(new Option("COMMON_TOKEN_ACTION", "false", Option.BOOLEAN));
    optionSet.add(new Option("USER_TOKEN_MANAGER", "false", Option.BOOLEAN));
    optionSet.add(new Option("USER_CHAR_STREAM", "false", Option.BOOLEAN));
    optionSet.add(new Option("BUILD_PARSER", "true", Option.BOOLEAN));
    optionSet.add(new Option("BUILD_TOKEN_MANAGER", "true", Option.BOOLEAN));
    optionSet.add(new Option("SANITY_CHECK", "true", Option.BOOLEAN));
    optionSet.add(new Option("FORCE_LA_CHECK", "false", Option.BOOLEAN));
    optionSet.add(new Option("CACHE_TOKENS", "false", Option.BOOLEAN));
    optionSet.add(new Option("KEEP_LINE_COLUMN", "true", Option.BOOLEAN));

    // string options
    // path option
    optionSet.add(new Option("OUTPUT_DIRECTORY", "", Option.PATH));
    
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
    pathField[0].setStringValue(JavaccPlugin.getResourceString("JJCCOptions.outputdir"));
  }
  
  /**
   *  Unit test
   * NB configure VM arguments :
   * -Djava.library.path=C:\eclipse\plugins\org.eclipse.swt.win32_2.0.2\os\win32\x86
   */
  public static void main(String[] args) {
    Display display = new Display();
    Shell shell = new Shell(display);
    FillLayout fillLayout = new FillLayout ();
    shell.setLayout (fillLayout);

    JJCCOptions jjc = new JJCCOptions(shell, null);
    jjc.performDefaults();
    jjc.performOk();
    
    shell.pack();
    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
  }
}