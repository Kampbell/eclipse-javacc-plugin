package sf.eclipse.javacc.options;

import java.io.File;

import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;

/**
 * I Found no way but redefine DirectoryFieldEditor
 * for adding lastPath, and label.
 * @see org.eclipse.jface.preference.DirectoryFieldEditor
 * 
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */
public class DirectoryFieldEditor extends StringButtonFieldEditor {
  // The last path, or null if none.
  private String lastPath;
  // The label text for directory chooser, or null if none.
  private String label;

  /**
   * Creates a directory field editor.
   * 
   * @param name the name of the preference this field editor works on
   * @param label  the label text of the field editor
   * @param info   the label text of the Directory chooser
   * @param path   the path this editor opens in
   * @param parent the parent of the field editor's control
   */
  public DirectoryFieldEditor(String name, String label, String info, String path, Composite parent) {
    this.label = info;
    init(name, label);
    setErrorMessage(JFaceResources.getString("DirectoryFieldEditor.errorMessage")); //$NON-NLS-1$
    setChangeButtonText(JFaceResources.getString("openBrowse")); //$NON-NLS-1$
    setValidateStrategy(VALIDATE_ON_FOCUS_LOST);
    lastPath = path;
    createControl(parent);
  }
  
  public DirectoryFieldEditor(String p_path, String string, Composite fieldEditorParent) {
	super(p_path, string, fieldEditorParent);
  }
  
  /**
   * Method declared on StringFieldEditor.
   * Checks whether the text input field contains a valid directory.
   */
  protected boolean doCheckState() {
    String fileName = getTextControl().getText();
    fileName = fileName.trim();
    if (fileName.length() == 0 && isEmptyStringAllowed())
      return true;
    File file = new File(fileName);
    return file.isDirectory();
  }
  
  /**
   * @see org.eclipse.jface.preference.StringButtonFieldEditor#changePressed()
   */
  protected String changePressed() {
    DirectoryDialog dialog = new DirectoryDialog(getShell());
    if (label != null)
      dialog.setMessage(label);
    if (lastPath != null) {
      if (new File(lastPath).exists())
        dialog.setFilterPath(lastPath);
    }
    String dir = dialog.open();
    if (dir != null) {
      dir = dir.trim();
      if (dir.length() == 0)
        return null;
      lastPath = dir;
    }
    return dir;
  }
}
