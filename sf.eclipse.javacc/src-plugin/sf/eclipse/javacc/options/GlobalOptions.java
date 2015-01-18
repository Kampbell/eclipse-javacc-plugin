package sf.eclipse.javacc.options;

import static sf.eclipse.javacc.base.IConstants.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
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

import sf.eclipse.javacc.base.AbstractActivator;
import sf.eclipse.javacc.base.Compiler;
import sf.eclipse.javacc.base.Nature;

/**
 * The Tab for the JavaCC global options.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 */
class GlobalOptions extends Composite {

  // MMa 02/2010 : formatting and javadoc revision
  // MMa 04/2009 : formatting revision ; changed jar names
  // MMa 08/2011 : added mark generated files as derived option RFE 3314103
  // MMa 08/2011 : renamed
  // MMa 10/2012 : added JVM options option and keep deleted files in history option
  // MMa 01/2015 : fixed jars directory ; added default jars fields

  /** The JavaCC jar file */
  protected Text               jJavaCCjarFile;
  /** The JTB jar file */
  protected Text               jJTBJarFile;
  /** The default JTB jar file */
  protected String             jDefJTBJarFile;
  /** The JVM options */
  protected Text               jJvmOptions;
  /** The clear console flag */
  protected BooleanFieldEditor jClearConsole;
  /** The add Nature flag */
  protected BooleanFieldEditor jNature;
  /** The suppress warnings flag */
  protected BooleanFieldEditor jSuppressWarnings;
  /** The mark generated files as derived flag */
  protected BooleanFieldEditor jMarkGenFilesAsDerived;
  /** The keep deleted files in local history flag */
  protected BooleanFieldEditor jKeepDelFilesInHistory;
  /** The Resource to work on */
  protected IResource          jResource;

