package sf.eclipse.javacc.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * The TabbedPreferencePage Class.
 * 
 * @author Bill Fenlason 2012 - licensed under the JavaCC package license
 */
public abstract class TabbedPreferencePage extends FieldEditorPreferencePage {

  /** The tab folder */
  protected TabFolder          fTabFolder;

  /** The tab folder style (SWT.TOP or SWT.BOTTOM) */
  private int                  fTabFolderStyle = SWT.TOP;

  /** The apply changes at tab switch check box field editor */
  protected BooleanFieldEditor fApplyChangesAtTabSwitchFE;

  /** The tool tip text for the Apply button */
  private String               fApplyToolTip;

  /** The tool tip text for the Restore Defaults button */
  private String               fDefaultsToolTip;

  /** The field editors */
  protected List<FieldEditor>  fFieldEditors;

  /** The tab number list */
  private List<Integer>        fTabNumberList;

  /** The end of tab page flag */
  private boolean              fEndOfTabPages;

  /** The current tab number */
  protected int                fCurrentTabNumber;

  /** The last tab number */
  protected int                fLastTabNumber;

  /** The tab switch occurred flag */
  protected boolean            fTabSwitchOccurred;

  /**
   * Create a new default tabbed field editor preference page.
   */
  public TabbedPreferencePage() {
    this(GRID);
  }

  /**
   * Creates a new tabbed field editor preference page with the given style, an empty title, and no image.
   * 
   * @param style - either <code>GRID</code> or <code>FLAT</code>
   */
  protected TabbedPreferencePage(final int style) {
    super(style);
  }

  /**
   * Creates a new tabbed field editor preference page with the given title and style, but no image.
   * 
   * @param title - the title of this preference page
   * @param style - either <code>GRID</code> or <code>FLAT</code>
   */
  protected TabbedPreferencePage(final String title, final int style) {
    super(title, style);
  }

  /**
   * Creates a new tabbed field editor preference page with the given title, image, and style.
   * 
   * @param title - the title of this preference page
   * @param image - the image for this preference page, or <code>null</code> if none
   * @param style - either <code>GRID</code> or <code>FLAT</code>
   */
  protected TabbedPreferencePage(final String title, final ImageDescriptor image, final int style) {
    super(title, image, style);
  }

  /**
   * Adds a new tab to the preference page with no tab text, tool tip text or grid layout.
   */
  public void addTab() {
    addTab(null, null, null);
  }

  /**
   * Adds a new tab to the preference page with specified tab text and tool tip text but no grid layout.
   * 
   * @param tabText - the tab text
   * @param toolTipText - the tool tip text
   */
  public void addTab(final String tabText, final String toolTipText) {
    addTab(tabText, toolTipText, null);
  }

  /**
   * Adds a new tab to the preference page with specified tab text, tool tip text and layout.
   * 
   * @param tabText - the tab text
   * @param toolTipText - the tool tip text
   * @param layout - the layout
   */
  public void addTab(final String tabText, final String toolTipText, final Layout layout) {
    if (fTabFolder == null) {
      fTabFolder = new TabFolder(super.getFieldEditorParent(), fTabFolderStyle);
      fTabFolder.addSelectionListener(new SelectionAdapter() {

        @Override
        public void widgetSelected(@SuppressWarnings("unused") final SelectionEvent e) {

          if (!isValid()) { // Do not allow page switch if the field editor is invalid
            fTabFolder.setSelection(fLastTabNumber == 0 ? 0 : fLastTabNumber - 1);
          }
          else {
            fLastTabNumber = fCurrentTabNumber;
            fCurrentTabNumber = fTabFolder.getSelectionIndex() + 1;

            if (fApplyChangesAtTabSwitchFE != null && fApplyChangesAtTabSwitchFE.getBooleanValue()) {
              fTabSwitchOccurred = true;
              performOk();
            }
          }
        }
      });
    }

    final TabItem tabItem = new TabItem(fTabFolder, SWT.NONE);
    tabItem.setText(tabText == null ? "" : tabText); //$NON-NLS-1$
    tabItem.setToolTipText(toolTipText == null ? "" : toolTipText); //$NON-NLS-1$

    final Composite currentTab = new Composite(fTabFolder, SWT.NULL);

    if (layout != null) {
      currentTab.setLayout(layout);
    }
    currentTab.setFont(super.getFieldEditorParent().getFont());
    tabItem.setControl(currentTab);
  }

  /** {@inheritDoc} */
  @Override
  protected Composite getFieldEditorParent() {
    if (fTabFolder != null) {
      return (Composite) fTabFolder.getItem(fTabFolder.getItemCount() - 1).getControl();
    }
    return super.getFieldEditorParent();
  }

