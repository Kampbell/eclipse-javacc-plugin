package sf.eclipse.javacc.options;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

/**
 * I Found no way but subclass to allow setting of CheckBox state
 * 
 * @author Remi Koutcherawy 2003 
 * GPL Licence www.gnu.org/licenses/gpl.txt
 */
public class BooleanFieldEditor
  extends org.eclipse.jface.preference.BooleanFieldEditor {

  protected Button checkbox;

  /**
   * Constructor for BooleanFieldEditor.
   * @param name the name of the preference this field editor works on
   * @param label the label text of the field editor
   * @param parent the parent of the field editor's control
   */
  public BooleanFieldEditor(String name, String label, Composite parent) {
    super(name, label, parent);
  }

  /**
   * Sets this field editor's state.
   * @param state the new state
   */
  public void setBooleanValue(boolean state) {
    if (checkbox != null) {
      checkbox.setSelection(state);
      checkbox.notifyListeners(SWT.Selection, new Event());
    }
  }

  /**
   * Returns the change button for this field editor.
   * Redefined only to get checkBox access
   * @return the change button
   */
  protected Button getChangeControl(Composite parent) {
    checkbox = super.getChangeControl(parent);
    return checkbox;
  }
}
