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
 * @author Marc Mazas 2014
 */
class CycleButtonFieldEditor extends FieldEditor {

  // BF  05/2012 : added for JavaCC preference page font attribute selection
  // MMa 11/2014 : some renamings

  /** The toggle button */
  protected Button         jButton;

  /** The button label text array */
  protected final String[] jButtonLabels;

  /** The button preference value array */
  protected final String[] jButtonValues;

  /** The button tool tip text */
  protected final String   jButtonToolTipText;

  /** The label */
  protected Label          jLabel;

  /** The label text */
  protected final String   jLabelText;

  /** The label tool tip text */
  protected final String   jLabelToolTipText;

  /** The current selection index */
  protected int            jSelectionIndex;

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

    jButtonLabels = buttonText != null ? buttonText : emptyStringArray;
    jButtonValues = buttonValues != null ? buttonValues : emptyStringArray;
    Assert.isTrue(jButtonLabels.length > 0 && jButtonLabels.length == jButtonValues.length);

    jButtonToolTipText = buttonToolTipText;
    jLabelText = labelText;
    jLabelToolTipText = labelToolTipText;

    createControl(parent);
  }

  /** {@inheritDoc} */
  @Override
  public int getNumberOfControls() {
    return (jLabelText == null && jLabelToolTipText == null) ? 1 : 2;
  }

  /** {@inheritDoc} */
  @Override
  protected void doFillIntoGrid(final Composite parent, final int numColumns) {
    jButton = getPushButtonControl(parent);
    final GridData gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
    gd.horizontalSpan = numColumns - (getNumberOfControls() - 1);

    // insure that the button is wide enough for the longest label
    for (int i = jButtonLabels.length - 1; i >= 0; i--) {
      jButton.setText(jButtonLabels[i]);
      final Point trueSize = jButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
      if (trueSize.x > gd.widthHint) {
        gd.widthHint = trueSize.x;
      }
    }
    jButton.setLayoutData(gd);

    if (jLabel != null) {
      final GridData gdl = new GridData(SWT.LEFT, SWT.CENTER, false, false);
      jLabel.setLayoutData(gdl);
    }
  }

  /** {@inheritDoc} */
  @Override
  protected void adjustForNumColumns(final int numColumns) {
    if (jLabel != null) {
      ((GridData) jButton.getLayoutData()).horizontalSpan = 1;
      ((GridData) jLabel.getLayoutData()).horizontalSpan = numColumns - 1;
    }
    else {
      ((GridData) jButton.getLayoutData()).horizontalSpan = numColumns;
    }
  }

  /**
   * Returns this field editor's control.
   * 
   * @param parent - The parent Composite which contains the push button
   * @return the button control
   */
  public Button getPushButtonControl(final Composite parent) {
    if (jButton == null) {
      jButton = new Button(parent, SWT.PUSH);
      jButton.setFont(parent.getFont());
      jButton.setText(jButtonLabels[0]);
      jButton.setToolTipText(jButtonToolTipText);
      jButton.addSelectionListener(new SelectionAdapter() {

        /** {@inheritDoc} */
        @Override
        public void widgetSelected(final SelectionEvent e) {
          jSelectionIndex = (jSelectionIndex + 1 >= jButtonLabels.length) ? 0 : jSelectionIndex + 1;
          jButton.setText(jButtonLabels[jSelectionIndex]);
        }
      });

      if (jLabelText != null || jLabelToolTipText != null) {
        jLabel = getLabelControl(parent);
        setLabelText(jLabelText == null ? "" : jLabelText); //$NON-NLS-1$
        jLabel.setToolTipText(jLabelToolTipText);
      }

      if (getPreferenceName().length() == 0) {
        setVisible(false);
      }
    }
    else {
      checkParent(jButton, parent);
    }
    return jButton;
  }

  /** {@inheritDoc} */
  @Override
  public void setEnabled(final boolean enabled, final Composite parent) {
    if (jButton != null) {
      jButton.setEnabled(enabled);
    }
    if (jLabel != null) {
      jLabel.setEnabled(enabled);
    }
  }

  /**
   * Set the button visibility.
   * 
   * @param visible - the boolean visibility value
   */
  public void setVisible(final boolean visible) {
    if (jButton != null) {
      jButton.setVisible(visible);
    }
    if (jLabel != null) {
      jLabel.setVisible(visible);
    }
  }

  /** {@inheritDoc} */
  @Override
  protected void doLoad() {
    jSelectionIndex = 0;

    if (getPreferenceName().length() > 0) {
      final String value = getPreferenceStore().getString(getPreferenceName());

      for (jSelectionIndex = 0; jSelectionIndex < jButtonValues.length; jSelectionIndex++) {
        if (value.equals(jButtonValues[jSelectionIndex])) {
          break;
        }
      }
      jSelectionIndex = (jSelectionIndex >= jButtonValues.length) ? 0 : jSelectionIndex;
    }
    jButton.setText(jButtonLabels[jSelectionIndex]);
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
      getPreferenceStore().setValue(getPreferenceName(), jButtonValues[jSelectionIndex]);
    }
  }

}
