package sf.eclipse.javacc.options;

import static sf.eclipse.javacc.base.IConstants.JJDOC_OPTIONS;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Composite;

import sf.eclipse.javacc.base.Option;
import sf.eclipse.javacc.base.OptionSet;

/**
 * The Tab for JJDoc options.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 * @author Bill Fenlason 2012
 */
class JJDocOptions extends OptionsAbstractTab {

  // MMa 02/2010 : formatting and javadoc revision ; fixed output file handling
  // BF  06/2012 : removed redundant superinterface to prevent warning

  // TEXT (default false) 
  //     Setting TEXT to true causes JJDoc to generate a plain text
  // BNF (default false)
  //     Setting BNF to true causes JJDoc to generate a pure BNF document
  // ONE_TABLE (default true) 
  //     The default value of ONE_TABLE is used to generate a single HTML table
  // OUTPUT_FILE 
  //     You can supply a different file name with this option
  // CSS (default "")
  //     This option allows you to specify a CSS file name

  /**
   * Initializes with JJDoc known options.
   * 
   * @param aParent - the parent
   * @param aRes - the resource
   */
  public JJDocOptions(final Composite aParent, final IResource aRes) {
    super(aParent, aRes);

    // All options are saved in a single property
    jPreferenceName = JJDOC_OPTIONS;

    jOptionSet = new OptionSet(true);

    // int options
    // boolean options
    jNbColBooleans = 1;
    jOptionSet.add(new Option("BNF", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    jOptionSet.add(new Option("ONE_TABLE", "true", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    jOptionSet.add(new Option("TEXT", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    // string options    
    // path options
    // file options
    jOptionSet.add(new Option("CSS", "", Option.FILE)); //$NON-NLS-1$ //$NON-NLS-2$
    jOptionSet.add(new Option("OUTPUT_FILE", "", Option.FILE)); //$NON-NLS-1$ //$NON-NLS-2$

    // Fix values to default values
    jOptionSet.resetToDefaultValues();

    // Super class fills the content from property and optionSet
    createContents();

    //    // For JJDoc add a section for File options at the project level
    //    if (fResource.getType() == IResource.PROJECT) {
    //      addFileOptSection();
    //    }
  }

  /**
   * Sets defaults in Eclipse.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public void performDefaults() {
    super.performDefaults();
  }
}