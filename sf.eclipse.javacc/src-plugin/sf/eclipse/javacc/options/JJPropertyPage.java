package sf.eclipse.javacc.options;


import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.dialogs.PropertyPage;

import sf.eclipse.javacc.base.IJJConstants;
import sf.eclipse.javacc.head.Activator;

/**
 * The Property page class for JavaCC projects or files. Enables setting of JavaCC options for project or jj
 * file.<br>
 * Referenced by plugin.xml<br>
 * <extension point="org.eclipse.ui.propertyPages">
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class JJPropertyPage extends PropertyPage implements IJJConstants {

  // MMa 02/2010 : formatting and javadoc revision ; added some properties

  /** The current folder */
  protected TabFolder        fFolder;
  /** The current global item */
  protected TabItem          fJJRunItem;
  /** The current JavaCC item */
  protected TabItem          fJJCCItem;
  /** The current JJTree item */
  protected TabItem          fJJTreeItem;
  /** The current JJDoc item */
  protected TabItem          fJJDocItem;
  /** The current JTB item */
  protected TabItem          fJTBItem;

  /** The current global options */
  protected RuntimeOptions fJJRunOptions;
  /** The current JavaCC options */
  protected JavaccOptions      fJJCCOptions;
  /** The current JJTree options */
  protected JjtreeOptions    fJJTreeOptions;
  /** The current JJDoc options */
  protected JjdocOptions     fJJDocOptions;
  /** The current JTB options */
  protected JtbOptions       fJTBOptions;

  /** The current resource */
  protected IResource        fResource;
  /** The current project */
  protected IProject         fProject;

  /**
   * Creates contents (called from plugin.xml).
   * 
   * @see PreferencePage#createContents(Composite)
   */
  @Override
  protected Control createContents(final Composite aParent) {
    // create a TabFolder
    fFolder = new TabFolder(aParent, SWT.NONE);
    final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    fFolder.setLayoutData(gd);

    // read configuration from IResource
    final IAdaptable ia = getElement();
    fResource = (IResource) ia.getAdapter(IResource.class);
    if (fResource != null) {
      fProject = fResource.getProject();
    }

    // JJRuntime always present
    fJJRunOptions = new RuntimeOptions(fFolder, fResource);
    fJJRunItem = new TabItem(fFolder, SWT.NONE);
    fJJRunItem.setText(Activator.getString("JJPropertyPage.Runtime_options_Tab")); //$NON-NLS-1$
    fJJRunItem.setToolTipText(Activator.getString("JJPropertyPage.Runtime_options_Tab_TT")); //$NON-NLS-1$
    fJJRunItem.setControl(fJJRunOptions);

    // for project
    addJCCTab();
    addJTreeTab();
    addJDocTab();
    addJTBTab();

    // test a property to see if in need of a first initialization
    final IScopeContext projectScope = new ProjectScope(fProject);
    final IEclipsePreferences prefs = projectScope.getNode(IJJConstants.ID);
    if (prefs.get(RUNTIME_JJJAR, null) == null) {
      performDefaults();
    }
    return aParent;
  }

  /*
   * Convenient methods
   */
  /**
   * Adds the JavaCC preference tab.
   */
  protected void addJCCTab() {
    fJJCCOptions = new JavaccOptions(fFolder, fResource);
    fJJCCItem = new TabItem(fFolder, SWT.NONE);
    fJJCCItem.setText(Activator.getString("JJPropertyPage.JavaCC_options_Tab")); //$NON-NLS-1$
    fJJCCItem.setToolTipText(Activator.getString("JJPropertyPage.JavaCC_options_Tab_TT")); //$NON-NLS-1$
    fJJCCItem.setControl(fJJCCOptions);
  }

  /**
   * Adds the JJTree preference tab.
   */
  protected void addJTreeTab() {
    fJJTreeOptions = new JjtreeOptions(fFolder, fResource);
    fJJTreeItem = new TabItem(fFolder, SWT.NONE);
    fJJTreeItem.setText(Activator.getString("JJPropertyPage.JJTree_options_Tab")); //$NON-NLS-1$
    fJJTreeItem.setToolTipText(Activator.getString("JJPropertyPage.JJTree_options_Tab_TT")); //$NON-NLS-1$
    fJJTreeItem.setControl(fJJTreeOptions);
  }

  /**
   * Adds the JJDoc preference tab.
   */
  protected void addJDocTab() {
    fJJDocOptions = new JjdocOptions(fFolder, fResource);
    fJJDocItem = new TabItem(fFolder, SWT.NONE);
    fJJDocItem.setText(Activator.getString("JJPropertyPage.JJDoc_options_Tab")); //$NON-NLS-1$
    fJJDocItem.setToolTipText(Activator.getString("JJPropertyPage.JJDoc_options_Tab_TT")); //$NON-NLS-1$
    fJJDocItem.setControl(fJJDocOptions);
  }

  /**
   * Adds the JTB preference tab.
   */
  protected void addJTBTab() {
    fJTBOptions = new JtbOptions(fFolder, fResource);
    fJTBItem = new TabItem(fFolder, SWT.NONE);
    fJTBItem.setText(Activator.getString("JJPropertyPage.JTB_options_Tab")); //$NON-NLS-1$
    fJTBItem.setToolTipText(Activator.getString("JJPropertyPage.JTB_options_Tab_TT")); //$NON-NLS-1$
    fJTBItem.setControl(fJTBOptions);
  }

  /**
   * @see IPreferencePage#performOk()
   */
  @Override
  public boolean performOk() {
    fJJRunOptions.performOk();
    fJJCCOptions.performOk();
    fJJTreeOptions.performOk();
    fJJDocOptions.performOk();
    fJTBOptions.performOk();

    // ask for rebuild (should check if an option has changed)
    try {
      final IProject proj = fResource.getProject();
      final MessageDialog dialog = new MessageDialog(
                                                     getShell(),
                                                     Activator
                                                              .getString("JJPropertyPage.Ask_for_rebuild_title"), //$NON-NLS-1$
                                                     null,
                                                     Activator
                                                              .getString("JJPropertyPage.Ask_for_rebuild_msg"), //$NON-NLS-1$
                                                     MessageDialog.QUESTION, new String[] {
                                                         IDialogConstants.YES_LABEL,
                                                         IDialogConstants.NO_LABEL,
                                                         IDialogConstants.CANCEL_LABEL }, 2);
      final int res = dialog.open();
      if (res == Window.OK) {
        // OK clean build
        proj.build(IncrementalProjectBuilder.CLEAN_BUILD, JJ_BUILDER_ID, null, null);
      }
      else if (res != 1) {
        // Cancel abort
        return false;
      }
      // "OK" or "NO clean" proceeds to set options
    } catch (final CoreException e) {
      e.printStackTrace();
      return false;
    }
    return super.performOk();
  }

  /**
   * @see PreferencePage#performDefaults()
   */
  @Override
  protected void performDefaults() {
    super.performDefaults();
    fJJRunOptions.performDefaults();
    if (fJJCCOptions != null) {
      fJJCCOptions.performDefaults();
    }
    if (fJJTreeOptions != null) {
      fJJTreeOptions.performDefaults();
    }
    if (fJJDocOptions != null) {
      fJJDocOptions.performDefaults();
    }
    if (fJTBOptions != null) {
      fJTBOptions.performDefaults();
    }
  }
}