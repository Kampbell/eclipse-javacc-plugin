package sf.eclipse.javacc.options;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

/**
 * I found no way but sub-classing to allow setting of CheckBox state.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 */
class BooleanFieldEditor extends org.eclipse.jface.preference.BooleanFieldEditor {

  // MMa 02/2010 : formatting and javadoc revision

  /** The checkbox */
  protected Button jCheckbox;

  /**
   * Constructor for BooleanFieldEditor.
   * 
   * @param aName - the name of the preference this field editor works on
   * @param aLabel - the label text of the field editor
   * @param aParent - the parent of the field editor's control
   */
  public BooleanFieldEditor(final String aName, final String aLabel, final Composite aParent) {
    super(aName, aLabel, aParent);
  }

  /**
   * Sets this field editor's state.
   * 
   * @param aState - the new state
   */
  public void setBooleanValue(final boolean aState) {
    if (jCheckbox != null) {
      jCheckbox.setSelection(aState);
      jCheckbox.notifyListeners(SWT.Selection, new Event());
    }
  }

  /**
   * Returns the change button for this field editor. Redefined only to get checkBox access.
   * <p>
   * {@inheritDoc}
   */
  @Override
  protected Button getChangeControl(final Composite aParent) {
    jCheckbox = super.getChangeControl(aParent);
    return jCheckbox;
  }
}
