package sf.eclipse.javacc.options;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Composite;

import sf.eclipse.javacc.IJJConstants;

/**
 * The Tab for JJTB options.
 * 
 * @author Remi Koutcherawy 2003-2006 - CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009
 */
// ModMMa : added description field (for JTB cryptic options)
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
   * Standard constructor : initializes with known JTB options.
   * 
   * @param parent the parent
   * @param res the ressource
   */
  public JTBOptions(Composite parent, IResource res) {
    super(parent, res);
    // All options are saved in a single property
    preferenceName = JTB_OPTIONS;
    optionSet = new OptionSet();
    // int options
    // void options, reordered on two columns
//    optionSet.add(new Option("h - displays this help", "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("pp", "generate parent pointers in all node classes", "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    optionSet.add(new Option("e", "suppress semantic error checking", "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    optionSet.add(new Option("tk", "stores special tokens in the parse tree", "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    optionSet.add(new Option("w", "do not overwrite existing files", "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    optionSet.add(new Option("jd", "generate javadoc comments in nodes and visitor", "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    optionSet.add(new Option("scheme", "generatee for the Scheme language", "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    optionSet.add(new Option("f", "generate descriptive node class field names", "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    optionSet.add(new Option("printer", "generate TreeDumper & TreeFormatter visitors ", "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    // string options
    optionSet.add(new Option("o", "generated file name", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    optionSet.add(new Option("np", "generated syntax tree classes path", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    optionSet.add(new Option("vp", "generated visitor classes path", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    optionSet.add(new Option("p", "np + vp", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    optionSet.add(new Option("ns", "generated nodes classes super class name", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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