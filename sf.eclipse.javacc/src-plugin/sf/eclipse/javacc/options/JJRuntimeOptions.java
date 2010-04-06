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
 * The Tab for JavaCC runtime options.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class JJRuntimeOptions extends Composite implements IJJConstants {

  // MMa 02/2010 : formatting and javadoc revision
  // MMa 04/2009 : formatting revision ; changed jar names

  /** The JavaCC jar file */
  protected Text               fJavaCCjarFile;
  /** The suppress warnings flag */
  protected BooleanFieldEditor fSuppressWarnings;
  /** The clear console flag */
  protected BooleanFieldEditor fClearConsole;
  /** the add JJNature flag */
  protected BooleanFieldEditor fJJNature;
  /** The JTB jar file */
  protected Text               fJtbjarFile;
  /** The Resource to work on */
  protected IResource          fResource;

  /**
   * Constructor for JJRuntimeOptions.
   * 
   * @param aParent the parent
   * @param aResource the resource
   */
  public JJRuntimeOptions(final Composite aParent, final IResource aResource) {
    super(aParent, SWT.NONE);
    fResource = aResource;

    // add layout
    final GridLayout layout = new GridLayout(1, false);
    setLayout(layout);
    setLayoutData(new GridData(GridData.FILL_BOTH));
    layout.marginWidth = 10;
    layout.marginHeight = 10;

    // add group
    final Group groupProject = new Group(this, SWT.NONE);
    groupProject.setText(Activator.getString("JJRuntimeOptions.Common_options_Group")); //$NON-NLS-1$
    groupProject.setLayout(layout);
    groupProject.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

    // add runtime_jar selection control
    final Composite subGroup = new Composite(groupProject, SWT.NONE);
    subGroup.setLayout(new GridLayout(4, false));
    new Label(subGroup, SWT.LEFT | SWT.HORIZONTAL)
                                                  .setText(Activator
                                                                    .getString("JJRuntimeOptions.Select_jar_files")); //$NON-NLS-1$
    new Label(subGroup, SWT.LEFT | SWT.HORIZONTAL).setText(""); //$NON-NLS-1$
    new Label(subGroup, SWT.LEFT | SWT.HORIZONTAL).setText(""); //$NON-NLS-1$
    new Label(subGroup, SWT.LEFT | SWT.HORIZONTAL).setText(""); //$NON-NLS-1$
    // add File Field Editor (no more FileFieldEditor)
    // code inspired by org.eclipse.jdt.internal.ui.preferences.OptionsConfigurationBlock
    new Label(subGroup, SWT.LEFT | SWT.HORIZONTAL)
                                                  .setText(Activator
                                                                    .getString("JJRuntimeOptions.Set_the_JavaCC_jar_file")); //$NON-NLS-1$
    fJavaCCjarFile = new Text(subGroup, SWT.BORDER | SWT.SINGLE);
    GridData gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    // Eclipse 3.5
    // import org.eclipse.jface.layout.PixelConverter
    // PixelConverter converter= new PixelConverter(aParent);
    // gd.widthHint = converter.convertWidthInCharsToPixels(50);
    // Eclipse 3.4
    gd.widthHint = 300;

    fJavaCCjarFile.setLayoutData(gd);
    Button browse = new Button(subGroup, SWT.PUSH);
    browse.setText(Activator.getString(Activator.getString("JJRuntimeOptions.Browse"))); //$NON-NLS-1$
    browse.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(@SuppressWarnings("unused") final SelectionEvent event) {
        final FileDialog dialog = new FileDialog(fJavaCCjarFile.getShell(), SWT.OPEN);
        dialog.setText(Activator.getString("JJRuntimeOptions.Choose_file")); //$NON-NLS-1$
        dialog.setFilterPath(fJavaCCjarFile.getText());
        final String path = dialog.open();
        if (path != null) {
          fJavaCCjarFile.setText(path);
        }
      }
    });
    // add "Variables..." button
    Button variables = new Button(subGroup, SWT.PUSH);
    variables.setText(Activator.getString(Activator.getString("JJRuntimeOptions.Variables"))); //$NON-NLS-1$
    variables.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(@SuppressWarnings("unused") final SelectionEvent event) {
        final StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(
                                                                                       fJavaCCjarFile
                                                                                                     .getShell());
        if (dialog.open() == Window.OK) {
          fJavaCCjarFile.insert(dialog.getVariableExpression());
        }
      }
    });
    // add jtb runtime_jar selection control    
    new Label(subGroup, SWT.LEFT | SWT.HORIZONTAL)
                                                  .setText(Activator
                                                                    .getString("JJRuntimeOptions.Set_the_jtb_jar_file")); //$NON-NLS-1$
    fJtbjarFile = new Text(subGroup, SWT.BORDER | SWT.SINGLE);
    gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    // Eclipse 3.5
    // import org.eclipse.jface.layout.PixelConverter
    // PixelConverter converter= new PixelConverter(aParent);
    // gd.widthHint = converter.convertWidthInCharsToPixels(50);
    // Eclipse 3.4
    gd.widthHint = 300;

    fJtbjarFile.setLayoutData(gd);
    browse = new Button(subGroup, SWT.PUSH);
    browse.setText(Activator.getString(Activator.getString("JJRuntimeOptions.Browse"))); //$NON-NLS-1$
    browse.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(@SuppressWarnings("unused") final SelectionEvent event) {
        final FileDialog dialog = new FileDialog(fJavaCCjarFile.getShell(), SWT.OPEN);
        dialog.setText(Activator.getString("JJRuntimeOptions.Choose_file")); //$NON-NLS-1$
        dialog.setFilterPath(fJtbjarFile.getText());
        final String path = dialog.open();
        if (path != null) {
          fJtbjarFile.setText(path);
        }
      }
    });
    // add "Variables..." button
    variables = new Button(subGroup, SWT.PUSH);
    variables.setText(Activator.getString(Activator.getString("JJRuntimeOptions.Variables"))); //$NON-NLS-1$
    variables.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(@SuppressWarnings("unused") final SelectionEvent event) {
        final StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(
                                                                                       fJavaCCjarFile
                                                                                                     .getShell());
        if (dialog.open() == Window.OK) {
          fJtbjarFile.insert(dialog.getVariableExpression());
        }
      }
    });

    // add Checkboxes for boolean values
    final Composite checkGroup = new Composite(groupProject, SWT.NONE);
    checkGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    fClearConsole = new BooleanFieldEditor(
                                           CLEAR_CONSOLE,
                                           Activator
                                                    .getString("JJRuntimeOptions.Clear_JavaCC_console_before_build"), checkGroup); //$NON-NLS-1$
    fJJNature = new BooleanFieldEditor(
                                       JJ_NATURE_NAME,
                                       Activator.getString("JJRuntimeOptions.Build_automatically_on_save"), checkGroup); //$NON-NLS-1$
    fSuppressWarnings = new BooleanFieldEditor(
                                               SUPPRESS_WARNINGS,
                                               Activator
                                                        .getString("JJRuntimeOptions.Automatically_suppress_warnings"), checkGroup); //$NON-NLS-1$

    // read and set values
    if (aResource != null) {
      final IProject project = aResource.getProject();
      final IScopeContext projectScope = new ProjectScope(project);
      final IEclipsePreferences prefs = projectScope.getNode(IJJConstants.ID);
      try {
        // set according to PersistentProperties
        fJavaCCjarFile.setText(prefs.get(RUNTIME_JJJAR, "")); //$NON-NLS-1$
        fJtbjarFile.setText(prefs.get(RUNTIME_JTBJAR, "")); //$NON-NLS-1$
        fClearConsole.setBooleanValue("true".equals((prefs.get(CLEAR_CONSOLE, "false")))); //$NON-NLS-1$ //$NON-NLS-2$
        final boolean hasJavaccNature = project.getDescription().hasNature(JJ_NATURE_ID);
        fJJNature.setBooleanValue(hasJavaccNature);
        fSuppressWarnings.setBooleanValue("true".equals((prefs.get(SUPPRESS_WARNINGS, "false")))); //$NON-NLS-1$ //$NON-NLS-2$
      } catch (final CoreException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Sets the defaults.
   */
  public void performDefaults() {
    // by default we use the jar in the plugin
    final URL installURL = Activator.getDefault().getBundle().getEntry("/"); //$NON-NLS-1$
    try {
      final URL resolvedURL = org.eclipse.core.runtime.FileLocator.resolve(installURL);
      String home = org.eclipse.core.runtime.FileLocator.toFileURL(resolvedURL).getFile();
      // returned String is like "/C:/workspace/sf.eclipse.javacc/jtb132.jar"
      if (home.startsWith("/") && home.startsWith(":", 2)) { //$NON-NLS-1$ //$NON-NLS-2$
        home = home.substring(1);
      }
      fJavaCCjarFile.setText(home + JAVACC_JAR_NAME);
      fJtbjarFile.setText(home + JTB_JAR_NAME);
    } catch (final IOException e) {
      e.printStackTrace();
    }
    fClearConsole.setBooleanValue(true);
    fJJNature.setBooleanValue(true);
    fSuppressWarnings.setBooleanValue(false);
    //    fCheckSpelling.setBooleanValue(true);
  }

  /**
   * Save settings in Properties (called by JJPropertyPage).
   * 
   * @return true if successful, false otherwise
   */
  public boolean performOk() {
    if (fResource != null) {
      final IProject project = fResource.getProject();
      final IScopeContext projectScope = new ProjectScope(project);
      final IEclipsePreferences prefs = projectScope.getNode(IJJConstants.ID);

      prefs.put(RUNTIME_JJJAR, fJavaCCjarFile.getText());
      prefs.put(RUNTIME_JTBJAR, fJtbjarFile.getText());
      prefs.put(JJ_NATURE, fJJNature.getBooleanValue() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
      prefs.put(CLEAR_CONSOLE, fClearConsole.getBooleanValue() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
      prefs.put(SUPPRESS_WARNINGS, fSuppressWarnings.getBooleanValue() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$

      // set the nature 
      JJNature.setJJNature(fJJNature.getBooleanValue(), project);

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