package sf.eclipse.javacc.options;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.dialogs.PropertyPage;

import sf.eclipse.javacc.IConstants;
import sf.eclipse.javacc.JavaccPlugin;

/**
 * The Property page class for JavaCC projects or files
 * Enables setting of JavaCC options for project or jj file
 * 
 * @author Remi Koutcherawy 2003 
 * GPL Licence www.gnu.org/licenses/gpl.txt */
public class JJPropertyPage extends PropertyPage
  implements IPropertyChangeListener, IConstants {
    
  protected TabFolder folder;
  protected TabItem jjRunItem;
  protected TabItem jjCCItem;
  protected TabItem jjTreeItem;
  protected TabItem jjDocItem;
  
  protected JJRuntimeOptions jjRun;
  protected JJCCOptions jjCC;
  protected JJTreeOptions jjTree;
  protected JJDocOptions jjDoc;
  
  protected boolean isProject;
  protected boolean isProjectOverride;
  protected boolean isFile;
  protected boolean isJJ;
  
  protected IResource res;
  protected IProject project;

  /**
   * Called from plugin.xml, creates contents
   * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
   */
  protected Control createContents(Composite parent) {
    // Creates a TabFolder
    folder = new TabFolder(parent, SWT.NONE);
    folder.setLayout(new TabFolderLayout());  
    folder.setLayoutData(new GridData(GridData.FILL_BOTH));
    
    // Reads config from IResource
    IAdaptable ia = getElement();
    res = (IResource) ia.getAdapter(IResource.class);
    isProject = (res != null && res.getType() == IResource.PROJECT);
    try {
      if (res != null) {
        project = res.getProject();
        isProjectOverride = ("true").equals((project.getPersistentProperty( //$NON-NLS-1$
          QN_PROJECT_OVERRIDE)));
      }
    } catch (CoreException e) {}
    isFile = (res != null && res.getType() == IResource.FILE);
    isJJ = (res != null && isFile && res.getName().endsWith("jj")); //$NON-NLS-1$
    
    // JJRuntime always present
    jjRun = new JJRuntimeOptions(folder, res);
    jjRunItem = new TabItem(folder, SWT.NONE);
    jjRunItem.setText(JavaccPlugin.getResourceString("JJPropertyPage.JavaCC_runtime_options_Tab")); //$NON-NLS-1$
    jjRunItem.setControl(jjRun);
    
    // To deal with reconfiguration for project override and Nature
    jjRun.setPropertyChangeListener(this); 

    // For files
    if (isFile) {
      if (!isProjectOverride) {
        if (isJJ)
          addJCCTab();
        else
          addJTreeTab();
        addJDocTab();
      }
    }
    
    // For project
    if (isProject) {
      addJCCTab();
      addJTreeTab();
      addJDocTab();
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
          else
            addJTreeTab();
          addJDocTab();
        } else {
          if (isJJ)
            jjCCItem.dispose();
          else
            jjTreeItem.dispose();
          jjDocItem.dispose();
        }
      }
    }
//    if (field.getPreferenceName().equals(JJ_NATURE_NAME)) {
//      boolean hasJJNature = b.booleanValue();
//      JJNature.setJJNature(hasJJNature, project);
//    }
  }
  
  /**
   * Convenient methods
   */
  protected void addJCCTab() {
    jjCC = new JJCCOptions(folder, res);
    jjCCItem = new TabItem(folder, SWT.NONE);
    jjCCItem.setText(JavaccPlugin.getResourceString("JJPropertyPage.JavaCC_options_Tab")); //$NON-NLS-1$
    jjCCItem.setControl(jjCC);
  }
  protected void addJTreeTab() {
    jjTree = new JJTreeOptions(folder, res);
    jjTreeItem = new TabItem(folder, SWT.NONE);
    jjTreeItem.setText(JavaccPlugin.getResourceString("JJPropertyPage.JJTree_options_Tab")); //$NON-NLS-1$
    jjTreeItem.setControl(jjTree);
  }
  protected void addJDocTab() {
    jjDoc = new JJDocOptions(folder, res);
    jjDocItem = new TabItem(folder, SWT.NONE);
    jjDocItem.setText(JavaccPlugin.getResourceString("JJPropertyPage.JJDoc_options_Tab")); //$NON-NLS-1$
    jjDocItem.setControl(jjDoc);
  }

  /**
   * @see org.eclipse.jface.preference.IPreferencePage#performOk()
   */
  public boolean performOk() {
    jjRun.performOk();
    if (jjCC != null) jjCC.performOk();
    if (jjTree != null) jjTree.performOk();
    if (jjDoc != null) jjDoc.performOk();
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
  }  

  /**
   *  Unit test
   * NB configure VM arguments :
   * -Djava.library.path=C:\eclipse\plugins\org.eclipse.swt.win32_2.0.2\os\win32\x86
   */
  public static void main(String[] args) {
    Display display = new Display();
    Shell shell = new Shell(display);
    FillLayout fillLayout = new FillLayout ();
    shell.setLayout (fillLayout);
    
    JJPropertyPage jjo = new JJPropertyPage();
//    jjo.setElement(new org.eclipse.ui.internal.dialogs.FakeAction("l", null)); //$NON-NLS-1$
    jjo.createContents(shell);

    shell.pack();
    shell.open();
    while (!shell.isDisposed()){
      if(!display.readAndDispatch())
        display.sleep();
    }
  }
}