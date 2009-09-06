package sf.eclipse.javacc.options;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Composite;

import sf.eclipse.javacc.IJJConstants;

/**
 * The Tab for JJDoc options
 * 
 * @author Remi Koutcherawy 2003-2006
 * CeCILL License http://www.cecill.info/index.en.html
 */
public class JJDocOptions extends JJAbstractTab implements IJJConstants {
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
   * Initialize with JJDoc known options
   */
  public JJDocOptions(Composite parent, IResource res) {
    super(parent, res);
 
    // All options are saved in a single property
    preferenceName = JJDOC_OPTIONS;
    optionSet = new OptionSet();

    // int options
    // boolean options
    optionSet.add(new Option("TEXT", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
    optionSet.add(new Option("BNF", "false", Option.BOOLEAN)); //$NON-NLS-1$ //$NON-NLS-2$
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