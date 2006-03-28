package sf.eclipse.javacc.options;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import sf.eclipse.javacc.Activator;
import sf.eclipse.javacc.IJJConstants;
import sf.eclipse.javacc.JJNature;

/**
 * The Tab for JavaCC runtime options
 * 
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */
public class JJRuntimeOptions extends Composite implements IJJConstants {
  
  // Controls
  protected FileFieldEditor jarFile;
  protected BooleanFieldEditor checkProjectOverride;
  protected BooleanFieldEditor checkShowConsole;
  protected BooleanFieldEditor checkClearConsole;
  protected BooleanFieldEditor checkJJNature;
  protected FileFieldEditor jtbjarFile;
  
  // The Resource to work on.
  protected IResource resource;
  protected boolean isFile;

  /**
   * Constructor for JJRuntimeOptions.
   * @param parent
   * @param style
   */
  public JJRuntimeOptions(Composite parent, IResource res, boolean isFile) {
    super(parent, SWT.NONE);
    this.isFile = isFile;
    this.resource = res;
    GridLayout layout = new GridLayout();
    setLayout(layout);
    setLayoutData(new GridData(GridData.FILL_BOTH));
    layout.marginWidth = 10;
    layout.marginHeight = 10;

    // Adds Project options
    Group groupProject = new Group(this, SWT.NONE);
    groupProject.setText(Activator.getString("JJRuntimeOptions.Shared_project_options_Group")); //$NON-NLS-1$
    groupProject.setLayout (new GridLayout());
    groupProject.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    // Adds runtime_jar selection control
    Composite subGroup = new Composite(groupProject, SWT.NONE);
    new Label(subGroup,SWT.LEFT | SWT.HORIZONTAL).setText(Activator.getString("JJRuntimeOptions.Select_jar_file")); //$NON-NLS-1$
    new Label(subGroup,SWT.LEFT | SWT.HORIZONTAL).setText(Activator.getString("JJRuntimeOptions.Give_an_absolute_path")); //$NON-NLS-1$
    new Label(subGroup,SWT.LEFT | SWT.HORIZONTAL).setText(""); //$NON-NLS-1$
    jarFile = new FileFieldEditor(RUNTIME_JAR,
      Activator.getString("JJRuntimeOptions.Set_the_JavaCC_jar_file"), subGroup); //$NON-NLS-1$
//    jarFile.setFileExtensions(new String[] {"*.jar", "*.zip"}); //$NON-NLS-1$ //$NON-NLS-2$
    
    // Add Checkboxes for boolean values
    Composite checkGroup = new Composite(groupProject, SWT.NONE);
    checkGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    checkShowConsole = new BooleanFieldEditor(SHOW_CONSOLE,
	Activator.getString("JJRuntimeOptions.Show_JavaCC_output_in_console"), checkGroup); //$NON-NLS-1$
    checkClearConsole = new BooleanFieldEditor(CLEAR_CONSOLE,
	Activator.getString("JJRuntimeOptions.Clear_JavaCC_console_before_build"), checkGroup); //$NON-NLS-1$
    checkJJNature = new BooleanFieldEditor(JJ_NATURE_NAME,
	Activator.getString("JJRuntimeOptions.Build_automatically_on_save"), checkGroup);           //$NON-NLS-1$
    checkProjectOverride = new BooleanFieldEditor(PROJECT_OVERRIDE,
	Activator.getString("JJRuntimeOptions.Project_options_override_File_options"), checkGroup); //$NON-NLS-1$
    
    // Adds jtb runtime_jar selection control
    Composite jtbGroup = new Composite(groupProject, SWT.NONE);
    new Label(jtbGroup,SWT.LEFT | SWT.HORIZONTAL).setText("JJRuntimeOptions.Select_jtb_jar_file"); //$NON-NLS-1$
    new Label(jtbGroup,SWT.LEFT | SWT.HORIZONTAL).setText(Activator.getString("JJRuntimeOptions.Give_an_absolute_path")); //$NON-NLS-1$
    new Label(jtbGroup,SWT.LEFT | SWT.HORIZONTAL).setText(""); //$NON-NLS-1$
    jtbjarFile = new FileFieldEditor(RUNTIME_JAR,
      Activator.getString("JJRuntimeOptions.Set_the_jtb_jar_file"), subGroup); //$NON-NLS-1$
//    jtbjarFile.setFileExtensions(new String[] {"*.jar", "*.zip"}); //$NON-NLS-1$ //$NON-NLS-2$

    // Reads and sets values
    if (res != null) {
      IProject proj = res.getProject();
      try {
        // Sets according to PersistentProperties
        jarFile.setStringValue(proj.getPersistentProperty(QN_RUNTIME_JAR));
        checkShowConsole.setBooleanValue("true".equals((proj.getPersistentProperty( //$NON-NLS-1$
          QN_SHOW_CONSOLE))));        
        checkClearConsole.setBooleanValue("true".equals((proj.getPersistentProperty( //$NON-NLS-1$
          QN_CLEAR_CONSOLE))));
        boolean hasJavaccNature = proj.getDescription().hasNature(JJ_NATURE_ID);
        checkJJNature.setBooleanValue(hasJavaccNature);
        checkProjectOverride.setBooleanValue("true".equals((proj.getPersistentProperty( //$NON-NLS-1$
            QN_PROJECT_OVERRIDE))));
        jtbjarFile.setStringValue(proj.getPersistentProperty(QN_RUNTIME_JTBJAR));
       } catch (CoreException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Just set defaults
   */
  public void performDefaults() {
    jarFile.setStringValue(Activator.getString("JJBuilder.defaultJar"));  //$NON-NLS-1$
    jtbjarFile.setStringValue(Activator.getString("JJBuilder.defaultJtbJar"));  //$NON-NLS-1$
    checkShowConsole.setBooleanValue(true); 
    checkClearConsole.setBooleanValue(false);
    checkProjectOverride.setBooleanValue(true); 
    checkJJNature.setBooleanValue(true);
  }

  /**
   * Called by JJPropertyPage to save settings in Properties
   */
  public boolean performOk() {
   // Reads and store values
    IResource res = resource;
    if (res != null) {
      IProject proj = res.getProject();
      try {
       proj.setPersistentProperty(QN_RUNTIME_JAR,
         jarFile.getStringValue());
       proj.setPersistentProperty(QN_RUNTIME_JTBJAR,
         jtbjarFile.getStringValue());  
       proj.setPersistentProperty(QN_SHOW_CONSOLE,
        checkShowConsole.getBooleanValue() ? "true":"false"); //$NON-NLS-1$ //$NON-NLS-2$
       proj.setPersistentProperty(QN_CLEAR_CONSOLE,
        checkClearConsole.getBooleanValue() ? "true":"false"); //$NON-NLS-1$ //$NON-NLS-2$
       proj.setPersistentProperty(QN_PROJECT_OVERRIDE,
         checkProjectOverride.getBooleanValue() ? "true":"false"); //$NON-NLS-1$ //$NON-NLS-2$
       proj.setPersistentProperty(QN_JJ_NATURE,
         checkJJNature.getBooleanValue() ? "true":"false"); //$NON-NLS-1$ //$NON-NLS-2$
     
       // Sets the nature directly
       JJNature.setJJNature(checkJJNature.getBooleanValue(), proj);
     } catch (CoreException e) {
        e.printStackTrace();
      }
    }
    return true;
  }
  
  /**
   * Sets a PropertyChangeListener to CheckBox "Project Override"
   *  "Add Nature" and "Exclude from build" to handle immediately
   * @param jJOptions
   */
  public void setPropertyChangeListener(IPropertyChangeListener listener) {
      checkProjectOverride.setPropertyChangeListener(listener);
      checkJJNature.setPropertyChangeListener(listener);
  }
}