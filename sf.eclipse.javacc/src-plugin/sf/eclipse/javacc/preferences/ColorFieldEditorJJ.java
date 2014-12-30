package sf.eclipse.javacc.preferences;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * Extends the ColorFieldEditor to add constructors including tool tip text.
 * 
 * @author Bill Fenlason 2012 - licensed under the JavaCC package license
 */
class ColorFieldEditorJJ extends ColorFieldEditor {

  // MMa 10/2012 : renamed

  /**
   * Creates a color field editor with tool tip text
   * <p>
   * The supplied tool tip text is applied to both the label and button
   * 
   * @param name - the name of the preference this field editor works on
   * @param labelText - the label text of the field editor
   * @param toolTipText - the tool tip text
   * @param parent - the parent of the field editor's control
   */
  public ColorFieldEditorJJ(final String name, final String labelText, final String toolTipText,
                            final Composite parent) {
    super(name, labelText, parent);

    super.getLabelControl().setToolTipText(toolTipText);
    super.getColorSelector().getButton().setToolTipText(toolTipText);
  }

  /**
   * Creates a color field editor with separate tool tip text for the label and the color selector button.
   * 
   * @param name - the name of the preference this field editor works on
   * @param labelText - the label text of the field editor
   * @param labelToolTipText - the label tool tip text
   * @param buttonToolTipText - the color selector button tool tip text
   * @param parent - the parent of the field editor's control
   */
  public ColorFieldEditorJJ(final String name, final String labelText, final String labelToolTipText,
                            final String buttonToolTipText, final Composite parent) {
    super(name, labelText, parent);

    super.getLabelControl().setToolTipText(labelToolTipText);
    super.getColorSelector().getButton().setToolTipText(buttonToolTipText);
  }
}
