package sf.eclipse.javacc.options;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

/**
 * I Found no way but subclass to allow setting of CheckBox state.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class BooleanFieldEditor extends org.eclipse.jface.preference.BooleanFieldEditor {

  // MMa 02/2010 : formatting and javadoc revision

  /** the checkbox */
  protected Button fCheckbox;

  /**
   * Constructor for BooleanFieldEditor.
   * 
   * @param name the name of the preference this field editor works on
   * @param label the label text of the field editor
   * @param parent the parent of the field editor's control
   */
  public BooleanFieldEditor(final String name, final String label, final Composite parent) {
    super(name, label, parent);
  }

  /**
   * Sets this field editor's state.
   * 
   * @param state the new state
   */
  public void setBooleanValue(final boolean state) {
    if (fCheckbox != null) {
      fCheckbox.setSelection(state);
      fCheckbox.notifyListeners(SWT.Selection, new Event());
    }
  }

  /**
   * Returns the change button for this field editor. Redefined only to get checkBox access.
   * 
   * @return the change button
   */
  @Override
  protected Button getChangeControl(final Composite parent) {
    fCheckbox = super.getChangeControl(parent);
    return fCheckbox;
  }
}
