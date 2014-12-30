package sf.eclipse.javacc.options;

import static sf.eclipse.javacc.base.IConstants.BUILDER_ID;
import static sf.eclipse.javacc.base.IConstants.PLUGIN_QN;
import static sf.eclipse.javacc.base.IConstants.RUNTIME_JJJAR;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.dialogs.PropertyPage;

import sf.eclipse.javacc.base.AbstractActivator;

/**
 * The Property page class for JavaCC projects or files. Enables setting of JavaCC options for project or jj
 * file.<br>
 * Referenced by plugin.xml.<br>
 * <extension point="org.eclipse.ui.propertyPages">.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014
 */
public class PropertyPageJJ extends PropertyPage {

  // MMa 02/2010 : formatting and javadoc revision ; added some properties
  // MMa 08/2011 : renamed
  // MMa 10/2012 : renamed

  /** The current folder */
  protected TabFolder     jFolder;
  /** The current global item */
  protected TabItem       jRunItem;
  /** The current JavaCC item */
  protected TabItem       jjItem;
  /** The current JJTree item */
  protected TabItem       jjTreeItem;
  /** The current JJDoc item */
  protected TabItem       jjDocItem;
  /** The current JTB item */
  protected TabItem       jtbItem;

  /** The current global options */
  protected GlobalOptions jRunOptions;
  /** The current JavaCC options */
  protected JavaCCOptions jjOptions;
  /** The current JJTree options */
  protected JJTreeOptions jjTreeOptions;
  /** The current JJDoc options */
  protected JJDocOptions  jjDocOptions;
  /** The current JTB options */
  protected JTBOptions    jtbOptions;

  /** The current resource */
  protected IResource     jResource;
  /** The current project */
  protected IProject      jProject;

  /**
   * Creates contents (called from plugin.xml).
   * <p>
   * {@inheritDoc}
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
    jRunOptions = new GlobalOptions(jFolder, jResource);
    jRunItem = new TabItem(jFolder, SWT.NONE);
    jRunItem.setText(AbstractActivator.getMsg("PropPage.Runtime_options_Tab")); //$NON-NLS-1$
    jRunItem.setToolTipText(AbstractActivator.getMsg("PropPage.Runtime_options_Tab_TT")); //$NON-NLS-1$
    jRunItem.setControl(jRunOptions);

    // for project
    addJCCTab();
    addJTreeTab();
    addJDocTab();
    addJTBTab();

    // test a property to see if in need of a first initialization
    final IEclipsePreferences prefs = new ProjectScope(jProject).getNode(PLUGIN_QN);
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
    jjOptions = new JavaCCOptions(jFolder, jResource);
    jjItem = new TabItem(jFolder, SWT.NONE);
    jjItem.setText(AbstractActivator.getMsg("PropPage.JavaCC_options_Tab")); //$NON-NLS-1$
    jjItem.setToolTipText(AbstractActivator.getMsg("PropPage.JavaCC_options_Tab_TT")); //$NON-NLS-1$
    jjItem.setControl(jjOptions);
  }

  /**
   * Adds the JJTree preference tab.
   */
  protected void addJTreeTab() {
    jjTreeOptions = new JJTreeOptions(jFolder, jResource);
    jjTreeItem = new TabItem(jFolder, SWT.NONE);
    jjTreeItem.setText(AbstractActivator.getMsg("PropPage.JJTree_options_Tab")); //$NON-NLS-1$
    jjTreeItem.setToolTipText(AbstractActivator.getMsg("PropPage.JJTree_options_Tab_TT")); //$NON-NLS-1$
    jjTreeItem.setControl(jjTreeOptions);
  }

  /**
   * Adds the JJDoc preference tab.
   */
  protected void addJDocTab() {
    jjDocOptions = new JJDocOptions(jFolder, jResource);
    jjDocItem = new TabItem(jFolder, SWT.NONE);
    jjDocItem.setText(AbstractActivator.getMsg("PropPage.JJDoc_options_Tab")); //$NON-NLS-1$
    jjDocItem.setToolTipText(AbstractActivator.getMsg("PropPage.JJDoc_options_Tab_TT")); //$NON-NLS-1$
    jjDocItem.setControl(jjDocOptions);
  }

  /**
   * Adds the JTB preference tab.
   */
  protected void addJTBTab() {
    jtbOptions = new JTBOptions(jFolder, jResource);
    jtbItem = new TabItem(jFolder, SWT.NONE);
    jtbItem.setText(AbstractActivator.getMsg("PropPage.JTB_options_Tab")); //$NON-NLS-1$
    jtbItem.setToolTipText(AbstractActivator.getMsg("PropPage.JTB_options_Tab_TT")); //$NON-NLS-1$
    jtbItem.setControl(jtbOptions);
  }

  /** {@inheritDoc} */
  @Override
  public boolean performOk() {
    jRunOptions.performOk();
    jjOptions.performOk();
    jjTreeOptions.performOk();
    jjDocOptions.performOk();
    jtbOptions.performOk();

    // ask for rebuild (should check if an option has changed)
    try {
      final IProject proj = jResource.getProject();
      final MessageDialog dialog = new MessageDialog(getShell(),
                                                     AbstractActivator.getMsg("PropPage.Ask_for_rebuild_title"), //$NON-NLS-1$
                                                     null, AbstractActivator.getMsg("PropPage.Ask_for_rebuild_msg"), //$NON-NLS-1$
                                                     MessageDialog.QUESTION, new String[] {
                                                         IDialogConstants.YES_LABEL,
                                                         IDialogConstants.NO_LABEL,
                                                         IDialogConstants.CANCEL_LABEL }, 2);
      final int res = dialog.open();
      if (res == Window.OK) {
        // OK clean build
        proj.build(IncrementalProjectBuilder.CLEAN_BUILD, BUILDER_ID, null, null);
      }
      else if (res != 1) {
        // Cancel abort
        return false;
      }
      // "OK" or "NO clean" proceeds to set options
    } catch (final CoreException e) {
      AbstractActivator.logBug(e);
      return false;
    }
    return super.performOk();
  }

  /** {@inheritDoc} */
  @Override
  protected void performDefaults() {
    super.performDefaults();
    jRunOptions.performDefaults();
    if (jjOptions != null) {
      jjOptions.performDefaults();
    }
    if (jjTreeOptions != null) {
      jjTreeOptions.performDefaults();
    }
    if (jjDocOptions != null) {
      jjDocOptions.performDefaults();
    }
    if (jtbOptions != null) {
      jtbOptions.performDefaults();
    }
  }
}