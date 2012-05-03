package sf.eclipse.javacc.options;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Composite;

import sf.eclipse.javacc.base.IJJConstants;
import sf.eclipse.javacc.base.Option;
import sf.eclipse.javacc.base.OptionSet;
import sf.eclipse.javacc.head.Activator;

/**
 * The JTB options Tab that enables setting the JTB options.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2011
 */
public class JTBOptions extends OptionsAbstractTab implements IJJConstants {

  // MMa 04/2009 : added description field (for JTB cryptic options)
  // MMa 11/2009 : added -d and -nd and -vd options
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 02/2011 : added -va and -npfx and -nsfx options

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
  //  -npfx str   Use str as prefix for the syntax tree nodes.
  //  -nsfx str   Use str as suffix for the syntax tree nodes.
  //  -ns class   Use class as the class which all node classes will extend.
  //  -o file     Use NAME as the filename for the annotated output grammar.
  //  -p pkg      \"-p pkg\" is short for \"-np pkg.syntaxtree -vp pkg.visitor\".
  //  -pp         Generate parent pointers in all node classes.
  //  -printer    Generate a syntax tree dumping visitor.
  //  -si         Read from standard input rather than a file.
  //  -scheme     Generate: (1) Scheme records representing the grammar.
  //                        (2) A Scheme tree building visitor.
  //  -tk         Generate special tokens into the tree. "
  //  -va         Generate visitors with an argument of a vararg type.
  //  -vd dir     Use dir as the package for the default visitor classes.
  //  -vp pkg     Use pkg as the package for the default visitor classes.
  //  -w          Do not overwrite existing files.

  /**
   * Standard constructor : initializes with known JTB options.
   * 
   * @param aParent the parent
   * @param aRes the resource
   */
  public JTBOptions(final Composite aParent, final IResource aRes) {
    super(aParent, aRes);

    // all options are saved in a single property
    jPreferenceName = JTB_OPTIONS;

    jOptionSet = new OptionSet(false);

    // int options

    // boolean options, reordered on two columns
    jNbColBooleans = 2;
    jOptionSet.add(new Option("cl", Activator.getString("JTBOptions.PrClLst"), "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("dl", Activator.getString("JTBOptions.GenDpthLvl"), "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("e", Activator.getString("JTBOptions.SupSemErrChk"), "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("f", Activator.getString("JTBOptions.GenDescFldNm"), "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("ia", Activator.getString("JTBOptions.InlnAccStmt"), "true", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("jd", Activator.getString("JTBOptions.GenJdocCmt"), "true", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("pp", Activator.getString("JTBOptions.GenParPntrs"), "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("printer", Activator.getString("JTBOptions.GenTDTFVis"), "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("tk", Activator.getString("JTBOptions.GenSpecTk"), "true", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("scheme", Activator.getString("JTBOptions.GenScheme"), "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("va", Activator.getString("JTBOptions.GenVarargs"), "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("w", Activator.getString("JTBOptions.NoOvrwrt"), "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    // string options
    jOptionSet.add(new Option("o", Activator.getString("JTBOptions.GnrtdFlNm"), "jtb.out.jj", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("np", Activator.getString("JTBOptions.GnrtdSTPck"), "syntaxtree", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("vp", Activator.getString("JTBOptions.GnrtdVisPck"), "visitor", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("p", Activator.getString("JTBOptions.NPVP"), "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("nd", Activator.getString("JTBOptions.GnrtdSTDir"), "syntaxtree", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("vd", Activator.getString("JTBOptions.GnrtdVisDir"), "visitor", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("d", Activator.getString("JTBOptions.NDVD"), "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("ns", Activator.getString("JTBOptions.GnrtdSupCl"), "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("npfx", Activator.getString("JTBOptions.NodePrefix"), "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("nsfx", Activator.getString("JTBOptions.NodeSuffix"), "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    // set values to default values
    jOptionSet.resetToDefaultValues();

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