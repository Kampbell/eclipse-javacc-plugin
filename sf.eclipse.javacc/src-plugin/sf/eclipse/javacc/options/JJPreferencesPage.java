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
    setDescription("Colors for JavaCC plug-in");
  }
  
  /**
   * Creates the field editors. 
   * Each field editor knows how to save and restore itself.
   */
  public void createFieldEditors() {
    // Colors
    addField(new ColorFieldEditor(JJPreferences.P_JJKEYWORD,
        "JavaCC Keyword:", getFieldEditorParent()));
    addField(new ColorFieldEditor(JJPreferences.P_JAVAKEYWORD,
        "Java Keyword:", getFieldEditorParent()));
    addField(new ColorFieldEditor(JJPreferences.P_BACKGROUND,
        "Background:", getFieldEditorParent()));
    addField(new ColorFieldEditor(JJPreferences.P_STRING,
        "Strings:", getFieldEditorParent()));
    addField(new ColorFieldEditor(JJPreferences.P_COMMENT,
        "Comments:", getFieldEditorParent()));
    addField(new ColorFieldEditor(JJPreferences.P_JDOC_COMMENT,
        "Javadoc comments:", getFieldEditorParent()));
    addField(new ColorFieldEditor(JJPreferences.P_TOKEN,
        "Token declaration:", getFieldEditorParent()));
    addField(new ColorFieldEditor(JJPreferences.P_PTOKEN,
        "Private token declaration:", getFieldEditorParent()));
    addField(new ColorFieldEditor(JJPreferences.P_DEFAULT,
        "Text by default:", getFieldEditorParent()));
    addField(new ColorFieldEditor(JJPreferences.P_MATCHING_CHAR,
        "Matching char:", getFieldEditorParent())); 
    addField(new ColorFieldEditor(JJPreferences.P_CONSOLE_COMMAND,
        "Console commands:", getFieldEditorParent()));  
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