package sf.eclipse.javacc.options;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchWindow;

import sf.eclipse.javacc.Activator;
import sf.eclipse.javacc.editors.JJEditor;

/**
 * The Preferences page class for JavaCC 
 * Referenced by plugin.xml
 *  <extension point="org.eclipse.ui.preferencePages">
 *  
 * @author Remi Koutcherawy 2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */

public class JJPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

  public JJPreferencesPage() {
    super(GRID);
    setPreferenceStore(Activator.getDefault().getPreferenceStore());
    setDescription(Activator.getString("JJPreferencesPage.Colors_for_javacc_plugin")); //$NON-NLS-1$
  }
  
  /**
   * Creates the field editors. 
   * Each field editor knows how to save and restore itself.
   */
  public void createFieldEditors() {
    // Colors
    addField(new ColorFieldEditor(JJPreferences.P_JJKEYWORD,
        Activator.getString("JJPreferencesPage.JavaCC_Keyword"), getFieldEditorParent())); //$NON-NLS-1$
    addField(new ColorFieldEditor(JJPreferences.P_JAVAKEYWORD,
        Activator.getString("JJPreferencesPage.Java_Keyword"), getFieldEditorParent())); //$NON-NLS-1$
//    addField(new ColorFieldEditor(JJPreferences.P_BACKGROUND,
//        Activator.getString("JJPreferencesPage.Background"), getFieldEditorParent())); //$NON-NLS-1$
    addField(new ColorFieldEditor(JJPreferences.P_STRING,
        Activator.getString("JJPreferencesPage.Strings"), getFieldEditorParent())); //$NON-NLS-1$
    addField(new ColorFieldEditor(JJPreferences.P_COMMENT,
        Activator.getString("JJPreferencesPage.Comments"), getFieldEditorParent())); //$NON-NLS-1$
    addField(new ColorFieldEditor(JJPreferences.P_JDOC_COMMENT,
        Activator.getString("JJPreferencesPage.Javadoc_comments"), getFieldEditorParent())); //$NON-NLS-1$
    addField(new ColorFieldEditor(JJPreferences.P_TOKEN,
        Activator.getString("JJPreferencesPage.Token_declaration"), getFieldEditorParent())); //$NON-NLS-1$
    addField(new ColorFieldEditor(JJPreferences.P_PTOKEN,
        Activator.getString("JJPreferencesPage.Private_token_declaration"), getFieldEditorParent())); //$NON-NLS-1$
    addField(new ColorFieldEditor(JJPreferences.P_DEFAULT,
        Activator.getString("JJPreferencesPage.Text_by_default"), getFieldEditorParent())); //$NON-NLS-1$
    addField(new ColorFieldEditor(JJPreferences.P_MATCHING_CHAR,
        Activator.getString("JJPreferencesPage.Matching_char"), getFieldEditorParent()));  //$NON-NLS-1$
    addField(new ColorFieldEditor(JJPreferences.P_CONSOLE_COMMAND,
        Activator.getString("JJPreferencesPage.Console_commands"), getFieldEditorParent()));   //$NON-NLS-1$
  }

  protected void performApply() {
    updateColors();
    super.performApply();
  }
  
  public boolean performOk() {
    updateColors();
    return super.performOk();
  }
  
  protected void updateColors(){
    IWorkbenchWindow window = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
    if (window != null) {
      IWorkbenchPage page = window.getActivePage();
      if (page != null) {
        IEditorReference[] editorReference = page.getEditorReferences();
        for (int i = 0; i < editorReference.length; i++) {
          IEditorPart editorPart = editorReference[i].getEditor(false);
          if (editorPart instanceof JJEditor) {
            JJEditor editor = (JJEditor) editorPart;
            editor.updateColors();
          }
        }
      }
    }
  }

  public void init(IWorkbench workbench) {
    // TODO Raccord de méthode auto-généré
  }
}