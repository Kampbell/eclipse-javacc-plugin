package sf.eclipse.javacc.options;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import sf.eclipse.javacc.IConstants;
import sf.eclipse.javacc.JavaccPlugin;

/**
 * The Tab for JJDoc options
 * 
 * @author Remi Koutcherawy 2003 
 * GPL Licence www.gnu.org/licenses/gpl.txt */
public class JJDocOptions extends JJAbstractTab implements IConstants {
  // Memo beware JJDoc uses ":" instead of "="
  // TEXT (default false) 
  //     Setting TEXT to true causes JJDoc to generate a plain text.
  // ONE_TABLE (default true) 
  //     The default value of ONE_TABLE is used to generate a single HTML table.
  // OUTPUT_FILE
  //     The default behavior is to put the JJDoc output into
  //     a file with either .html or .txt added as a suffix to
  //     the input file's base name.
  // OUTPUT_DIRECTORY
  //     Added in javaCC 3.1

  /**
   * Initialize with JJDoc known options
   */
  public JJDocOptions(Composite parent, IResource res) {
    super(parent, res);
 
    // All options are saved in a single property
    qualifiedName = QN_JJDOC_OPTIONS;
    optionSet = new OptionSet();

    // int options
    // boolean options
    optionSet.add(new Option("TEXT", "false", Option.BOOLEAN));
    optionSet.add(new Option("ONE_TABLE", "true", Option.BOOLEAN));

    // string options    
    // path options
    optionSet.add(new Option("OUTPUT_DIRECTORY", "", Option.PATH));

    // file options
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
    pathField[0].setStringValue(JavaccPlugin.getResourceString("JJDocOptions.outputdir"));
  }  
  /**
   *  Unit test
   * NB configure VM arguments :
   * -Djava.library.path=C:\eclipse\plugins\org.eclipse.swt.win32_2.0.2\os\win32\x86
   */
  public static void main(String[] args) {
    Display display = new Display();
    Shell shell = new Shell(display);
    GridLayout layout = new GridLayout();
    layout.numColumns = 1;
    shell.setLayout(layout);
    
    JJDocOptions jjdoc = new JJDocOptions(shell, null);
    jjdoc.performDefaults();
    jjdoc.performOk();

    shell.pack();
    shell.open();
    while (!shell.isDisposed()){
      if(!display.readAndDispatch())
        display.sleep();
    }
  }
}