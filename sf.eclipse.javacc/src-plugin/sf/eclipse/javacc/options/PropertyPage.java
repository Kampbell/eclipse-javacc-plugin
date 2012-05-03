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

import sf.eclipse.javacc.base.IJJConstants;
import sf.eclipse.javacc.head.Activator;

/**
 * The Property page class for JavaCC projects or files. Enables setting of JavaCC options for project or jj
 * file.<br>
 * Referenced by plugin.xml<br>
 * <extension point="org.eclipse.ui.propertyPages">
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011
 */
public class PropertyPage extends org.eclipse.ui.dialogs.PropertyPage implements IJJConstants {

  // MMa 02/2010 : formatting and javadoc revision ; added some properties
  // MMa 08/2011 : renamed

  /** The current folder */
  protected TabFolder     jFolder;
  /** The current global item */
  protected TabItem       jJJRunItem;
  /** The current JavaCC item */
  protected TabItem       jJJCCItem;
  /** The current JJTree item */
  protected TabItem       jJJTreeItem;
  /** The current JJDoc item */
  protected TabItem       jJJDocItem;
  /** The current JTB item */
  protected TabItem       jJTBItem;

  /** The current global options */
  protected GlobalOptions jJJRunOptions;
  /** The current JavaCC options */
  protected JavaCCOptions jJJCCOptions;
  /** The current JJTree options */
  protected JJTreeOptions jJJTreeOptions;
  /** The current JJDoc options */
  protected JJDocOptions  jJJDocOptions;
  /** The current JTB options */
  protected JTBOptions    jJTBOptions;

  /** The current resource */
  protected IResource     jResource;
  /** The current project */
  protected IProject      jProject;

  /**
   * Creates contents (called from plugin.xml).
   * 
   * @see PreferencePage#createContents(Composite)
   */
  @Override
  protected Control createContents(final Composite aParent) {
    // create a TabFolder
    jFolder = new TabFolder(aParent, SWT.NONE);
    final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    jFolder.setLayoutData(gd);

    // read configuration from IResource
    final IAdaptable ia = getElement();
    jResource = (IResource) ia.getAdapter(IResource.class);
    if (jResource != null) {
      jProject = jResource.getProject();
    }

    // JJRuntime always present
    jJJRunOptions = new GlobalOptions(jFolder, jResource);
    jJJRunItem = new TabItem(jFolder, SWT.NONE);
    jJJRunItem.setText(Activator.getString("PropertyPage.Runtime_options_Tab")); //$NON-NLS-1$
    jJJRunItem.setToolTipText(Activator.getString("PropertyPage.Runtime_options_Tab_TT")); //$NON-NLS-1$
    jJJRunItem.setControl(jJJRunOptions);

    // for project
    addJCCTab();
    addJTreeTab();
    addJDocTab();
    addJTBTab();

    // test a property to see if in need of a first initialization
    final IScopeContext projectScope = new ProjectScope(jProject);
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
    jJJCCOptions = new JavaCCOptions(jFolder, jResource);
    jJJCCItem = new TabItem(jFolder, SWT.NONE);
    jJJCCItem.setText(Activator.getString("PropertyPage.JavaCC_options_Tab")); //$NON-NLS-1$
    jJJCCItem.setToolTipText(Activator.getString("PropertyPage.JavaCC_options_Tab_TT")); //$NON-NLS-1$
    jJJCCItem.setControl(jJJCCOptions);
  }

  /**
   * Adds the JJTree preference tab.
   */
  protected void addJTreeTab() {
    jJJTreeOptions = new JJTreeOptions(jFolder, jResource);
    jJJTreeItem = new TabItem(jFolder, SWT.NONE);
    jJJTreeItem.setText(Activator.getString("PropertyPage.JJTree_options_Tab")); //$NON-NLS-1$
    jJJTreeItem.setToolTipText(Activator.getString("PropertyPage.JJTree_options_Tab_TT")); //$NON-NLS-1$
    jJJTreeItem.setControl(jJJTreeOptions);
  }

  /**
   * Adds the JJDoc preference tab.
   */
  protected void addJDocTab() {
    jJJDocOptions = new JJDocOptions(jFolder, jResource);
    jJJDocItem = new TabItem(jFolder, SWT.NONE);
    jJJDocItem.setText(Activator.getString("PropertyPage.JJDoc_options_Tab")); //$NON-NLS-1$
    jJJDocItem.setToolTipText(Activator.getString("PropertyPage.JJDoc_options_Tab_TT")); //$NON-NLS-1$
    jJJDocItem.setControl(jJJDocOptions);
  }

  /**
   * Adds the JTB preference tab.
   */
  protected void addJTBTab() {
    jJTBOptions = new JTBOptions(jFolder, jResource);
    jJTBItem = new TabItem(jFolder, SWT.NONE);
    jJTBItem.setText(Activator.getString("PropertyPage.JTB_options_Tab")); //$NON-NLS-1$
    jJTBItem.setToolTipText(Activator.getString("PropertyPage.JTB_options_Tab_TT")); //$NON-NLS-1$
    jJTBItem.setControl(jJTBOptions);
  }

  /**
   * @see IPreferencePage#performOk()
   */
  @Override
  public boolean performOk() {
    jJJRunOptions.performOk();
    jJJCCOptions.performOk();
    jJJTreeOptions.performOk();
    jJJDocOptions.performOk();
    jJTBOptions.performOk();

    // ask for rebuild (should check if an option has changed)
    try {
      final IProject proj = jResource.getProject();
      final MessageDialog dialog = new MessageDialog(
                                                     getShell(),
                                                     Activator.getString("PropertyPage.Ask_for_rebuild_title"), //$NON-NLS-1$
                                                     null,
                                                     Activator.getString("PropertyPage.Ask_for_rebuild_msg"), //$NON-NLS-1$
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
    jJJRunOptions.performDefaults();
    if (jJJCCOptions != null) {
      jJJCCOptions.performDefaults();
    }
    if (jJJTreeOptions != null) {
      jJJTreeOptions.performDefaults();
    }
    if (jJJDocOptions != null) {
      jJJDocOptions.performDefaults();
    }
    if (jJTBOptions != null) {
      jJTBOptions.performDefaults();
    }
  }
}