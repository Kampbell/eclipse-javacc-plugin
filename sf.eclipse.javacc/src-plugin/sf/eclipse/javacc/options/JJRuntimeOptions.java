package sf.eclipse.javacc.options;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.osgi.service.prefs.BackingStoreException;

import sf.eclipse.javacc.Activator;
import sf.eclipse.javacc.IJJConstants;
import sf.eclipse.javacc.JJNature;

/**
 * The Tab for JavaCC runtime options
 *
 * @author Remi Koutcherawy 2003-2006
 * CeCILL License http://www.cecill.info/index.en.html
 */
public class JJRuntimeOptions extends Composite implements IJJConstants {

  // Controls
  protected FileFieldEditor jarFile;
  protected BooleanFieldEditor checkSuppressWarnings;
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
  public JJRuntimeOptions(final Composite parent, final IResource res, final boolean isFile) {
    super(parent, SWT.NONE);
    this.isFile = isFile;
    this.resource = res;
    final GridLayout layout = new GridLayout();
    setLayout(layout);
    setLayoutData(new GridData(GridData.FILL_BOTH));
    layout.marginWidth = 10;
    layout.marginHeight = 10;

    // Adds Project options
    final Group groupProject = new Group(this, SWT.NONE);
    groupProject.setText(Activator.getString("JJRuntimeOptions.Shared_project_options_Group")); //$NON-NLS-1$
    groupProject.setLayout (new GridLayout());
    groupProject.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    // Adds runtime_jar selection control
    final Composite subGroup = new Composite(groupProject, SWT.NONE);
    new Label(subGroup,SWT.LEFT | SWT.HORIZONTAL).setText(Activator.getString("JJRuntimeOptions.Select_jar_file")); //$NON-NLS-1$
    new Label(subGroup,SWT.LEFT | SWT.HORIZONTAL).setText(Activator.getString("JJRuntimeOptions.Give_an_absolute_path")); //$NON-NLS-1$
    new Label(subGroup,SWT.LEFT | SWT.HORIZONTAL).setText(""); //$NON-NLS-1$
    jarFile = new FileFieldEditor(RUNTIME_JAR,
      Activator.getString("JJRuntimeOptions.Set_the_JavaCC_jar_file"), subGroup); //$NON-NLS-1$

    // Adds jtb runtime_jar selection control
    final Composite jtbGroup = new Composite(groupProject, SWT.NONE);
    new Label(jtbGroup,SWT.LEFT | SWT.HORIZONTAL).setText(Activator.getString("JJRuntimeOptions.Select_jtb_jar_file")); //$NON-NLS-1$
    new Label(jtbGroup,SWT.LEFT | SWT.HORIZONTAL).setText(Activator.getString("JJRuntimeOptions.Give_an_absolute_path")); //$NON-NLS-1$
    new Label(jtbGroup,SWT.LEFT | SWT.HORIZONTAL).setText(""); //$NON-NLS-1$
    jtbjarFile = new FileFieldEditor(RUNTIME_JTBJAR,
      Activator.getString("JJRuntimeOptions.Set_the_jtb_jar_file"), subGroup); //$NON-NLS-1$

    // Add Checkboxes for boolean values
    final Composite checkGroup = new Composite(groupProject, SWT.NONE);
    checkGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    checkClearConsole = new BooleanFieldEditor(CLEAR_CONSOLE,
	Activator.getString("JJRuntimeOptions.Clear_JavaCC_console_before_build"), checkGroup); //$NON-NLS-1$
    checkJJNature = new BooleanFieldEditor(JJ_NATURE_NAME,
	Activator.getString("JJRuntimeOptions.Build_automatically_on_save"), checkGroup);           //$NON-NLS-1$
    checkSuppressWarnings = new BooleanFieldEditor(SUPPRESS_WARNINGS,
	Activator.getString("JJRuntimeOptions.Automatically_suppress_warnings"), checkGroup); //$NON-NLS-1$

    // Reads and sets values
    if (res != null) {
      final IProject proj = res.getProject();
      final IScopeContext projectScope = new ProjectScope(proj);
      final IEclipsePreferences prefs = projectScope.getNode(IJJConstants.ID);
      try {
        // Sets according to PersistentProperties
        jarFile.setStringValue(prefs.get(RUNTIME_JAR, "")); //$NON-NLS-1$
        jtbjarFile.setStringValue(prefs.get(RUNTIME_JTBJAR, "")); //$NON-NLS-1$
        checkClearConsole.setBooleanValue("true".equals((prefs.get(CLEAR_CONSOLE, "false")))); //$NON-NLS-1$ //$NON-NLS-2$
        final boolean hasJavaccNature = proj.getDescription().hasNature(JJ_NATURE_ID);
        checkJJNature.setBooleanValue(hasJavaccNature);
        checkSuppressWarnings.setBooleanValue("true".equals((prefs.get(SUPPRESS_WARNINGS, "false")))); //$NON-NLS-1$ //$NON-NLS-2$
       } catch (final CoreException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Just set defaults
   */
  public void performDefaults() {
    jarFile.setStringValue("");  //$NON-NLS-1$
    jtbjarFile.setStringValue("");  //$NON-NLS-1$
    checkClearConsole.setBooleanValue(true);
    checkSuppressWarnings.setBooleanValue(false);
    checkJJNature.setBooleanValue(true);
  }

  /**
   * Called by JJPropertyPage to save settings in Properties
   */
  public boolean performOk() {
   // Reads and store values
    IResource res = resource;
    if (res != null) 
    {
        IProject proj = res.getProject();
        IScopeContext projectScope = new ProjectScope(proj);
        IEclipsePreferences prefs = projectScope.getNode(IJJConstants.ID);

        prefs.put(RUNTIME_JAR, jarFile.getStringValue());
        prefs.put(CLEAR_CONSOLE, checkClearConsole.getBooleanValue() ? "true":"false"); //$NON-NLS-1$ //$NON-NLS-2$
        prefs.put(SUPPRESS_WARNINGS, checkSuppressWarnings.getBooleanValue() ? "true":"false"); //$NON-NLS-1$ //$NON-NLS-2$
        prefs.put(JJ_NATURE, checkJJNature.getBooleanValue() ? "true":"false"); //$NON-NLS-1$ //$NON-NLS-2$
        
        // Sets the nature directly
        JJNature.setJJNature(checkJJNature.getBooleanValue(), proj);
        
        try 
        {
              prefs.flush();
        } catch (BackingStoreException e) {
              e.printStackTrace();
              return false;
        }
    }
    return true;
  }
}