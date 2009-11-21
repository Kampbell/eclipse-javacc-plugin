package sf.eclipse.javacc.options;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.prefs.BackingStoreException;

import sf.eclipse.javacc.Activator;
import sf.eclipse.javacc.IJJConstants;
import sf.eclipse.javacc.JJNature;

/**
 * The Tab for JavaCC runtime options
 * 
 * @author Remi Koutcherawy 2003-2009 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009
 */
public class JJRuntimeOptions extends Composite implements IJJConstants {

  // MMa 04/09 : formatting revision ; changed jar names

  // Controls
  protected Text               jarFile;
  protected BooleanFieldEditor checkSuppressWarnings;
  protected BooleanFieldEditor checkClearConsole;
  protected BooleanFieldEditor checkJJNature;
  protected Text               jtbjarFile;

  // The Resource to work on.
  protected IResource          resource;

  /**
   * Constructor for JJRuntimeOptions.
   * 
   * @param parent
   * @param style
   */
  public JJRuntimeOptions(final Composite parent, final IResource res) {
    super(parent, SWT.NONE);
    this.resource = res;
    final GridLayout layout = new GridLayout(1, false);
    setLayout(layout);
    setLayoutData(new GridData(GridData.FILL_BOTH));
    layout.marginWidth = 10;
    layout.marginHeight = 10;

    // Adds Project options
    final Group groupProject = new Group(this, SWT.NONE);
    groupProject.setText(Activator.getString("JJRuntimeOptions.Shared_project_options_Group")); //$NON-NLS-1$
    groupProject.setLayout(layout);
    groupProject.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    // Adds runtime_jar selection control
    final Composite subGroup = new Composite(groupProject, SWT.NONE);
    subGroup.setLayout(new GridLayout(4, false));
    new Label(subGroup, SWT.LEFT | SWT.HORIZONTAL)
                                                  .setText(Activator
                                                                    .getString("JJRuntimeOptions.Select_jar_file")); //$NON-NLS-1$
    new Label(subGroup, SWT.LEFT | SWT.HORIZONTAL).setText(""); //$NON-NLS-1$
    new Label(subGroup, SWT.LEFT | SWT.HORIZONTAL).setText(""); //$NON-NLS-1$
    new Label(subGroup, SWT.LEFT | SWT.HORIZONTAL).setText(""); //$NON-NLS-1$
    // Add File Field Editor (no more FileFieldEditor)
    // Code inspired by org.eclipse.jdt.internal.ui.preferences.OptionsConfigurationBlock
    new Label(subGroup, SWT.LEFT | SWT.HORIZONTAL)
                                                  .setText(Activator
                                                                    .getString("JJRuntimeOptions.Set_the_JavaCC_jar_file")); //$NON-NLS-1$
    jarFile = new Text(subGroup, SWT.BORDER | SWT.SINGLE);
    GridData gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    // Eclipse 3.5
    // import org.eclipse.jface.layout.PixelConverter
    // PixelConverter converter= new PixelConverter(parent);
    // gd.widthHint = converter.convertWidthInCharsToPixels(50);

    // Eclipse 3.4
    gd.widthHint = 300;

    jarFile.setLayoutData(gd);
    Button browse = new Button(subGroup, SWT.PUSH);
    browse.setText(Activator.getString(Activator.getString("JJRuntimeOptions.Browse"))); //$NON-NLS-1$
    browse.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(final SelectionEvent event) {
        final FileDialog dialog = new FileDialog(jarFile.getShell(), SWT.OPEN);
        dialog.setText(Activator.getString("JJRuntimeOptions.Choose_file")); //$NON-NLS-1$
        dialog.setFilterPath(jarFile.getText());
        final String path = dialog.open();
        if (path != null) {
          jarFile.setText(path);
        }
      }
    });
    // Add "Variables..." button
    Button variables = new Button(subGroup, SWT.PUSH);
    variables.setText(Activator.getString(Activator.getString("JJRuntimeOptions.Variables"))); //$NON-NLS-1$
    variables.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(final SelectionEvent e) {
        final StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(jarFile.getShell());
        if (dialog.open() == Window.OK) {
          jarFile.insert(dialog.getVariableExpression());
        }
      }
    });
    // Adds jtb runtime_jar selection control    
    new Label(subGroup, SWT.LEFT | SWT.HORIZONTAL)
                                                  .setText(Activator
                                                                    .getString("JJRuntimeOptions.Set_the_jtb_jar_file")); //$NON-NLS-1$
    jtbjarFile = new Text(subGroup, SWT.BORDER | SWT.SINGLE);
    gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    // Eclipse 3.5
    // import org.eclipse.jface.layout.PixelConverter
    // PixelConverter converter= new PixelConverter(parent);
    // gd.widthHint = converter.convertWidthInCharsToPixels(50);

    // Eclipse 3.4
    gd.widthHint = 300;

    jtbjarFile.setLayoutData(gd);
    browse = new Button(subGroup, SWT.PUSH);
    browse.setText(Activator.getString(Activator.getString("JJRuntimeOptions.Browse"))); //$NON-NLS-1$
    browse.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(final SelectionEvent event) {
        final FileDialog dialog = new FileDialog(jarFile.getShell(), SWT.OPEN);
        dialog.setText(Activator.getString("JJRuntimeOptions.Choose_file")); //$NON-NLS-1$
        dialog.setFilterPath(jtbjarFile.getText());
        final String path = dialog.open();
        if (path != null) {
          jtbjarFile.setText(path);
        }
      }
    });
    // Add "Variables..." button
    variables = new Button(subGroup, SWT.PUSH);
    variables.setText(Activator.getString(Activator.getString("JJRuntimeOptions.Variables"))); //$NON-NLS-1$
    variables.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(final SelectionEvent e) {
        final StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(jarFile.getShell());
        if (dialog.open() == Window.OK) {
          jtbjarFile.insert(dialog.getVariableExpression());
        }
      }
    });

    // Add Checkboxes for boolean values
    final Composite checkGroup = new Composite(groupProject, SWT.NONE);
    checkGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    checkClearConsole = new BooleanFieldEditor(
                                               CLEAR_CONSOLE,
                                               Activator
                                                        .getString("JJRuntimeOptions.Clear_JavaCC_console_before_build"), checkGroup); //$NON-NLS-1$
    checkJJNature = new BooleanFieldEditor(
                                           JJ_NATURE_NAME,
                                           Activator
                                                    .getString("JJRuntimeOptions.Build_automatically_on_save"), checkGroup); //$NON-NLS-1$
    checkSuppressWarnings = new BooleanFieldEditor(
                                                   SUPPRESS_WARNINGS,
                                                   Activator
                                                            .getString("JJRuntimeOptions.Automatically_suppress_warnings"), checkGroup); //$NON-NLS-1$

    // Reads and sets values
    if (res != null) {
      final IProject proj = res.getProject();
      final IScopeContext projectScope = new ProjectScope(proj);
      final IEclipsePreferences prefs = projectScope.getNode(IJJConstants.ID);
      try {
        // Sets according to PersistentProperties
        jarFile.setText(prefs.get(RUNTIME_JJJAR, "")); //$NON-NLS-1$
        jtbjarFile.setText(prefs.get(RUNTIME_JTBJAR, "")); //$NON-NLS-1$
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
    // By default we use the jar in the plugin
    final URL installURL = Activator.getDefault().getBundle().getEntry("/"); //$NON-NLS-1$
    try {
      final URL resolvedURL = org.eclipse.core.runtime.FileLocator.resolve(installURL);
      String home = org.eclipse.core.runtime.FileLocator.toFileURL(resolvedURL).getFile();
      // Returned String is like "/C:/workspace/sf.eclipse.javacc/jtb132.jar"
      if (home.startsWith("/") && home.startsWith(":", 2)) { //$NON-NLS-1$ //$NON-NLS-2$
        home = home.substring(1);
      }
      jarFile.setText(home + JAVACC_JAR_NAME);
      jtbjarFile.setText(home + JTB_JAR_NAME);
    } catch (final IOException e) {
      e.printStackTrace();
    }
    checkClearConsole.setBooleanValue(true);
    checkSuppressWarnings.setBooleanValue(false);
    checkJJNature.setBooleanValue(true);
  }

  /**
   * Called by JJPropertyPage to save settings in Properties
   */
  public boolean performOk() {
    // Reads and store values
    final IResource res = resource;
    if (res != null) {
      final IProject proj = res.getProject();
      final IScopeContext projectScope = new ProjectScope(proj);
      final IEclipsePreferences prefs = projectScope.getNode(IJJConstants.ID);

      prefs.put(RUNTIME_JJJAR, jarFile.getText());
      prefs.put(RUNTIME_JTBJAR, jtbjarFile.getText());
      prefs.put(CLEAR_CONSOLE, checkClearConsole.getBooleanValue() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
      prefs.put(SUPPRESS_WARNINGS, checkSuppressWarnings.getBooleanValue() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
      prefs.put(JJ_NATURE, checkJJNature.getBooleanValue() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$

      // Sets the nature directly
      JJNature.setJJNature(checkJJNature.getBooleanValue(), proj);

      try {
        prefs.flush();
      } catch (final BackingStoreException e) {
        e.printStackTrace();
        return false;
      }
    }
    return true;
  }
}