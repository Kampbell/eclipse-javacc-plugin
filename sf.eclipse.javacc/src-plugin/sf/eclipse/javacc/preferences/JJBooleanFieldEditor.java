package sf.eclipse.javacc.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * Extends BooleanFieldEditor to add a constructor including tool tip text
 * 
 * @author Bill Fenlason 2012 - licensed under the JavaCC package license
 */
public class JJBooleanFieldEditor extends BooleanFieldEditor {

  /**
   * Creates a boolean field editor in the given style and tool tip text
   * 
   * @param name - the name of the preference this field editor works on
   * @param labelText - the label text of the field editor
   * @param toolTipText - the tool tip text for the label
   * @param style - the style, either <code>DEFAULT</code> or <code>SEPARATE_LABEL</code>
   * @param parent - the parent of the field editor's control
   * @see #DEFAULT
   * @see #SEPARATE_LABEL
   */
  public JJBooleanFieldEditor(final String name, final String labelText, final String toolTipText,
                              final int style, final Composite parent) {
    super(name, labelText, style, parent);

    getDescriptionControl(parent).setToolTipText(toolTipText);
  }

  /**
   * Creates a boolean field editor in the default style and tool tip text.
   * 
   * @param name - the name of the preference this field editor works on
   * @param label - the label text of the field editor
   * @param toolTipText - the tool tip text for the label
   * @param parent - the parent of the field editor's control
   */
  public JJBooleanFieldEditor(final String name, final String label, final String toolTipText,
                              final Composite parent) {
    this(name, label, toolTipText, DEFAULT, parent);
  }

  /**
   * Sets this field editor's state.
   * 
   * @param state - the new state
   */
  public void setBooleanValue(final boolean state) {
    getPreferenceStore().setValue(getPreferenceName(), state);
    load();
  }

}
