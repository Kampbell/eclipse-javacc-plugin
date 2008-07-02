package sf.eclipse.javacc.options;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IAdaptable;
//import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
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
    if (res != null) 
      project = res.getProject();

    isFile = (res != null && res.getType() == IResource.FILE);
    isJJ = (res != null && isFile && res.getName().endsWith("jj")); //$NON-NLS-1$
    isJJT = (res != null && isFile && res.getName().endsWith("jjt")); //$NON-NLS-1$
    isJTB = (res != null && isFile && res.getName().endsWith("jtb")); //$NON-NLS-1$
    
    // JJRuntime always present
    jjRun = new JJRuntimeOptions(folder, res, isFile);
    jjRunItem = new TabItem(folder, SWT.NONE);
    jjRunItem.setText(Activator.getString("JJPropertyPage.JavaCC_runtime_options_Tab")); //$NON-NLS-1$
    jjRunItem.setControl(jjRun);

    // For project
    addJCCTab();
    addJTreeTab();
    addJDocTab();
    addJTBTab();
    
    // Test a property to see if in need of a first initialization
    IScopeContext projectScope = new ProjectScope(project);
    IEclipsePreferences prefs = projectScope.getNode(IJJConstants.ID);
    if (prefs.get(RUNTIME_JAR, null) == null)
      performDefaults();
    return parent;
  }
  
  /**
   * Listens to CheckBox "Project Override"
   * and to CheckBox "Set JavaCC Nature"
   * and sets tab in coherence
   */
  public void propertyChange(PropertyChangeEvent event) {

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
    jjCC.performOk();
    jjTree.performOk();
    jjDoc.performOk();
    jtb.performOk();
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