package sf.eclipse.javacc.preferences;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * A field editor for a cyclic toggle button preference.
 * <p>
 * The push button cycles through a list of button labels and the corresponding value is stored as the
 * preference value.
 * 
 * @author Bill Fenlason 2012 - licensed under the JavaCC package license
 */
public class CycleButtonFieldEditor extends FieldEditor {

  // BF  05/2012 : added for JavaCC preference page font attribute selection

  /** The toggle button */
  protected Button         fButton;

  /** The button label text array */
  protected final String[] fButtonLabels;

  /** The button preference value array */
  protected final String[] fButtonValues;

  /** The button tool tip text */
  private final String     fButtonToolTipText;

  /** The label */
  private Label            fLabel;

  /** The label text */
  private final String     fLabelText;

  /** The label tool tip text */
  private final String     fLabelToolTipText;

  /** The current selection index */
  protected int            fSelectionIndex;

  /**
   * Creates a cyclic toggle button field editor with no label
   * 
   * @param preferenceName - the name of the preference this field editor works on
   * @param buttonText - the button text array
   * @param buttonValues - the button preference values array
   * @param buttonToolTipText - the button tool tip text
   * @param parent - the parent of the field editor's control
   */
  public CycleButtonFieldEditor(final String preferenceName, //
                                final String[] buttonText, //
                                final String[] buttonValues, //
                                final String buttonToolTipText, //
                                final Composite parent) {
    this(preferenceName, buttonText, buttonValues, buttonToolTipText, null, null, parent);
  }

  /**
   * Creates a cyclic toggle button field editor with a label.
   * 
   * @param preferenceName - the name of the preference this field editor works on
   * @param buttonText - the button text array
   * @param buttonValues - the button values array
   * @param buttonToolTipText - the button tool tip text
   * @param labelText - the label text of the field editor control, or null
   * @param labelToolTipText - the
   * @param parent - the parent of the field editor's control
   */
  public CycleButtonFieldEditor(final String preferenceName, // 
                                final String[] buttonText, //
                                final String[] buttonValues, //
                                final String buttonToolTipText, //
                                final String labelText, //
                                final String labelToolTipText, //
                                final Composite parent) {
    final String[] emptyStringArray = {
      " " }; //$NON-NLS-1$

    setPreferenceName(preferenceName != null ? preferenceName : ""); //$NON-NLS-1$

    fButtonLabels = buttonText != null ? buttonText : emptyStringArray;
    fButtonValues = buttonValues != null ? buttonValues : emptyStringArray;
    Assert.isTrue(fButtonLabels.length > 0 && fButtonLabels.length == fButtonValues.length);

    fButtonToolTipText = buttonToolTipText;
    fLabelText = labelText;
    fLabelToolTipText = labelToolTipText;

    createControl(parent);
  }

  /** {@inheritDoc} */
  @Override
  public int getNumberOfControls() {
    return (fLabelText == null && fLabelToolTipText == null) ? 1 : 2;
  }

  /** {@inheritDoc} */
  @Override
  protected void doFillIntoGrid(final Composite parent, final int numColumns) {
    fButton = getPushButtonControl(parent);
    final GridData gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
    gd.horizontalSpan = numColumns - (getNumberOfControls() - 1);

    // insure that the button is wide enough for the longest label
    for (int i = fButtonLabels.length - 1; i >= 0; i--) {
      fButton.setText(fButtonLabels[i]);
      final Point trueSize = fButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
      if (trueSize.x > gd.widthHint) {
        gd.widthHint = trueSize.x;
      }
    }
    fButton.setLayoutData(gd);

    if (fLabel != null) {
      final GridData gdl = new GridData(SWT.LEFT, SWT.CENTER, false, false);
      fLabel.setLayoutData(gdl);
    }
  }

  /** {@inheritDoc} */
  @Override
  protected void adjustForNumColumns(final int numColumns) {
    if (fLabel != null) {
      ((GridData) fButton.getLayoutData()).horizontalSpan = 1;
      ((GridData) fLabel.getLayoutData()).horizontalSpan = numColumns - 1;
    }
    else {
      ((GridData) fButton.getLayoutData()).horizontalSpan = numColumns;
    }
  }

  /**
   * Returns this field editor's control.
   * 
   * @param parent - The parent Composite which contains the push button
   * @return the button control
   */
  public Button getPushButtonControl(final Composite parent) {
    if (fButton == null) {
      fButton = new Button(parent, SWT.PUSH);
      fButton.setFont(parent.getFont());
      fButton.setText(fButtonLabels[0]);
      fButton.setToolTipText(fButtonToolTipText);
      fButton.addSelectionListener(new SelectionAdapter() {

        @Override
        public void widgetSelected(@SuppressWarnings("unused") final SelectionEvent e) {
          fSelectionIndex = (fSelectionIndex + 1 >= fButtonLabels.length) ? 0 : fSelectionIndex + 1;
          fButton.setText(fButtonLabels[fSelectionIndex]);
        }
      });

      if (fLabelText != null || fLabelToolTipText != null) {
        fLabel = getLabelControl(parent);
        setLabelText(fLabelText == null ? "" : fLabelText); //$NON-NLS-1$
        fLabel.setToolTipText(fLabelToolTipText);
      }

      if (getPreferenceName().length() == 0) {
        setVisible(false);
      }
    }
    else {
      checkParent(fButton, parent);
    }
    return fButton;
  }

  /** {@inheritDoc} */
  @Override
  public void setEnabled(final boolean enabled, @SuppressWarnings("unused") final Composite parent) {
    if (fButton != null) {
      fButton.setEnabled(enabled);
    }
    if (fLabel != null) {
      fLabel.setEnabled(enabled);
    }
  }

  /**
   * Set the button visibility.
   * 
   * @param visible - the boolean visibility value
   */
  public void setVisible(final boolean visible) {
    if (fButton != null) {
      fButton.setVisible(visible);
    }
    if (fLabel != null) {
      fLabel.setVisible(visible);
    }
  }

  /** {@inheritDoc} */
  @Override
  protected void doLoad() {
    fSelectionIndex = 0;

    if (getPreferenceName().length() > 0) {
      final String value = getPreferenceStore().getString(getPreferenceName());

      for (fSelectionIndex = 0; fSelectionIndex < fButtonValues.length; fSelectionIndex++) {
        if (value.equals(fButtonValues[fSelectionIndex])) {
          break;
        }
      }
      fSelectionIndex = (fSelectionIndex >= fButtonValues.length) ? 0 : fSelectionIndex;
    }
    fButton.setText(fButtonLabels[fSelectionIndex]);
  }

  /** {@inheritDoc} */
  @Override
  protected void doLoadDefault() {
    if (getPreferenceName().length() > 0) {
      getPreferenceStore().setToDefault(getPreferenceName());
      doLoad();
    }
  }

  /** {@inheritDoc} */
  @Override
  protected void doStore() {
    if (getPreferenceName().length() > 0) {
      getPreferenceStore().setValue(getPreferenceName(), fButtonValues[fSelectionIndex]);
    }
  }

}
