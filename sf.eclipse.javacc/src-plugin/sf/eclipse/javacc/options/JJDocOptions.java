package sf.eclipse.javacc.options;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Composite;

import sf.eclipse.javacc.IJJConstants;

/**
 * The Tab for JJDoc options.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class JJDocOptions extends JJAbstractTab implements IJJConstants {

  // MMa 02/2010 : formatting and javadoc revision ; fixed output file handling

  // TEXT (default false) 
  //     Setting TEXT to true causes JJDoc to generate a plain text.
  // BNF (default false)
  //     Setting BNF to true causes JJDoc to generate a pure BNF document.
  // ONE_TABLE (default true) 
  //     The default value of ONE_TABLE is used to generate a single HTML table.
  // OUTPUT_FILE 
  //     You can supply a different file name with this option. 
  // CSS (default "")
  //     This option allows you to specify a CSS file name. 

  /**
   * Initializes with JJDoc known options.
   * 
   * @param parent the parent
   * @param res the resource
   */
  public JJDocOptions(final Composite parent, final IResource res) {
    super(parent, res);

    // All options are saved in a single property
    fPreferenceName = JJDOC_OPTIONS;

    fOptionSet = new OptionSet(true);

    // int options
    // boolean options
    fNbColBooleans = 1;
    fOptionSet.add(new Option("BNF", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("ONE_TABLE", "true", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("TEXT", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    // string options    
    // path options
    // file options
    fOptionSet.add(new Option("CSS", "", Option.FILE)); //$NON-NLS-1$ //$NON-NLS-2$
    fOptionSet.add(new Option("OUTPUT_FILE", "", Option.FILE)); //$NON-NLS-1$ //$NON-NLS-2$

    // Fix values to default values
    fOptionSet.resetToDefaultValues();

    // Super class fills the content from property and optionSet
    createContents();

    //    // For JJDoc add a section for File options at the project level
    //    if (fResource.getType() == IResource.PROJECT) {
    //      addFileOptSection();
    //    }
  }

  /**
   * Sets defaults in Eclipse.
   */
  @Override
  public void performDefaults() {
    super.performDefaults();
  }
}