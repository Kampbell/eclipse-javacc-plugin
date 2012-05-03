package sf.eclipse.javacc.options;

import static sf.eclipse.javacc.options.PreferencesInitializer.*;

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

import sf.eclipse.javacc.editors.JJEditor;
import sf.eclipse.javacc.head.Activator;

/**
 * The Preferences page class for the JavaCC Plugin Preferences.<br>
 * Referenced by plugin.xml<br>
 * <extension point="org.eclipse.ui.preferencePages">
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011
 */
public class PreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

  // MMa : added some colors and indentation preferences
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 03/2010 : added groups and check spelling option
  // MMa 08/2011 : renamed

  /**
   * Standard constructor
   */
  public PreferencesPage() {
    super(GRID);
    setPreferenceStore(Activator.getDefault().getPreferenceStore());
    setDescription(Activator.getString("PreferencesPage.Prefs_for_javacc_plugin")); //$NON-NLS-1$
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
     * Checkbox for spelling
     */
    final Group gpSpell = new Group(composite, SWT.NONE);
    gpSpell.setText(Activator.getString(Activator.getString("PreferencesPage.Spell_Group"))); //$NON-NLS-1$
    gpSpell.setToolTipText(Activator.getString(Activator.getString("PreferencesPage.Spell_Group_TT"))); //$NON-NLS-1$
    gpSpell.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
    gpSpell.setLayout(new GridLayout());

    final Composite coSpell = new Composite(gpSpell, SWT.NONE);
    coSpell.setLayoutData(new GridData());
    coSpell.setLayout(new GridLayout());

    addField(new BooleanFieldEditor(
                                    P_NO_SPELL_CHECKING,
                                    Activator.getString("PreferencesPage.Spell_check_disable"), BooleanFieldEditor.DEFAULT, coSpell)); //$NON-NLS-1$

    /*
     * Checkboxes and integer field for indentation
     */
    final Group gpIndent = new Group(composite, SWT.NONE);
    gpIndent.setText(Activator.getString(Activator.getString("PreferencesPage.Indent_Group"))); //$NON-NLS-1$
    gpIndent.setToolTipText(Activator.getString(Activator.getString("PreferencesPage.Indent_Group_TT"))); //$NON-NLS-1$
    gpIndent.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
    gpIndent.setLayout(new GridLayout());

    final Composite coIndent = new Composite(gpIndent, SWT.NONE);
    coIndent.setLayoutData(new GridData());
    coIndent.setLayout(new GridLayout());

    addField(new BooleanFieldEditor(
                                    P_NO_ADV_AUTO_INDENT,
                                    Activator.getString("PreferencesPage.No_Adv_Auto_Indent"), BooleanFieldEditor.DEFAULT, coIndent)); //$NON-NLS-1$
    addField(new BooleanFieldEditor(
                                    P_INDENT_CHAR,
                                    Activator.getString("PreferencesPage.Indent_char"), BooleanFieldEditor.DEFAULT, coIndent)); //$NON-NLS-1$
    final IntegerFieldEditor ife = new IntegerFieldEditor(
                                                          P_INDENT_CHAR_NB,
                                                          Activator.getString("PreferencesPage.Indent_chars_number"), coIndent, 1); //$NON-NLS-1$
    ife.setValidRange(1, 8);
    addField(ife);

    /*
     * Colors
     */
    final Group gpColors = new Group(composite, SWT.NONE);
    gpColors.setText(Activator.getString(Activator.getString("PreferencesPage.Colors_Group"))); //$NON-NLS-1$
    gpColors.setToolTipText(Activator.getString(Activator.getString("PreferencesPage.Colors_Group_TT"))); //$NON-NLS-1$
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

    addField(new ColorFieldEditor(P_JJKEYWORD,
                                  Activator.getString("PreferencesPage.JavaCC_Keyword"), coColorsLeft)); //$NON-NLS-1$
    addField(new ColorFieldEditor(P_JAVAKEYWORD,
                                  Activator.getString("PreferencesPage.Java_Keyword"), coColorsLeft)); //$NON-NLS-1$
    // addField(new ColorFieldEditor(JJPreferences.P_BACKGROUND, Activator.getString("PreferencesPage.Background"), coColors)); //$NON-NLS-1$
    addField(new ColorFieldEditor(P_STRING, Activator.getString("PreferencesPage.Strings"), coColorsLeft)); //$NON-NLS-1$
    addField(new ColorFieldEditor(P_COMMENT, Activator.getString("PreferencesPage.Comments"), coColorsLeft)); //$NON-NLS-1$
    addField(new ColorFieldEditor(P_JDOC_COMMENT,
                                  Activator.getString("PreferencesPage.Javadoc_comments"), coColorsLeft)); //$NON-NLS-1$
    addField(new ColorFieldEditor(P_NORMALLABEL,
                                  Activator.getString("PreferencesPage.Token_declaration"), coColorsLeft)); //$NON-NLS-1$
    addField(new ColorFieldEditor(
                                  P_PRIVATELABEL,
                                  Activator.getString("PreferencesPage.Private_token_declaration"), coColorsLeft)); //$NON-NLS-1$
    addField(new ColorFieldEditor(
                                  P_LEXICALSTATE,
                                  Activator.getString("PreferencesPage.Lexical_state_declaration"), coColorsRight)); //$NON-NLS-1$
    addField(new ColorFieldEditor(
                                  P_REGEXPUNCT,
                                  Activator.getString("PreferencesPage.RegExPunct_declaration"), coColorsRight)); //$NON-NLS-1$
    addField(new ColorFieldEditor(
                                  P_CHOICESPUNCT,
                                  Activator.getString("PreferencesPage.ChoicesPunct_declaration"), coColorsRight)); //$NON-NLS-1$
    addField(new ColorFieldEditor(P_DEFAULT,
                                  Activator.getString("PreferencesPage.Text_by_default"), coColorsRight)); //$NON-NLS-1$
    addField(new ColorFieldEditor(P_MATCHING_CHAR,
                                  Activator.getString("PreferencesPage.Matching_char"), coColorsRight)); //$NON-NLS-1$
    addField(new ColorFieldEditor(P_CONSOLE_COMMAND,
                                  Activator.getString("PreferencesPage.Console_commands"), coColorsRight)); //$NON-NLS-1$
  }

  /**
   * Updates spelling and colors on Apply action.
   */
  @Override
  protected void performApply() {
    updateSpellingAndColors();
    super.performApply();
  }

  /**
   * Updates spelling and colors on OK action.
   * 
   * @return always true
   */
  @Override
  public boolean performOk() {
    updateSpellingAndColors();
    return super.performOk();
  }

  /**
   * Updates spelling and colors.
   */
  protected void updateSpellingAndColors() {
    final IWorkbenchWindow window = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
    if (window != null) {
      final IWorkbenchPage page = window.getActivePage();
      if (page != null) {
        final IEditorReference[] editorReference = page.getEditorReferences();
        for (int i = 0; i < editorReference.length; i++) {
          final IEditorPart editorPart = editorReference[i].getEditor(false);
          if (editorPart instanceof JJEditor) {
            final JJEditor editor = (JJEditor) editorPart;
            editor.updateSpellingAndColors();
          }
        }
      }
    }
  }

  /**
   * Does nothing.
   * 
   * @see IWorkbenchPreferencePage
   * @param aWorkbench the current workbench
   */
  @Override
  public void init(@SuppressWarnings("unused") final IWorkbench aWorkbench) {
    // nothing done here
  }
}