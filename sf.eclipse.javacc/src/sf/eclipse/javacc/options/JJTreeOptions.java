package sf.eclipse.javacc.options;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import sf.eclipse.javacc.IConstants;
import sf.eclipse.javacc.JavaccPlugin;

/**
 * The Tab for JJTree options
 * Enables setting of JJTree options for project or jjt file
 * 
 * @author Remi Koutcherawy 2003 
 * GPL Licence www.gnu.org/licenses/gpl.txt */
public class JJTreeOptions extends JJAbstractTab implements IConstants {
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
    optionSet.add(new Option("STATIC", "true", Option.BOOLEAN));
    optionSet.add(new Option("MULTI", "false", Option.BOOLEAN));
    optionSet.add(new Option("NODE_DEFAULT_VOID", "false", Option.BOOLEAN));
    optionSet.add(new Option("NODE_SCOPE_HOOK", "false", Option.BOOLEAN));
    optionSet.add(new Option("NODE_FACTORY", "false", Option.BOOLEAN));
    optionSet.add(new Option("NODE_USES_PARSER", "false", Option.BOOLEAN));
    optionSet.add(new Option("BUILD_NODE_FILES", "true", Option.BOOLEAN));
    optionSet.add(new Option("VISITOR", "false", Option.BOOLEAN));
    
    // string options
    optionSet.add(new Option("NODE_PREFIX", "AST", Option.STRING));
    optionSet.add(new Option("NODE_PACKAGE", "", Option.STRING));
    optionSet.add(new Option("VISITOR_EXCEPTION", "", Option.STRING));

    // path option
    optionSet.add(new Option("OUTPUT_DIRECTORY", "", Option.PATH));
    
    // file option
    optionSet.add(new Option("OUTPUT_FILE", "", Option.FILE));
    
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
    pathField[0].setStringValue(JavaccPlugin.getResourceString("JJTreeOptions.outputdir"));
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
    
    JJTreeOptions jjt = new JJTreeOptions(shell, null);
    jjt.performDefaults();
    jjt.performOk();

    shell.pack();
    shell.open();
    while (!shell.isDisposed()){
      if(!display.readAndDispatch())
        display.sleep();
    }
  }
}