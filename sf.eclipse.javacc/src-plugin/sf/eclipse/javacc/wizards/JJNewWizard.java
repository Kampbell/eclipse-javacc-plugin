package sf.eclipse.javacc.wizards;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import sf.eclipse.javacc.Activator;
import sf.eclipse.javacc.IJJConstants;

/**
 * This wizard creates one file with the extension "jj", "jjt" or "jtb" based on
 * files in templates directory 
 * Referenced by plugin.xml <extension point="org.eclipse.ui.newWizards">
 * 
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */
public class JJNewWizard extends NewElementWizard {
  private JJNewJJPage fPage;

  /**
   * Constructor for JJNewWizard. 
   * Provides the image, DialogSetting, and title.
   */
  public JJNewWizard() {
    super();
    ImageDescriptor id = Activator.getImageDescriptor("jjnew_wiz.gif");
    setDefaultPageImageDescriptor(id);
    setDialogSettings(Activator.getDefault().getDialogSettings());
    setWindowTitle("Creates .jj, .jjt or .jtb example file.");
  }

  /**
   * Add the page to the wizard and initialise it with selection
   */
  public void addPages() {
    fPage = new JJNewJJPage();
    addPage(fPage);
    fPage.init(getSelection());
  }

  /*(non-Javadoc) 
   * @see org.eclipse.jface.wizard.IWizard#performFinish()
   */
  public boolean performFinish() {
    final String srcdir = fPage.getSrcDir();
    final String packageName = fPage.getPackage();
    final String fileName = fPage.getFileNameWithoutExtension();
    final String extension = fPage.getExtension();
    
    IRunnableWithProgress op = new IRunnableWithProgress() {
      public void run(IProgressMonitor monitor) throws InvocationTargetException {
        try {
          doFinish(srcdir, fileName, extension, packageName, monitor);
        } catch (CoreException e) {
          Activator.log(e.getMessage());
        } finally {
          monitor.done();
        }
      }
    };
    try {
      getContainer().run(true, false, op);
    } catch (InterruptedException e) {
      return false;
    } catch (InvocationTargetException e) {
      e.printStackTrace();
      Activator.log(e.getMessage());
      return false;
    }
    return true;
  }
  /**
   * The worker method. 
   * Create the file and open the editor on the file.
   */
  void doFinish(String srcDir, String fileName, String extension,
      String packageName, IProgressMonitor monitor) throws CoreException {
    monitor.beginTask(Activator.getString("JJNewWizard.Creating") + fileName, 2); //$NON-NLS-1$
    
    // first look for the srcDir+package 
    String resName;
    if (packageName.equals(""))
      resName = srcDir;
    else
      resName = srcDir+"/"+packageName.replace('.','/');
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IResource res = root.findMember(new Path(resName));
    if (!res.exists() || !(res instanceof IContainer)) {
      Activator.log("SrcDir does not exist.");
    }
    
    // second create the file
    IContainer container = (IContainer) res;
    final IFile file = container.getFile(new Path(fileName+extension));
    try {
      InputStream stream = openTemplateContentStream(extension, packageName);
      if (file.exists()) {
        file.setContents(stream, true, true, monitor);
      } else {
        file.create(stream, true, monitor);
      }
      stream.close();
    } catch (IOException e) {
      Activator.log("Creation of "+fileName+" failed. "+e.getMessage());
    }
    monitor.worked(1);
    monitor.setTaskName(Activator.getString("JJNewWizard.Opening_file_for_editing")); //$NON-NLS-1$
    getShell().getDisplay().asyncExec(new Runnable() {
      public void run() {
        IWorkbenchPage wpage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        try {
          IDE.openEditor(wpage, file, true);
        } catch (PartInitException e) {
          Activator.log("Opening of "+file+" failed. "+e.getMessage());
        }
      }
    });
    monitor.worked(1);
    
    // Tricky part JTB needs -p packageName
    res.getProject().setPersistentProperty(IJJConstants.QN_JTB_OPTIONS,"-p="+packageName);
  }

  /**
   * Initialize file contents with a sample .jj or .jjt file.
   * @param extension "jj" or "jjt"
   */
  private InputStream openTemplateContentStream(String extension, String packageName) {
    URL installURL = Activator.getDefault().getBundle().getEntry("/templates/"); //$NON-NLS-1$
    URL url;
    try {
      // the extension gives the right template
      String filename = Activator.getString("JJNewWizard.new_file") + extension; //$NON-NLS-1$
      url = new URL(installURL, filename);
      // makeInputStream customize the template
      return makeInputStream(url.openStream(), packageName);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * This could have been a FilterInputStream
   * @param stream
   * @param packageName
   * @return stream
   */
  private InputStream makeInputStream(InputStream stream, String packageName) {
    // read InputStream into buffer
    byte buffer[];
    try {
      buffer = Util.getInputStreamAsByteArray(stream, -1);
      stream.close();      
    } catch (IOException e) {
      Activator.log("Reading failed "+e.getMessage());
      return null;
    }
    
    // make a String str from buffer
    String str = new String(buffer);
    
    // instanciate template
    if (packageName.equals("")){
      // default package ? remove all
      str = str.replaceAll("<\\?package.*\\?>", ""); 
    }
    else {
      // add lines
      str = str.replaceAll("<\\?package_declare\\?>", "package "+packageName+";\n");
      str = str.replaceAll("<\\?package\\?>", packageName+".");
    }
    
    // return InputStream made from str
    buffer = str.getBytes();
    ByteArrayInputStream baif = new ByteArrayInputStream(buffer);
    return baif;
  }

  protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
  }

  public IJavaElement getCreatedElement() {
    return null;
  }
}