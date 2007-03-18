package sf.eclipse.javacc.options;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Composite;

import sf.eclipse.javacc.IJJConstants;

/**
 * The Tab for JJDoc options
 * 
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */
public class JJDocOptions extends JJAbstractTab implements IJJConstants {
  // Memo beware JJDoc uses ":" instead of "="
  // TEXT (default false) 
  //     Setting TEXT to true causes JJDoc to generate a plain text.
  // ONE_TABLE (default true) 
  //     The default value of ONE_TABLE is used to generate a single HTML table.
  // OUTPUT_FILE 
  // or JJDOC_OUTPUT_FILE (Proposed for a future version)
  //     The default behavior is to put the JJDoc output into
  //     a file with either .html or .txt added as a suffix to
  //     the input file's base name.
  // OUTPUT_DIRECTORY
  //     Added in javaCC 3.1
  // JJDOC_OUTPUT_DIRECTORY (default: current directory)
  //     Proposed for a future version
  // CSS (default "")
  //     Proposed for javaCC 4.1

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
    optionSet.add(new Option("TEXT", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("ONE_TABLE", "true", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$

    // string options    
    // path options
    // file options
    optionSet.add(new Option("CSS", "", Option.FILE)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("OUTPUT_FILE", "", Option.FILE)); //$NON-NLS-1$ //$NON-NLS-2$

    // Fix values to default values
    optionSet.resetToDefaultValues();
    
    // Super class fills the content from property and optionSet
    createContents();
    
    // For JJDoc add a section for File options at the project level
    if (resource.getType() == IResource.PROJECT) 
      addFileOptionsSection();
  }
  
  /**
   * Set defaults in Eclipse
   */
  public void performDefaults() {
    super.performDefaults();
  }  
}