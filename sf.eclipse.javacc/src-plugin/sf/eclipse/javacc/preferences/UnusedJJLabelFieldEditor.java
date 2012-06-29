package sf.eclipse.javacc.preferences;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * A label field editor which only displays a text field
 * 
 * @author Bill Fenlason 2012 - licensed under the JavaCC package license
 */
public class UnusedJJLabelFieldEditor extends FieldEditor {

  // BF  05/2012 : based on www.eclipse.org/articles/Article-Field-Editors/field_editors.html

  /** The label */
  protected Label fLabel;

  /**
   * Constructs a new label field editor.
   * 
   * @param labelText - the label text
   * @param parent - the parent
   */
  public UnusedJJLabelFieldEditor(final String labelText, final Composite parent) {
    super("", labelText, parent); //$NON-NLS-1$
  }

  /** {@inheritDoc} */
  @Override
  protected void doFillIntoGrid(final Composite parent, final int numColumns) {
    fLabel = getLabelControl(parent);

    final GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
    gridData.horizontalSpan = numColumns;
    gridData.widthHint = fLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;

    fLabel.setLayoutData(gridData);
  }

  /** {@inheritDoc} */
  @Override
  protected void adjustForNumColumns(final int numColumns) {
    ((GridData) fLabel.getLayoutData()).horizontalSpan = numColumns;
  }

  /** {@inheritDoc} */
  @Override
  public int getNumberOfControls() {
    return 1;
  }

  /** {@inheritDoc} */
  @Override
  protected void doLoad() {
    // Required override, no action
  }

  /** {@inheritDoc} */
  @Override
  protected void doLoadDefault() {
    // Required override, no action
  }

  /** {@inheritDoc} */
  @Override
  protected void doStore() {
    // Required override, no action
  }
}
