package sf.eclipse.javacc.options;

import java.io.File;

import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;

/**
 * I Found no way but redefine DirectoryFieldEditor for adding lastPath, and label.
 * 
 * @see org.eclipse.jface.preference.DirectoryFieldEditor
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class DirectoryFieldEditor extends StringButtonFieldEditor {

  // MMa 02/2010 : formatting and javadoc revision

  /** The last path, or null if none */
  private String fLastPath;
  /** The label text for directory chooser, or null if none */
  private String fLabel;

  /**
   * Creates a directory field editor.
   * 
   * @param name the name of the preference this field editor works on
   * @param label the label text of the field editor
   * @param info the label text of the Directory chooser
   * @param path the path this editor opens in
   * @param parent the parent of the field editor's control
   */
  public DirectoryFieldEditor(final String name, final String label, final String info, final String path,
                              final Composite parent) {
    fLabel = info;
    init(name, label);
    setErrorMessage(JFaceResources.getString("DirectoryFieldEditor.errorMessage")); //$NON-NLS-1$
    setChangeButtonText(JFaceResources.getString("openBrowse")); //$NON-NLS-1$
    setValidateStrategy(VALIDATE_ON_FOCUS_LOST);
    fLastPath = path;
    createControl(parent);
  }

  /**
   * Creates a label field editor.
   * 
   * @param name the name of the preference this field editor works on
   * @param label the label text of the field editors
   * @param parent the parent of the field editor's control
   */
  public DirectoryFieldEditor(final String name, final String label, final Composite parent) {
    super(name, label, parent);
  }

  /**
   * Method declared on StringFieldEditor. Checks whether the text input field contains a valid directory.
   */
  @Override
  protected boolean doCheckState() {
    String fileName = getTextControl().getText();
    fileName = fileName.trim();
    if (fileName.length() == 0 && isEmptyStringAllowed()) {
      return true;
    }
    final File file = new File(fileName);
    return file.isDirectory();
  }

  /**
   * @see StringButtonFieldEditor#changePressed()
   */
  @Override
  protected String changePressed() {
    final DirectoryDialog dialog = new DirectoryDialog(getShell());
    if (fLabel != null) {
      dialog.setMessage(fLabel);
    }
    if (fLastPath != null) {
      if (new File(fLastPath).exists()) {
        dialog.setFilterPath(fLastPath);
      }
    }
    String dir = dialog.open();
    if (dir != null) {
      dir = dir.trim();
      if (dir.length() == 0) {
        return null;
      }
      fLastPath = dir;
    }
    return dir;
  }
}
