package sf.eclipse.javacc.options;

import java.io.File;

import org.eclipse.jface.preference.StringButtonFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;

/**
 * I found no way but sub-classing to allow adding the last path and the label.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class DirectoryFieldEditor extends StringButtonFieldEditor {

  // MMa 02/2010 : formatting and javadoc revision

  /** The last path, or null if none */
  private String jLastPath;
  /** The label text for directory chooser, or null if none */
  private String jLabel;

  /**
   * Creates a directory field editor.
   * 
   * @param aName the name of the preference this field editor works on
   * @param aLabel the label text of the field editor
   * @param aInfo the label text of the Directory chooser
   * @param aPath the path this editor opens in
   * @param aParent the parent of the field editor's control
   */
  public DirectoryFieldEditor(final String aName, final String aLabel, final String aInfo, final String aPath,
                              final Composite aParent) {
    jLabel = aInfo;
    init(aName, aLabel);
    setErrorMessage(JFaceResources.getString("DirectoryFieldEditor.errorMessage")); //$NON-NLS-1$
    setChangeButtonText(JFaceResources.getString("openBrowse")); //$NON-NLS-1$
    setValidateStrategy(VALIDATE_ON_FOCUS_LOST);
    jLastPath = aPath;
    createControl(aParent);
  }

  /**
   * Creates a label field editor.
   * 
   * @param aName the name of the preference this field editor works on
   * @param aLabel the label text of the field editors
   * @param aParent the parent of the field editor's control
   */
  public DirectoryFieldEditor(final String aName, final String aLabel, final Composite aParent) {
    super(aName, aLabel, aParent);
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
    if (jLabel != null) {
      dialog.setMessage(jLabel);
    }
    if (jLastPath != null) {
      if (new File(jLastPath).exists()) {
        dialog.setFilterPath(jLastPath);
      }
    }
    String dir = dialog.open();
    if (dir != null) {
      dir = dir.trim();
      if (dir.length() == 0) {
        return null;
      }
      jLastPath = dir;
    }
    return dir;
  }
}
