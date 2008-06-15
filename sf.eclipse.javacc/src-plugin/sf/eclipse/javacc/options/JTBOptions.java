package sf.eclipse.javacc.options;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Composite;

import sf.eclipse.javacc.IJJConstants;

/**
 * The Tab for JJTB options
 * 
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */
public class JTBOptions extends JJAbstractTab implements IJJConstants {
  // Memo 
  // Usage: jtb [OPTIONS] [inputfile]
  // -h          Displays this help message.
  // -o NAME     Uses NAME as the filename for the annotated output grammar.
  // -np NAME    Uses NAME as the package for the syntax tree nodes.
  // -vp NAME    Uses NAME as the package for the default Visitor class.
  // -p NAME     "-p pkg" is short for "-np pkg.syntaxtree -vp pkg.visitor"
  // -si         Read from standard input rather than a file.
  // -w          Do not overwrite existing files.
  // -e          Suppress JTB semantic error checking.
  // -jd         Generate JavaDoc-friendly comments in the nodes and visitor.
  // -f          Use descriptive node class field names.
  // -ns NAME    Uses NAME as the class which all node classes will extend.
  // -pp         Generate parent pointers in all node classes.
  // -tk         Generate special tokens into the tree.
  // Toolkit options:
  // -scheme     Generate: (1) Scheme records representing the grammar.
  //                       (2) A Scheme tree building visitor.
  // -printer    Generate a syntax tree dumping visitor.

  /**
   * Initialize with JTB known options
   */
  public JTBOptions(Composite parent, IResource res) {
    super(parent, res);
 
    // All options are saved in a single property
    preferenceName = JTB_OPTIONS;
    optionSet = new OptionSet();

    // int options    
    // void options
    optionSet.add(new Option("h", "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("w", "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("e", "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("jd", "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("f", "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("pp", "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("tk", "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("scheme", "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("printer", "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$
    
    // string options
    optionSet.add(new Option("o", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("np", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("vp", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("p", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("ns", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$

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
    // No pathField yet for JTB
//    pathField[0].setStringValue(Activator.getString("JTBOptions.outputdir")); //$NON-NLS-1$
  }  
}