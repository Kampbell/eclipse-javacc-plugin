package sf.eclipse.javacc.options;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchWindow;

import sf.eclipse.javacc.Activator;
import sf.eclipse.javacc.editors.JJEditor;

/**
 * The Preferences page class for JavaCC.<br>
 * Referenced by plugin.xml<br>
 * <extension point="org.eclipse.ui.preferencePages">
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class JJPreferencesPageNew extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

  // MMa : added some colors and indentation preferences
  // MMa 02/2010 : formatting and javadoc revision

  /**
   * Standard constructor
   */
  public JJPreferencesPageNew() {
    super(GRID);
    setPreferenceStore(Activator.getDefault().getPreferenceStore());
    setDescription(Activator.getString("JJPreferencesPage.Prefs_for_javacc_plugin")); //$NON-NLS-1$
  }

  /**
   * Creates the field editors. Each field editor knows how to save and restore itself.
   */
  @Override
  public void createFieldEditors() {
    final Composite parent = getFieldEditorParent();

    final Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayoutData(new GridData(GridData.FILL_BOTH));
    composite.setLayout(new GridLayout());

    /*
     * Checkboxes and integer field for indentation
     */
    final Group gpIndent = new Group(composite, SWT.NONE);
    gpIndent.setText(Activator.getString("JJPreferencesPage.Indent_for_javacc_plugin"));
    gpIndent.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
    gpIndent.setLayout(new GridLayout());

    final Composite coIndent = new Composite(gpIndent, SWT.NONE);
    coIndent.setLayoutData(new GridData());
    coIndent.setLayout(new GridLayout());

    addField(new BooleanFieldEditor(
                                    JJPreferences.P_NO_ADV_AUTO_INDENT,
                                    Activator.getString("JJPreferencesPage.No_Adv_Auto_Indent"), BooleanFieldEditor.DEFAULT, coIndent)); //$NON-NLS-1$
    addField(new BooleanFieldEditor(
                                    JJPreferences.P_INDENT_CHAR,
                                    Activator.getString("JJPreferencesPage.Indent_char"), BooleanFieldEditor.DEFAULT, coIndent)); //$NON-NLS-1$
    final IntegerFieldEditor ife = new IntegerFieldEditor(
                                                          JJPreferences.P_INDENT_CHAR_NB,
                                                          Activator
                                                                   .getString("JJPreferencesPage.Indent_chars_number"), coIndent, 1); //$NON-NLS-1$
    ife.setValidRange(1, 8);
    addField(ife);

    /*
     * Colors
     */
    final Group gpColors = new Group(composite, SWT.NONE);
    gpColors.setText(Activator.getString("JJPreferencesPage.Colors_for_javacc_plugin"));
    gpColors.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
    final GridLayout ly = new GridLayout(2, true);
    gpColors.setLayout(ly);

    final Composite coColorsLeft = new Composite(gpColors, SWT.NONE);
    coColorsLeft.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
    final GridLayout lyLeft = new GridLayout();
    coColorsLeft.setLayout(lyLeft);

    final Composite coColorsRight = new Composite(gpColors, SWT.NONE);
    coColorsRight.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
    final GridLayout lyRight = new GridLayout();
    coColorsRight.setLayout(lyRight);

    addField(new ColorFieldEditor(JJPreferences.P_JJKEYWORD,
                                  Activator.getString("JJPreferencesPage.JavaCC_Keyword"), coColorsLeft)); //$NON-NLS-1$
    addField(new ColorFieldEditor(JJPreferences.P_JAVAKEYWORD,
                                  Activator.getString("JJPreferencesPage.Java_Keyword"), coColorsLeft)); //$NON-NLS-1$
    // addField(new ColorFieldEditor(JJPreferences.P_BACKGROUND, Activator.getString("JJPreferencesPage.Background"), coColors)); //$NON-NLS-1$
    addField(new ColorFieldEditor(JJPreferences.P_STRING,
                                  Activator.getString("JJPreferencesPage.Strings"), coColorsLeft)); //$NON-NLS-1$
    addField(new ColorFieldEditor(JJPreferences.P_COMMENT,
                                  Activator.getString("JJPreferencesPage.Comments"), coColorsLeft)); //$NON-NLS-1$
    addField(new ColorFieldEditor(JJPreferences.P_JDOC_COMMENT,
                                  Activator.getString("JJPreferencesPage.Javadoc_comments"), coColorsLeft)); //$NON-NLS-1$
    addField(new ColorFieldEditor(JJPreferences.P_NORMALLABEL,
                                  Activator.getString("JJPreferencesPage.Token_declaration"), coColorsLeft)); //$NON-NLS-1$
    addField(new ColorFieldEditor(
                                  JJPreferences.P_PRIVATELABEL,
                                  Activator.getString("JJPreferencesPage.Private_token_declaration"), coColorsLeft)); //$NON-NLS-1$
    addField(new ColorFieldEditor(
                                  JJPreferences.P_LEXICALSTATE,
                                  Activator.getString("JJPreferencesPage.Lexical_state_declaration"), coColorsRight)); //$NON-NLS-1$
    addField(new ColorFieldEditor(
                                  JJPreferences.P_REGEXPUNCT,
                                  Activator.getString("JJPreferencesPage.RegExPunct_declaration"), coColorsRight)); //$NON-NLS-1$
    addField(new ColorFieldEditor(
                                  JJPreferences.P_CHOICESPUNCT,
                                  Activator.getString("JJPreferencesPage.ChoicesPunct_declaration"), coColorsRight)); //$NON-NLS-1$
    addField(new ColorFieldEditor(JJPreferences.P_DEFAULT,
                                  Activator.getString("JJPreferencesPage.Text_by_default"), coColorsRight)); //$NON-NLS-1$
    addField(new ColorFieldEditor(JJPreferences.P_MATCHING_CHAR,
                                  Activator.getString("JJPreferencesPage.Matching_char"), coColorsRight)); //$NON-NLS-1$
    addField(new ColorFieldEditor(JJPreferences.P_CONSOLE_COMMAND,
                                  Activator.getString("JJPreferencesPage.Console_commands"), coColorsRight)); //$NON-NLS-1$
  }

  /**
   * Updates colors on Apply action.
   */
  @Override
  protected void performApply() {
    updateColors();
    super.performApply();
  }

  /**
   * Updates colors on OK action.
   * 
   * @return always true
   */
  @Override
  public boolean performOk() {
    updateColors();
    return super.performOk();
  }

  /**
   * Updates all colors.
   */
  protected void updateColors() {
    final IWorkbenchWindow window = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
    if (window != null) {
      final IWorkbenchPage page = window.getActivePage();
      if (page != null) {
        final IEditorReference[] editorReference = page.getEditorReferences();
        for (int i = 0; i < editorReference.length; i++) {
          final IEditorPart editorPart = editorReference[i].getEditor(false);
          if (editorPart instanceof JJEditor) {
            final JJEditor editor = (JJEditor) editorPart;
            editor.updateColors();
          }
        }
      }
    }
  }

  /**
   * Does nothing.
   * 
   * @see IWorkbenchPreferencePage
   * @param workbench the current workbench
   */
  public void init(@SuppressWarnings("unused") final IWorkbench workbench) {
    // nothing done here
  }
  //public class JJPreferencesPageNew extends PreferencePage implements IWorkbenchPreferencePage {
  //
  //  // MMa : added some colors and indentation preferences
  //  // MMa 02/2010 : formatting and javadoc revision ; splitted into tabs
  //
  //  /** The current folder */
  //  protected TabFolder              fFolder;
  //  /** The current format item */
  //  protected TabItem                fFormatItem;
  //  /** The current colors item */
  //  protected TabItem                fColorsItem;
  //  /** The current format options */
  //  protected JJFormatOptions        fFormatOptions;
  //  /** The current colors options */
  //  protected JJColorsOptions        fColorsOptions;
  //  /** The current format preferences */
  //  protected JJFormatPreferencePage fFormatPP;
  //  /** The current colors preferences */
  //  protected JJColorsPreferencePage fColorsPP;
  //
  //  /**
  //   * Standard constructor
  //   */
  //  public JJPreferencesPageNew() {
  //    setPreferenceStore(Activator.getDefault().getPreferenceStore());
  //    setDescription(Activator.getString("JJPreferencesPage.Prefs_for_javacc_plugin")); //$NON-NLS-1$
  //  }
  //
  //  /**
  //   * @see PreferencePage#createControl(Composite)
  //   */
  //  @Override
  //  public void createControl(final Composite parent) {
  //    super.createControl(parent);
  //    PlatformUI
  //              .getWorkbench()
  //              .getHelpSystem()
  //              .setHelp(getControl(), Activator.getString("JJPreferencesPage.Prefs_for_javacc_plugin") + "New");
  //  }
  //
  //  /**
  //   * @see PreferencePage#createContents(Composite)
  //   */
  //  @Override
  //  protected Control createContents(final Composite parent) {
  //    // create a TabFolder
  //    fFolder = new TabFolder(parent, SWT.NONE);
  //    final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
  //    fFolder.setLayoutData(gd);
  //    addFormatTab();
  //    addColorsTab();
  //    return parent;
  //  }
  //
  //  /*
  //   * Convenient methods
  //   */
  //  /**
  //   * Adds the format preference tab.
  //   */
  //  protected void addFormatTab() {
  //    fFormatOptions = new JJFormatOptions(fFolder);
  //    fFormatItem = new TabItem(fFolder, SWT.NONE);
  //    fFormatItem.setText(Activator.getString("JJPreferencesPage.Ind_fmt_for_javacc_plugin")); //$NON-NLS-1$
  //    fFormatItem.setControl(fFormatOptions);
  //  }
  //
  //  /**
  //   * Adds the colors preference tab.
  //   */
  //  protected void addColorsTab() {
  //    fColorsOptions = new JJColorsOptions(fFolder);
  //    fColorsItem = new TabItem(fFolder, SWT.NONE);
  //    fColorsItem.setText(Activator.getString("JJPreferencesPage.Colors_for_javacc_plugin")); //$NON-NLS-1$
  //    fColorsItem.setControl(fColorsOptions);
  //  }
  //
  //  /**
  //   * Updates colors on Apply action.
  //   */
  //  @Override
  //  protected void performApply() {
  //    fColorsOptions.updateColors();
  //    super.performApply();
  //  }
  //
  //  /**
  //   * Updates colors on OK action.
  //   * 
  //   * @return always true
  //   */
  //  @Override
  //  public boolean performOk() {
  //    fColorsOptions.updateColors();
  //    return super.performOk();
  //  }
  //
  //  /**
  //   * Does nothing.
  //   * 
  //   * @see IWorkbenchPreferencePage
  //   * @param workbench the current workbench
  //   */
  //  public void init(@SuppressWarnings("unused") final IWorkbench workbench) {
  //    // nothing done here
  //  }
}