  /**
   * Adds a field editor to the tab page.
   * <p>
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("boxing")
  protected void addField(final FieldEditor fieldEditor) {
    if (!fEndOfTabPages) {
      if (fFieldEditors == null) {
        fFieldEditors = new ArrayList<FieldEditor>();
        fTabNumberList = new ArrayList<Integer>();
      }
      fFieldEditors.add(fieldEditor);
      fTabNumberList.add(fTabFolder != null ? fTabFolder.getItemCount() : 0);
    }
    super.addField(fieldEditor);
  }

  /**
   * End of tab pages
   * <p>
   * Ends the tab pages and adds the "Apply changes with tab switch" check box.
   * 
   * @param preferenceName - the preference name for the check box
   * @param labelText - the label text for the check box
   * @param toolTipText - the tool tip text
   */
  public void endOfTabPages(final String preferenceName, final String labelText, final String toolTipText) {
    fApplyChangesAtTabSwitchFE = new BooleanFieldEditor(preferenceName, labelText,
                                                        super.getFieldEditorParent());
    fEndOfTabPages = true;
    fApplyChangesAtTabSwitchFE.getDescriptionControl(super.getFieldEditorParent())
                              .setToolTipText(toolTipText);
    addField(fApplyChangesAtTabSwitchFE);
  }

  /**
   * Saves the tool tip text for the apply and default buttons. The tool tip text will be applied when the
   * control is created.
   * 
   * @param applyToolTip - the apply button tool tip text
   * @param defaultsToolTip - the restore defaults button tool tip text
   */
  public void setApplyAndDefaultToolTips(final String applyToolTip, final String defaultsToolTip) {
    fApplyToolTip = applyToolTip;
    fDefaultsToolTip = defaultsToolTip;
  }

  /**
   * Applies the tool tip text to the apply and restore default buttons when the control is created
   * <p>
   * {@inheritDoc}
   */
  @Override
  public void createControl(final Composite parent) {
    super.createControl(parent);
    getApplyButton().setToolTipText(fApplyToolTip);
    getDefaultsButton().setToolTipText(fDefaultsToolTip);
  }

  /**
   * Restores defaults for the field editors on the current tab page (only).
   * <p>
   * To restore all defaults, each page must be restored individually
   * <p>
   * {@inheritDoc}
   */
  @Override
  protected void performDefaults() {
    if (fFieldEditors == null || fTabFolder == null) {
      super.performDefaults();
      return;
    }

    for (int i = 0; i < fFieldEditors.size(); i++) {
      final int thisTabNumber = fTabNumberList.get(i).intValue();
      if (thisTabNumber == fCurrentTabNumber || thisTabNumber == 0) {
        fFieldEditors.get(i).loadDefault();
      }
      if (thisTabNumber > fCurrentTabNumber) {
        break;
      }
    }
    checkState();
    updateApplyButton();
  }

  /**
   * Applies changes for the last tab page if a tab switch has occurred and the check box to enable it has
   * been checked. If the check box is not checked or a tab switch has not occurred, all changes are applied
   * (and the dialog ends).
   * <p>
   * {@inheritDoc}
   */
  @Override
  public boolean performOk() {
    if (fTabSwitchOccurred) {
      fTabSwitchOccurred = false;
      return performOk(fLastTabNumber);
    }
    return super.performOk();
  }

  /**
   * Applies changes only for field editors on this tab page
   * <p>
   * {@inheritDoc}
   */
  @Override
  public void performApply() {
    fTabSwitchOccurred = true;
    fLastTabNumber = fCurrentTabNumber;
    performOk();
  }

  /**
   * Perform OK on a specified tab number.
   * 
   * @param tabNumber - the tab number
   * @return true, if successful
   */
  protected boolean performOk(final int tabNumber) {

    for (int i = 0; i < fFieldEditors.size(); i++) {
      final int editorTabNumber = fTabNumberList.get(i).intValue();

      if (editorTabNumber == tabNumber || editorTabNumber == 0) {
        final FieldEditor pe = fFieldEditors.get(i);
        pe.store();
        pe.load(); // pe.setPresentsDefaultValue(false); see eclipse bug 38547
      }
      if (editorTabNumber > tabNumber) {
        break;
      }
    }
    return true;
  }

  /**
   * Gets the tab folder control.
   * 
   * @return the tab folder control
   */
  protected TabFolder getTabFolderControl() {
    return fTabFolder;
  }

  /**
   * Sets the tab folder style.
   * <p>
   * If desired, must be called before the first tab is added.
   * 
   * @param tabFolderStyle - the new tab folder style
   */
  protected void setTabFolderStyle(final int tabFolderStyle) {
    fTabFolderStyle = tabFolderStyle;
  }

}