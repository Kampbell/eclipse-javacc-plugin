package sf.eclipse.javacc.options;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.widgets.Composite;
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
public class JJPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

  // MMa 02/2010 : formatting and javadoc revision
  // MMa : added some colors and indentation preferences

  /**
   * Standard constructor
   */
  public JJPreferencesPage() {
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
    /*
     * Checkboxes and integer field for indentation
     */
    addField(new BooleanFieldEditor(
                                    JJPreferences.P_NO_ADV_AUTO_INDENT,
                                    Activator.getString("JJPreferencesPage.No_Adv_Auto_Indent"), BooleanFieldEditor.SEPARATE_LABEL, parent)); //$NON-NLS-1$
    addField(new BooleanFieldEditor(
                                    JJPreferences.P_INDENT_CHAR,
                                    Activator.getString("JJPreferencesPage.Indent_char"), BooleanFieldEditor.SEPARATE_LABEL, parent)); //$NON-NLS-1$
    final IntegerFieldEditor ife = new IntegerFieldEditor(
                                                          JJPreferences.P_INDENT_CHAR_NB,
                                                          Activator
                                                                   .getString("JJPreferencesPage.Indent_chars_number"), parent, 1); //$NON-NLS-1$
    ife.setValidRange(1, 4);
    addField(ife);
    /*
     * Colors
     */
    addField(new ColorFieldEditor(JJPreferences.P_JJKEYWORD,
                                  Activator.getString("JJPreferencesPage.JavaCC_Keyword"), parent)); //$NON-NLS-1$
    addField(new ColorFieldEditor(JJPreferences.P_JAVAKEYWORD,
                                  Activator.getString("JJPreferencesPage.Java_Keyword"), parent)); //$NON-NLS-1$
    // addField(new ColorFieldEditor(JJPreferences.P_BACKGROUND, Activator.getString("JJPreferencesPage.Background"), parent)); //$NON-NLS-1$
    addField(new ColorFieldEditor(JJPreferences.P_STRING,
                                  Activator.getString("JJPreferencesPage.Strings"), parent)); //$NON-NLS-1$
    addField(new ColorFieldEditor(JJPreferences.P_COMMENT,
                                  Activator.getString("JJPreferencesPage.Comments"), parent)); //$NON-NLS-1$
    addField(new ColorFieldEditor(JJPreferences.P_JDOC_COMMENT,
                                  Activator.getString("JJPreferencesPage.Javadoc_comments"), parent)); //$NON-NLS-1$
    addField(new ColorFieldEditor(JJPreferences.P_NORMALLABEL,
                                  Activator.getString("JJPreferencesPage.Token_declaration"), parent)); //$NON-NLS-1$
    addField(new ColorFieldEditor(JJPreferences.P_PRIVATELABEL,
                                  Activator.getString("JJPreferencesPage.Private_token_declaration"), parent)); //$NON-NLS-1$
    addField(new ColorFieldEditor(JJPreferences.P_LEXICALSTATE,
                                  Activator.getString("JJPreferencesPage.Lexical_state_declaration"), parent)); //$NON-NLS-1$
    addField(new ColorFieldEditor(JJPreferences.P_REGEXPUNCT,
                                  Activator.getString("JJPreferencesPage.RegExPunct_declaration"), parent)); //$NON-NLS-1$
    addField(new ColorFieldEditor(JJPreferences.P_CHOICESPUNCT,
                                  Activator.getString("JJPreferencesPage.ChoicesPunct_declaration"), parent)); //$NON-NLS-1$
    addField(new ColorFieldEditor(JJPreferences.P_DEFAULT,
                                  Activator.getString("JJPreferencesPage.Text_by_default"), parent)); //$NON-NLS-1$
    addField(new ColorFieldEditor(JJPreferences.P_MATCHING_CHAR,
                                  Activator.getString("JJPreferencesPage.Matching_char"), parent)); //$NON-NLS-1$
    addField(new ColorFieldEditor(JJPreferences.P_CONSOLE_COMMAND,
                                  Activator.getString("JJPreferencesPage.Console_commands"), parent)); //$NON-NLS-1$
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
}