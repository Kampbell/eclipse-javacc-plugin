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

import sf.eclipse.javacc.base.IJJConstants;
import sf.eclipse.javacc.head.Activator;
import sf.eclipse.javacc.head.JJNature;

/**
 * The Tab for the JavaCC global options.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011
 */
public class GlobalOptions extends Composite implements IJJConstants {

  // MMa 02/2010 : formatting and javadoc revision
  // MMa 04/2009 : formatting revision ; changed jar names
  // MMa 08/2011 : added mark generated files as derived option RFE 3314103
  // MMa 08/2011 : renamed

  /** The JavaCC jar file */
  protected Text               jJavaCCjarFile;
  /** The suppress warnings flag */
  protected BooleanFieldEditor jSuppressWarnings;
  /** The mark generated files as derived flag */
  protected BooleanFieldEditor jMarkGenFilesAsDerived;
  /** The clear console flag */
  protected BooleanFieldEditor jClearConsole;
  /** the add JJNature flag */
  protected BooleanFieldEditor jJJNature;
  /** The JTB jar file */
  protected Text               jJTBJarFile;
  /** The Resource to work on */
  protected IResource          jResource;

  /**
   * Constructor for GlobalOptions.
   * 
   * @param aParent the parent
   * @param aResource the resource
   */
  public GlobalOptions(final Composite aParent, final IResource aResource) {
    super(aParent, SWT.NONE);
    jResource = aResource;

    // add layout
    final GridLayout layout = new GridLayout(1, false);
    setLayout(layout);
    setLayoutData(new GridData(GridData.FILL_BOTH));
    layout.marginWidth = 10;
    layout.marginHeight = 10;

    // add group
    final Group groupProject = new Group(this, SWT.NONE);
    groupProject.setText(Activator.getString("GlobalOptions.Common_options_Group")); //$NON-NLS-1$
    groupProject.setLayout(layout);
    groupProject.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

    // add runtime_jar selection control
    final Composite subGroup = new Composite(groupProject, SWT.NONE);
    subGroup.setLayout(new GridLayout(4, false));
    new Label(subGroup, SWT.LEFT | SWT.HORIZONTAL).setText(Activator.getString("GlobalOptions.Select_jar_files")); //$NON-NLS-1$
    new Label(subGroup, SWT.LEFT | SWT.HORIZONTAL).setText(""); //$NON-NLS-1$
    new Label(subGroup, SWT.LEFT | SWT.HORIZONTAL).setText(""); //$NON-NLS-1$
    new Label(subGroup, SWT.LEFT | SWT.HORIZONTAL).setText(""); //$NON-NLS-1$
    // add File Field Editor (no more FileFieldEditor)
    // code inspired by org.eclipse.jdt.internal.ui.preferences.OptionsConfigurationBlock
    new Label(subGroup, SWT.LEFT | SWT.HORIZONTAL).setText(Activator.getString("GlobalOptions.Set_the_JavaCC_jar_file")); //$NON-NLS-1$
    jJavaCCjarFile = new Text(subGroup, SWT.BORDER | SWT.SINGLE);
    GridData gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    // Eclipse 3.5
    // import org.eclipse.jface.layout.PixelConverter
    // PixelConverter converter= new PixelConverter(aParent);
    // gd.widthHint = converter.convertWidthInCharsToPixels(50);
    // Eclipse 3.4
    gd.widthHint = 300;

    jJavaCCjarFile.setLayoutData(gd);
    Button browse = new Button(subGroup, SWT.PUSH);
    browse.setText(Activator.getString(Activator.getString("GlobalOptions.Browse"))); //$NON-NLS-1$
    browse.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(@SuppressWarnings("unused") final SelectionEvent event) {
        final FileDialog dialog = new FileDialog(jJavaCCjarFile.getShell(), SWT.OPEN);
        dialog.setText(Activator.getString("GlobalOptions.Choose_file")); //$NON-NLS-1$
        dialog.setFilterPath(jJavaCCjarFile.getText());
        final String path = dialog.open();
        if (path != null) {
          jJavaCCjarFile.setText(path);
        }
      }
    });
    // add "Variables..." button
    Button variables = new Button(subGroup, SWT.PUSH);
    variables.setText(Activator.getString(Activator.getString("GlobalOptions.Variables"))); //$NON-NLS-1$
    variables.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(@SuppressWarnings("unused") final SelectionEvent event) {
        final StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(
                                                                                       jJavaCCjarFile.getShell());
        if (dialog.open() == Window.OK) {
          jJavaCCjarFile.insert(dialog.getVariableExpression());
        }
      }
    });
    // add jtb runtime_jar selection control    
    new Label(subGroup, SWT.LEFT | SWT.HORIZONTAL).setText(Activator.getString("GlobalOptions.Set_the_jtb_jar_file")); //$NON-NLS-1$
    jJTBJarFile = new Text(subGroup, SWT.BORDER | SWT.SINGLE);
    gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    // Eclipse 3.5
    // import org.eclipse.jface.layout.PixelConverter
    // PixelConverter converter= new PixelConverter(aParent);
    // gd.widthHint = converter.convertWidthInCharsToPixels(50);
    // Eclipse 3.4
    gd.widthHint = 300;

    jJTBJarFile.setLayoutData(gd);
    browse = new Button(subGroup, SWT.PUSH);
    browse.setText(Activator.getString(Activator.getString("GlobalOptions.Browse"))); //$NON-NLS-1$
    browse.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(@SuppressWarnings("unused") final SelectionEvent event) {
        final FileDialog dialog = new FileDialog(jJavaCCjarFile.getShell(), SWT.OPEN);
        dialog.setText(Activator.getString("GlobalOptions.Choose_file")); //$NON-NLS-1$
        dialog.setFilterPath(jJTBJarFile.getText());
        final String path = dialog.open();
        if (path != null) {
          jJTBJarFile.setText(path);
        }
      }
    });
    // add "Variables..." button
    variables = new Button(subGroup, SWT.PUSH);
    variables.setText(Activator.getString(Activator.getString("GlobalOptions.Variables"))); //$NON-NLS-1$
    variables.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(@SuppressWarnings("unused") final SelectionEvent event) {
        final StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(
                                                                                       jJavaCCjarFile.getShell());
        if (dialog.open() == Window.OK) {
          jJTBJarFile.insert(dialog.getVariableExpression());
        }
      }
    });

    // add Checkboxes for boolean values
    final Composite checkGroup = new Composite(groupProject, SWT.NONE);
    checkGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    final String def = " (".concat(Activator.getString(Activator.getString("JJAbstractTab.default"))) //$NON-NLS-1$  //$NON-NLS-2$ 
                           .concat(" "); //$NON-NLS-1$ 
    String str;
    str = Activator.getString(Activator.getString("GlobalOptions.Clear_JavaCC_console_before_build")).concat(def) //$NON-NLS-1$
                   .concat(DEF_CLEAR_CONSOLE).concat(")"); //$NON-NLS-1$
    jClearConsole = new BooleanFieldEditor(CLEAR_CONSOLE, str, checkGroup);

    str = Activator.getString(Activator.getString("GlobalOptions.Build_automatically_on_save")).concat(def) //$NON-NLS-1$
                   .concat(DEF_JJ_NATURE).concat(")"); //$NON-NLS-1$
    jJJNature = new BooleanFieldEditor(JJ_NATURE_NAME, str, checkGroup);

    str = Activator.getString(Activator.getString("GlobalOptions.Automatically_suppress_warnings")).concat(def) //$NON-NLS-1$
                   .concat(DEF_SUPPRESS_WARNINGS).concat(")"); //$NON-NLS-1$
    jSuppressWarnings = new BooleanFieldEditor(SUPPRESS_WARNINGS, str, checkGroup);

    str = Activator.getString(Activator.getString("GlobalOptions.Mark_generated_files_as_derived")).concat(def) //$NON-NLS-1$
                   .concat(DEF_MARK_GEN_FILES_AS_DERIVED).concat(")"); //$NON-NLS-1$
    jMarkGenFilesAsDerived = new BooleanFieldEditor(MARK_GEN_FILES_AS_DERIVED, str, checkGroup);

    // read and set values
    if (aResource != null) {
      final IProject project = aResource.getProject();
      final IScopeContext projectScope = new ProjectScope(project);
      final IEclipsePreferences prefs = projectScope.getNode(IJJConstants.ID);
      try {
        // set according to PersistentProperties
        jJavaCCjarFile.setText(prefs.get(RUNTIME_JJJAR, "")); //$NON-NLS-1$
        jJTBJarFile.setText(prefs.get(RUNTIME_JTBJAR, "")); //$NON-NLS-1$
        jClearConsole.setBooleanValue(isTrue(prefs.get(CLEAR_CONSOLE, DEF_CLEAR_CONSOLE)));
        final boolean hasJavaccNature = project.getDescription().hasNature(JJ_NATURE_ID);
        jJJNature.setBooleanValue(hasJavaccNature);
        jSuppressWarnings.setBooleanValue(isTrue(prefs.get(SUPPRESS_WARNINGS, DEF_SUPPRESS_WARNINGS)));
        jMarkGenFilesAsDerived.setBooleanValue(isTrue(prefs.get(MARK_GEN_FILES_AS_DERIVED,
                                                                DEF_MARK_GEN_FILES_AS_DERIVED)));
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
      jJavaCCjarFile.setText(home + JAVACC_JAR_NAME);
      jJTBJarFile.setText(home + JTB_JAR_NAME);
    } catch (final IOException e) {
      e.printStackTrace();
    }
    jClearConsole.setBooleanValue(isTrue(DEF_CLEAR_CONSOLE));
    jJJNature.setBooleanValue(isTrue(DEF_JJ_NATURE));
    jSuppressWarnings.setBooleanValue(isTrue(DEF_SUPPRESS_WARNINGS));
    jMarkGenFilesAsDerived.setBooleanValue(isTrue(DEF_MARK_GEN_FILES_AS_DERIVED));
    //    fCheckSpelling.setBooleanValue(true);
  }

  /**
   * @param str "true" or "false
   * @return true or false
   */
  static boolean isTrue(final String str) {
    return "true".equals(str); //$NON-NLS-1$
  }

  /**
   * Save settings in Properties (called by JJPropertyPage).
   * 
   * @return true if successful, false otherwise
   */
  public boolean performOk() {
    if (jResource != null) {
      final IProject project = jResource.getProject();
      final IScopeContext projectScope = new ProjectScope(project);
      final IEclipsePreferences prefs = projectScope.getNode(IJJConstants.ID);

      prefs.put(RUNTIME_JJJAR, jJavaCCjarFile.getText());
      prefs.put(RUNTIME_JTBJAR, jJTBJarFile.getText());
      prefs.put(JJ_NATURE, jJJNature.getBooleanValue() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
      prefs.put(CLEAR_CONSOLE, jClearConsole.getBooleanValue() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
      prefs.put(SUPPRESS_WARNINGS, jSuppressWarnings.getBooleanValue() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
      prefs.put(MARK_GEN_FILES_AS_DERIVED, jMarkGenFilesAsDerived.getBooleanValue() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$

      // set the nature 
      JJNature.setJJNature(jJJNature.getBooleanValue(), project);

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