  /**
   * Constructor for OptGlob.
   * 
   * @param aParent - the parent
   * @param aResource - the resource
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
    groupProject.setText(AbstractActivator.getMsg("OptGlob.Common_options_Group")); //$NON-NLS-1$
    groupProject.setLayout(layout);
    groupProject.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

    // add first sub group
    final Group subGroup1 = new Group(groupProject, SWT.NONE);
    subGroup1.setText(AbstractActivator.getMsg("OptGlob.Sel_jar_files_jvm_options")); //$NON-NLS-1$
    subGroup1.setLayout(layout);
    subGroup1.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

    // add a composite group for selection controls
    final Composite compSubGroup = new Composite(subGroup1, SWT.NONE);
    compSubGroup.setLayout(new GridLayout(4, false));

    // add javacc jar selection control
    // add File Field Editor (no more FileFieldEditor)
    // code inspired by org.eclipse.jdt.internal.ui.preferences.OptionsConfigurationBlock
    new Label(compSubGroup, SWT.LEFT | SWT.HORIZONTAL).setText(AbstractActivator.getMsg("OptGlob.Set_the_JavaCC_jar_file")); //$NON-NLS-1$
    jJavaCCjarFile = new Text(compSubGroup, SWT.BORDER | SWT.SINGLE);
    GridData gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    // Eclipse 3.5
    // import org.eclipse.jface.layout.PixelConverter
    // PixelConverter converter= new PixelConverter(aParent);
    // gd.widthHint = converter.convertWidthInCharsToPixels(50);
    // Eclipse 3.4
    gd.widthHint = 500;
    jJavaCCjarFile.setLayoutData(gd);
    // add "Browse..." button
    Button browse = new Button(compSubGroup, SWT.PUSH);
    browse.setText(AbstractActivator.getMsg(AbstractActivator.getMsg("OptGlob.Browse"))); //$NON-NLS-1$
    browse.addSelectionListener(new SelectionAdapter() {

      /** {@inheritDoc} */
      @Override
      public void widgetSelected(final SelectionEvent event) {
        final FileDialog dialog = new FileDialog(jJavaCCjarFile.getShell(), SWT.OPEN);
        dialog.setText(AbstractActivator.getMsg("OptGlob.Choose_file")); //$NON-NLS-1$
        dialog.setFilterPath(jJavaCCjarFile.getText());
        final String path = dialog.open();
        if (path != null) {
          jJavaCCjarFile.setText(path);
        }
      }
    });
    // add "Variables..." button
    Button variables = new Button(compSubGroup, SWT.PUSH);
    variables.setText(AbstractActivator.getMsg(AbstractActivator.getMsg("OptGlob.Variables"))); //$NON-NLS-1$
    variables.addSelectionListener(new SelectionAdapter() {

      /** {@inheritDoc} */
      @Override
      public void widgetSelected(final SelectionEvent event) {
        final StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(
                                                                                       jJavaCCjarFile.getShell());
        if (dialog.open() == Window.OK) {
          jJavaCCjarFile.insert(dialog.getVariableExpression());
        }
      }
    });

    // add plugin's default javacc jar display control
    new Label(compSubGroup, SWT.LEFT | SWT.HORIZONTAL).setText(AbstractActivator.getMsg("OptGlob.Default_JavaCC_jar_file")); //$NON-NLS-1$
    final String defJavaCCjarFile = Compiler.getDefaultJarFile("jj"); //$NON-NLS-1$
    new Label(compSubGroup, SWT.LEFT | SWT.HORIZONTAL).setText("  " + AbstractActivator.getMsg(defJavaCCjarFile)); //$NON-NLS-1$
    // TODO find a better way to fill the 2 last slots
    new Label(compSubGroup, SWT.LEFT | SWT.HORIZONTAL).setText(""); //$NON-NLS-1$
    new Label(compSubGroup, SWT.LEFT | SWT.HORIZONTAL).setText(""); //$NON-NLS-1$

    // add jtb jar selection control
    new Label(compSubGroup, SWT.LEFT | SWT.HORIZONTAL).setText(AbstractActivator.getMsg("OptGlob.Set_the_Jtb_jar_file")); //$NON-NLS-1$
    jJTBJarFile = new Text(compSubGroup, SWT.BORDER | SWT.SINGLE);
    gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    // Eclipse 3.5
    // import org.eclipse.jface.layout.PixelConverter
    // PixelConverter converter= new PixelConverter(aParent);
    // gd.widthHint = converter.convertWidthInCharsToPixels(50);
    // Eclipse 3.4
    gd.widthHint = 500;

    jJTBJarFile.setLayoutData(gd);
    // add "Browse..." button
    browse = new Button(compSubGroup, SWT.PUSH);
    browse.setText(AbstractActivator.getMsg(AbstractActivator.getMsg("OptGlob.Browse"))); //$NON-NLS-1$
    browse.addSelectionListener(new SelectionAdapter() {

      /** {@inheritDoc} */
      @Override
      public void widgetSelected(final SelectionEvent event) {
        final FileDialog dialog = new FileDialog(jJavaCCjarFile.getShell(), SWT.OPEN);
        dialog.setText(AbstractActivator.getMsg("OptGlob.Choose_file")); //$NON-NLS-1$
        dialog.setFilterPath(jJTBJarFile.getText());
        final String path = dialog.open();
        if (path != null) {
          jJTBJarFile.setText(path);
        }
      }
    });
    // add "Variables..." button
    variables = new Button(compSubGroup, SWT.PUSH);
    variables.setText(AbstractActivator.getMsg(AbstractActivator.getMsg("OptGlob.Variables"))); //$NON-NLS-1$
    variables.addSelectionListener(new SelectionAdapter() {

      /** {@inheritDoc} */
      @Override
      public void widgetSelected(final SelectionEvent event) {
        final StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(jJTBJarFile.getShell());
        if (dialog.open() == Window.OK) {
          jJTBJarFile.insert(dialog.getVariableExpression());
        }
      }
    });

    // add plugin's default jtb jar display control
    new Label(compSubGroup, SWT.LEFT | SWT.HORIZONTAL).setText(AbstractActivator.getMsg("OptGlob.Default_JTB_jar_file")); //$NON-NLS-1$
    final String defJtbCCjarFile = Compiler.getDefaultJarFile("jtb"); //$NON-NLS-1$
    new Label(compSubGroup, SWT.LEFT | SWT.HORIZONTAL).setText("  " + AbstractActivator.getMsg(defJtbCCjarFile)); //$NON-NLS-1$
    // TODO find a better way to fill the 2 last slots
    new Label(compSubGroup, SWT.LEFT | SWT.HORIZONTAL).setText(""); //$NON-NLS-1$
    new Label(compSubGroup, SWT.LEFT | SWT.HORIZONTAL).setText(""); //$NON-NLS-1$

    // add jvm options selection control
    new Label(compSubGroup, SWT.LEFT | SWT.HORIZONTAL).setText(AbstractActivator.getMsg("OptGlob.Set_the_Jvm_Options")); //$NON-NLS-1$
    jJvmOptions = new Text(compSubGroup, SWT.BORDER | SWT.SINGLE);
    gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    // Eclipse 3.5
    // import org.eclipse.jface.layout.PixelConverter
    // PixelConverter converter= new PixelConverter(aParent);
    // gd.widthHint = converter.convertWidthInCharsToPixels(50);
    // Eclipse 3.4
    gd.widthHint = 500;

    jJvmOptions.setLayoutData(gd);
    // add "Variables..." button
    variables = new Button(compSubGroup, SWT.PUSH);
    variables.setText(AbstractActivator.getMsg(AbstractActivator.getMsg("OptGlob.Variables"))); //$NON-NLS-1$
    variables.addSelectionListener(new SelectionAdapter() {

      /** {@inheritDoc} */
      @Override
      public void widgetSelected(final SelectionEvent event) {
        final StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(jJvmOptions.getShell());
        if (dialog.open() == Window.OK) {
          jJvmOptions.insert(dialog.getVariableExpression());
        }
      }
    });

    // add second sub group
    final Group subGroup2 = new Group(groupProject, SWT.NONE);
    subGroup2.setText(AbstractActivator.getMsg("OptGlob.Miscellaneous")); //$NON-NLS-1$
    subGroup2.setLayout(layout);
    subGroup2.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

    // add Checkboxes for boolean values
    final Composite checkGroup = new Composite(subGroup2, SWT.NONE);
    checkGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    final String def = " (".concat(AbstractActivator.getMsg(AbstractActivator.getMsg("OptAbsTab.default"))) //$NON-NLS-1$  //$NON-NLS-2$
                           .concat(" "); //$NON-NLS-1$
    String str;
    str = AbstractActivator.getMsg(AbstractActivator.getMsg("OptGlob.Clear_console_before_build")).concat(def) //$NON-NLS-1$
                           .concat(DEF_CLEAR_CONSOLE).concat(")"); //$NON-NLS-1$
    jClearConsole = new BooleanFieldEditor(CLEAR_CONSOLE, str, checkGroup);

    str = AbstractActivator.getMsg(AbstractActivator.getMsg("OptGlob.Build_automatically_on_save")).concat(def) //$NON-NLS-1$
                           .concat(DEF_NATURE).concat(")"); //$NON-NLS-1$
    jNature = new BooleanFieldEditor(NATURE_NAME, str, checkGroup);

    str = AbstractActivator.getMsg(AbstractActivator.getMsg("OptGlob.Automat_suppress_warnings")).concat(def) //$NON-NLS-1$
                           .concat(DEF_SUPPRESS_WARNINGS).concat(")"); //$NON-NLS-1$
    jSuppressWarnings = new BooleanFieldEditor(SUPPRESS_WARNINGS, str, checkGroup);

    str = AbstractActivator.getMsg(AbstractActivator.getMsg("OptGlob.Mark_gen_files_as_derived")).concat(def) //$NON-NLS-1$
                           .concat(DEF_MARK_GEN_FILES_DERIVED).concat(")"); //$NON-NLS-1$
    jMarkGenFilesAsDerived = new BooleanFieldEditor(MARK_GEN_FILES_DERIVED, str, checkGroup);

    str = AbstractActivator.getMsg(AbstractActivator.getMsg("OptGlob.Keep_del_files_in_history")).concat(def) //$NON-NLS-1$
                           .concat(DEF_KEEP_DEL_FILES_IN_HIST).concat(")"); //$NON-NLS-1$
    jKeepDelFilesInHistory = new BooleanFieldEditor(KEEP_DEL_FILES_IN_HIST, str, checkGroup);

    // read and set values
    if (aResource != null) {
      final IEclipsePreferences prefs = new ProjectScope(aResource.getProject()).getNode(PLUGIN_QN);
      try {
        // set according to PersistentProperties
        jJavaCCjarFile.setText(prefs.get(RUNTIME_JJJAR, "")); //$NON-NLS-1$
        jJTBJarFile.setText(prefs.get(RUNTIME_JTBJAR, "")); //$NON-NLS-1$
        jJvmOptions.setText(prefs.get(RUNTIME_JVMOPTIONS, "")); //$NON-NLS-1$
        jClearConsole.setBooleanValue(isTrue(prefs.get(CLEAR_CONSOLE, DEF_CLEAR_CONSOLE)));
        final boolean hasJavaccNature = aResource.getProject().getDescription().hasNature(NATURE_ID);
        jNature.setBooleanValue(hasJavaccNature);
        jSuppressWarnings.setBooleanValue(isTrue(prefs.get(SUPPRESS_WARNINGS, DEF_SUPPRESS_WARNINGS)));
        jMarkGenFilesAsDerived.setBooleanValue(isTrue(prefs.get(MARK_GEN_FILES_DERIVED,
                                                                DEF_MARK_GEN_FILES_DERIVED)));
        jKeepDelFilesInHistory.setBooleanValue(isTrue(prefs.get(KEEP_DEL_FILES_IN_HIST,
                                                                DEF_KEEP_DEL_FILES_IN_HIST)));
      } catch (final CoreException e) {
        AbstractActivator.logBug(e);
      }
    }
  }

  /**
   * Sets the defaults.
   */
  public void performDefaults() {
    jJavaCCjarFile.setText(""); //$NON-NLS-1$
    // keep empty for using the plugin's default jars
    jJTBJarFile.setText(""); //$NON-NLS-1$
    jJvmOptions.setText(""); //$NON-NLS-1$
    jClearConsole.setBooleanValue(isTrue(DEF_CLEAR_CONSOLE));
    jNature.setBooleanValue(isTrue(DEF_NATURE));
    jSuppressWarnings.setBooleanValue(isTrue(DEF_SUPPRESS_WARNINGS));
    jMarkGenFilesAsDerived.setBooleanValue(isTrue(DEF_MARK_GEN_FILES_DERIVED));
    jKeepDelFilesInHistory.setBooleanValue(isTrue(DEF_KEEP_DEL_FILES_IN_HIST));
    //    fCheckSpelling.setBooleanValue(true);
  }

  /**
   * @param str - "true" or "false
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
      final IEclipsePreferences prefs = new ProjectScope(jResource.getProject()).getNode(PLUGIN_QN);

      prefs.put(RUNTIME_JJJAR, jJavaCCjarFile.getText());
      prefs.put(RUNTIME_JTBJAR, jJTBJarFile.getText());
      prefs.put(RUNTIME_JVMOPTIONS, jJvmOptions.getText());
      prefs.put(NATURE, String.valueOf(jNature.getBooleanValue()));
      prefs.put(CLEAR_CONSOLE, String.valueOf(jClearConsole.getBooleanValue()));
      prefs.put(SUPPRESS_WARNINGS, String.valueOf(jSuppressWarnings.getBooleanValue()));
      prefs.put(MARK_GEN_FILES_DERIVED, String.valueOf(jMarkGenFilesAsDerived.getBooleanValue()));
      prefs.put(KEEP_DEL_FILES_IN_HIST, String.valueOf(jKeepDelFilesInHistory.getBooleanValue()));

      // set the nature
      Nature.setNature(jNature.getBooleanValue(), jResource.getProject());

      try {
        prefs.flush();
      } catch (final BackingStoreException e) {
        AbstractActivator.logBug(e);
        return false;
      }
    }
    return true;
  }
}