package sf.eclipse.javacc.options;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.dialogs.PropertyPage;

import sf.eclipse.javacc.Activator;
import sf.eclipse.javacc.IJJConstants;

/**
 * The Property page class for JavaCC projects or files
 * Enables setting of JavaCC options for project or jj file
 * Referenced by plugin.xml
 *  <extension point="org.eclipse.ui.propertyPages">
 *  
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */
public class JJPropertyPage extends PropertyPage
  implements IPropertyChangeListener, IJJConstants {
    
  protected TabFolder folder;
  protected TabItem jjRunItem;
  protected TabItem jjCCItem;
  protected TabItem jjTreeItem;
  protected TabItem jjDocItem;
  protected TabItem jtbItem;
  
  protected JJRuntimeOptions jjRun;
  protected JJCCOptions jjCC;
  protected JJTreeOptions jjTree;
  protected JJDocOptions jjDoc;
  protected JTBOptions jtb;
  
  protected boolean isProject;
  protected boolean isProjectOverride;
  protected boolean isFile;
  protected boolean isJJ;
  protected boolean isJJT;
  protected boolean isJTB;
  
  protected IResource res;
  protected IProject project;

  /**
   * Called from plugin.xml, creates contents
   * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
   */
  protected Control createContents(Composite parent) {
    // Creates a TabFolder
    folder = new TabFolder(parent, SWT.NONE);
    GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
    folder.setLayoutData(gd);
    
    // Reads config from IResource
    IAdaptable ia = getElement();
    res = (IResource) ia.getAdapter(IResource.class);
    isProject = (res != null && res.getType() == IResource.PROJECT);
    try {
      if (res != null) {
        project = res.getProject();
        String prop = project.getPersistentProperty(QN_PROJECT_OVERRIDE);
        isProjectOverride = ("true").equals(prop);//$NON-NLS-1$
      }
    } catch (CoreException e) {
      // Nothing to do, we don't need to bother the user
    }
    isFile = (res != null && res.getType() == IResource.FILE);
    isJJ = (res != null && isFile && res.getName().endsWith("jj")); //$NON-NLS-1$
    isJJT = (res != null && isFile && res.getName().endsWith("jjt")); //$NON-NLS-1$
    isJTB = (res != null && isFile && res.getName().endsWith("jtb")); //$NON-NLS-1$
    
    // JJRuntime always present
    jjRun = new JJRuntimeOptions(folder, res, isFile);
    jjRunItem = new TabItem(folder, SWT.NONE);
    jjRunItem.setText(Activator.getString("JJPropertyPage.JavaCC_runtime_options_Tab")); //$NON-NLS-1$
    jjRunItem.setControl(jjRun);
    
    // To deal with reconfiguration for project override and Nature
    jjRun.setPropertyChangeListener(this); 

    // For files
    if (isFile) {
      if (!isProjectOverride) {
        if (isJJ)
          addJCCTab();
        else if (isJJT)
          addJTreeTab();
        else if (isJTB)
          addJTBTab();
        addJDocTab();
      }
    }
    
    // For project
    if (isProject) {
      addJCCTab();
      addJTreeTab();
      addJDocTab();
      addJTBTab();
    }
    // Test a property to see if in need of a first initialization
    try {
      if (project.getPersistentProperty(QN_PROJECT_OVERRIDE) == null)
        performDefaults();
    } catch (CoreException e) {
      // Nothing to do
    }
    return parent;
  }
  
  /**
   * Listens to CheckBox "Project Override"
   * and to CheckBox "Set JavaCC Nature"
   * and sets tab in coherence
   */
  public void propertyChange(PropertyChangeEvent event) {
    Boolean b = (Boolean) event.getNewValue();
    BooleanFieldEditor field = (BooleanFieldEditor) event.getSource();
    String name = field.getPreferenceName();

    if (name.equals(PROJECT_OVERRIDE)) {
      isProjectOverride = b.booleanValue();
      // For files only
      if (isFile) {
        if (!isProjectOverride) {
          if (isJJ)
            addJCCTab();
          else if (isJJT)
            addJTreeTab();
          else if (isJTB)
            addJTBTab();
          addJDocTab();
        } else {
          if (isJJ)
            jjCCItem.dispose();
          else if (isJJT)
            jjTreeItem.dispose();
          else if (isJTB)
            jtbItem.dispose();
          jjDocItem.dispose();
        }
      }
    }
  }
  
  /**
   * Convenient methods
   */
  protected void addJCCTab() {
    jjCC = new JJCCOptions(folder, res);
    jjCCItem = new TabItem(folder, SWT.NONE);
    jjCCItem.setText(Activator.getString("JJPropertyPage.JavaCC_options_Tab")); //$NON-NLS-1$
    jjCCItem.setControl(jjCC);
  }
  protected void addJTreeTab() {
    jjTree = new JJTreeOptions(folder, res);
    jjTreeItem = new TabItem(folder, SWT.NONE);
    jjTreeItem.setText(Activator.getString("JJPropertyPage.JJTree_options_Tab")); //$NON-NLS-1$
    jjTreeItem.setControl(jjTree);
  }
  protected void addJDocTab() {
    jjDoc = new JJDocOptions(folder, res);
    jjDocItem = new TabItem(folder, SWT.NONE);
    jjDocItem.setText(Activator.getString("JJPropertyPage.JJDoc_options_Tab")); //$NON-NLS-1$
    jjDocItem.setControl(jjDoc);
  }
  
  protected void addJTBTab() {
    jtb = new JTBOptions(folder, res);
    jtbItem = new TabItem(folder, SWT.NONE);
    jtbItem.setText(Activator.getString("JJPropertyPage.JTB_options_Tab")); //$NON-NLS-1$
    jtbItem.setControl(jtb);
  }

  /**
   * @see org.eclipse.jface.preference.IPreferencePage#performOk()
   */
  public boolean performOk() {
    jjRun.performOk();
    if (jjCC != null) jjCC.performOk();
    if (jjTree != null) jjTree.performOk();
    if (jjDoc != null) jjDoc.performOk();
    if (jtb != null) jtb.performOk();
    return super.performOk();    
  }

  /**
   * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
   */
  protected void performDefaults() {
    super.performDefaults();
    jjRun.performDefaults();
    if (jjCC != null) jjCC.performDefaults();
    if (jjTree != null) jjTree.performDefaults();
    if (jjDoc != null) jjDoc.performDefaults();
    if (jtb != null) jtb.performDefaults();
  }  
}