package sf.eclipse.javacc.options;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Composite;

import sf.eclipse.javacc.base.Option;
import sf.eclipse.javacc.base.OptionSet;
import sf.eclipse.javacc.head.Activator;

/**
 * The JTB options Tab that enables setting the JTB options.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012
 * @author Bill Fenlason 2012
 */
public class JTBOptions extends OptionsAbstractTab {

  // MMa 04/2009 : added description field (for JTB cryptic options)
  // MMa 11/2009 : added -d and -nd and -vd options
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 02/2011 : added -va and -npfx and -nsfx options
  // BF  06/2012 : removed redundant super interface to prevent warning

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
   * @param aParent - the parent
   * @param aRes - the resource
   */
  public JTBOptions(final Composite aParent, final IResource aRes) {
    super(aParent, aRes);

    // all options are saved in a single property
    jPreferenceName = JTB_OPTIONS;

    jOptionSet = new OptionSet(false);

    // int options

    // boolean options, reordered on two columns
    jNbColBooleans = 2;
    jOptionSet.add(new Option("cl", Activator.getString("JTBOpt.PrClLst"), "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("dl", Activator.getString("JTBOpt.GenDpthLvl"), "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("e", Activator.getString("JTBOpt.SupSemErrChk"), "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("f", Activator.getString("JTBOpt.GenDescFldNm"), "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("ia", Activator.getString("JTBOpt.InlnAccStmt"), "true", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("jd", Activator.getString("JTBOpt.GenJdocCmt"), "true", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("pp", Activator.getString("JTBOpt.GenParPntrs"), "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("printer", Activator.getString("JTBOpt.GenTDTFVis"), "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("tk", Activator.getString("JTBOpt.GenSpecTk"), "true", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("scheme", Activator.getString("JTBOpt.GenScheme"), "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("va", Activator.getString("JTBOpt.GenVarargs"), "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("w", Activator.getString("JTBOpt.NoOvrwrt"), "false", Option.VOID)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    // string options
    jOptionSet.add(new Option("o", Activator.getString("JTBOpt.GnrtdFlNm"), "jtb.out.jj", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("np", Activator.getString("JTBOpt.GnrtdSTPck"), "syntaxtree", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("vp", Activator.getString("JTBOpt.GnrtdVisPck"), "visitor", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("p", Activator.getString("JTBOpt.NPVP"), "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("nd", Activator.getString("JTBOpt.GnrtdSTDir"), "syntaxtree", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("vd", Activator.getString("JTBOpt.GnrtdVisDir"), "visitor", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("d", Activator.getString("JTBOpt.NDVD"), "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("ns", Activator.getString("JTBOpt.GnrtdSupCl"), "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("npfx", Activator.getString("JTBOpt.NodePrefix"), "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    jOptionSet.add(new Option("nsfx", Activator.getString("JTBOpt.NodeSuffix"), "", Option.STRING)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

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