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
public class JTBOptions extends JJAbstractTab implements IJJConstants {

  //MMa 04/09 : added description field (for JTB cryptic options)
  //MMa 11/09 : added -d -nd -vd options

  // Memo 
  // Usage: jtb [OPTIONS] [inputfile]
  //  -cl         Print a list of the classes generated to standard out.
  //  -d dir     \"-d dir\" is short for \"-nd dir/syntaxtree -vd dir/visitor\".
  //  -dl         Generate depth level info.
  //  -e          Suppress JTB semantic error checking.
  //  -f          Use descriptive node class field names.
  //  -h          Display this help message and quit.
  //  -ia         Inline visitors accept methods on base classes.
  //  -jd         Generate JavaDoc-friendly comments in the nodes and visitor.
  //  -nd dir     Use dir as the package for the syntax tree nodes.
  //  -np pkg     Use pkg as the package for the syntax tree nodes.
  //  -ns class   Use class as the class which all node classes will extend.
  //  -o file     Use NAME as the filename for the annotated output grammar.
  //  -p pkg      \"-p pkg\" is short for \"-np pkg.syntaxtree -vp pkg.visitor\".
  //  -pp         Generate parent pointers in all node classes.
  //  -printer    Generate a syntax tree dumping visitor.
  //  -si         Read from standard input rather than a file.
  //  -scheme     Generate: (1) Scheme records representing the grammar.
  //                        (2) A Scheme tree building visitor.
  //  -tk         Generate special tokens into the tree. "
  //  -vd dir     Use dir as the package for the default visitor classes.
  //  -vp pkg     Use pkg as the package for the default visitor classes.
  //  -w          Do not overwrite existing files.

  /**
   * Standard constructor : initializes with known JTB options.
   * 
   * @param parent the parent
   * @param res the ressource
   */
  public JTBOptions(final Composite parent, final IResource res) {
    super(parent, res);
    // All options are saved in a single property
    preferenceName = JTB_OPTIONS;
    optionSet = new OptionSet();
    // int options
    // void options, reordered on two columns
    optionSet.add(new Option("cl", "print classes list", "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    optionSet.add(new Option("dl", "generate depth level", "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    optionSet.add(new Option("e", "suppress semantic error checking", "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    optionSet.add(new Option("f", "generate descriptive node classes field names", "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    optionSet.add(new Option("ia", "inline accept statements", "true", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    optionSet.add(new Option("jd", "generate javadoc comments in nodes and visitor", "true", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    optionSet.add(new Option("pp", "generate parent pointers in all node classes", "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    optionSet
             .add(new Option("printer", "generate TreeDumper & TreeFormatter visitors ", "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    optionSet.add(new Option("tk", "generate special tokens in the parse tree", "true", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    optionSet.add(new Option("scheme", "generate for the Scheme language", "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    optionSet.add(new Option("w", "do not overwrite existing files", "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    // string options
    optionSet.add(new Option("o", "generated file name", "jtb.out.jj", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    optionSet
             .add(new Option("np", "generated syntax tree classes package name", "syntaxtree", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    optionSet.add(new Option("vp", "generated visitor classes package name", "visitor", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    optionSet.add(new Option("p", "np = p + \".syntaxtree\", vp = p + \".visitor\"", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    optionSet
             .add(new Option(
                             "nd", "generated syntax tree classes directory name", "syntaxtree", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    optionSet.add(new Option("vd", "generated visitor classes directory name", "visitor", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    optionSet.add(new Option("d", "nd = d + \"/syntaxtree\", vd = d + \"/visitor\"", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    optionSet.add(new Option("ns", "generated nodes classes super class name", "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    // Fix values to default values
    optionSet.resetToDefaultValues();
    // Super class fills the content from property and optionSet
    createContents();
  }

  /**
   * Set defaults in Eclipse
   */
  @Override
  public void performDefaults() {
    super.performDefaults();
    // No pathField yet for JTB
    //    pathField[0].setStringValue(Activator.getString("JTBOptions.outputdir")); //$NON-NLS-1$
  }
}