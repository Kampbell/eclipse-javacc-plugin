package sf.eclipse.javacc.wizards;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.osgi.service.prefs.BackingStoreException;

import sf.eclipse.javacc.Activator;
import sf.eclipse.javacc.IJJConstants;
import sf.eclipse.javacc.JJNature;

/**
 * This wizard creates one file with the extension "jj", "jjt" or "jtb" based on files in templates directory
 * Referenced by plugin.xml <extension point="org.eclipse.ui.newWizards">
 * 
 * @author Remi Koutcherawy 2003-2009 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009
 */
@SuppressWarnings("restriction")
public class JJNewWizard extends NewElementWizard implements IJJConstants {

  // MMa 04/2009 : formatting revision ; changed jar names
  // MMa 02/2010 : formatting and javadoc revision ; differentiate static / non static files

  /** The wizard page */
  private JJNewJJPage fPage;

  /**
   * Constructor for JJNewWizard. Provides the image, DialogSetting, and title.
   */
  public JJNewWizard() {
    super();
    final ImageDescriptor id = Activator.getImageDescriptor("jjnew_wiz.gif"); //$NON-NLS-1$
    setDefaultPageImageDescriptor(id);
    setDialogSettings(Activator.getDefault().getDialogSettings());
    setWindowTitle(Activator.getString("JJNewWizard.creates_jj_example_file")); //$NON-NLS-1$
  }

  /**
   * Adds the page to the wizard and initialize it with selection.
   */
  @Override
  public void addPages() {
    fPage = new JJNewJJPage();
    addPage(fPage);
    fPage.init(getSelection());
  }

  /**
   * @see IWizard#performFinish()
   */
  @Override
  public boolean performFinish() {
    final String srcdir = fPage.getSrcDir();
    final String packageName = fPage.getPackage();
    final String fileName = fPage.getFileNameWithoutExtension();
    final String extension = fPage.getExtension();
    final boolean staticFlag = fPage.getStaticFalg();

    final IRunnableWithProgress op = new IRunnableWithProgress() {

      public void run(final IProgressMonitor monitor) {
        try {
          doFinish(srcdir, fileName, extension, packageName, staticFlag, monitor);
        } catch (final CoreException e) {
          e.printStackTrace();
          Activator.log(e.getMessage());
        } finally {
          monitor.done();
        }
      }
    };
    try {
      getContainer().run(true, false, op);
    } catch (final InterruptedException e) {
      return false;
    } catch (final InvocationTargetException e) {
      e.printStackTrace();
      Activator.log(e.getMessage());
      return false;
    }
    return true;
  }

  /**
   * Creates the file and open the editor on the file.
   * 
   * @param aSrcDir the source directory
   * @param aFileName the file name
   * @param aExtension the file extension
   * @param aPackageName the package name
   * @param aStaticFlag the static / non static flag
   * @param aMonitor the progress monitor
   * @throws CoreException if any problem
   */
  void doFinish(final String aSrcDir, final String aFileName, final String aExtension,
                final String aPackageName, final boolean aStaticFlag, final IProgressMonitor aMonitor)
                                                                                                      throws CoreException {
    aMonitor.beginTask(Activator.getString("JJNewWizard.Creating") + aFileName, 2); //$NON-NLS-1$

    // first: look for the srcDir+package 
    String resName;
    if (aPackageName.equals("")) { //$NON-NLS-1$
      resName = aSrcDir;
    }
    else {
      resName = aSrcDir + "/" + aPackageName.replace('.', '/'); //$NON-NLS-1$
    }
    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    final IResource res = root.findMember(new Path(resName));
    if (!res.exists() || !(res instanceof IContainer)) {
      Activator.log(Activator.getString("JJNewWizard.src_dir_doesnot_exist")); //$NON-NLS-1$
    }

    // second: create the file
    final IContainer container = (IContainer) res;
    final IFile file = container.getFile(new Path(aFileName + aExtension));
    try {
      final InputStream stream = openTemplateContentStream(aExtension, aPackageName, aStaticFlag);
      if (file.exists()) {
        file.setContents(stream, true, true, aMonitor);
      }
      else {
        file.create(stream, true, aMonitor);
      }
      stream.close();
    } catch (final IOException e) {
      Activator
               .log(Activator.getString("JJNewWizard.Creation_of") + aFileName + Activator.getString("JJNewWizard.failed") + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
    }
    aMonitor.worked(1);
    aMonitor.setTaskName(Activator.getString("JJNewWizard.Opening_file_for_editing")); //$NON-NLS-1$
    getShell().getDisplay().asyncExec(new Runnable() {

      public void run() {
        final IWorkbenchPage wpage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        try {
          IDE.openEditor(wpage, file, true);
        } catch (final PartInitException e) {
          Activator
                   .log(Activator.getString("JJNewWizard.opening_of") + file + Activator.getString("JJNewWizard.failed") + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
    });
    aMonitor.worked(1);

    // initialize properties do get automatically a full build
    final IProject pro = res.getProject();
    final IScopeContext projectScope = new ProjectScope(pro);
    final IEclipsePreferences prefs = projectScope.getNode(IJJConstants.ID);

    // tricky part: JTB needs -p packageName, only if there is a package name
    if (!aPackageName.equals("")) //$NON-NLS-1$
    {
      prefs.put(JTB_OPTIONS, "-p=" + aPackageName); //$NON-NLS-1$
    }

    // initializing properties do get automatically a full build
    if (prefs.get(RUNTIME_JJJAR, null) == null) {
      // use the jar(s) in the plugin
      String javaCCjarFile = "", jtbjarFile = ""; //$NON-NLS-1$ //$NON-NLS-2$
      final URL installURL = Activator.getDefault().getBundle().getEntry("/"); //$NON-NLS-1$
      try {
        final URL resolvedURL = org.eclipse.core.runtime.FileLocator.resolve(installURL);
        String home = org.eclipse.core.runtime.FileLocator.toFileURL(resolvedURL).getFile();
        // return string is "/C:/workspace/sf.eclipse.javacc/jtb132.jar"
        if (home.startsWith("/") && home.startsWith(":", 2)) { //$NON-NLS-1$ //$NON-NLS-2$
          home = home.substring(1);
        }
        javaCCjarFile = home + JAVACC_JAR_NAME;
        jtbjarFile = home + JTB_JAR_NAME;
      } catch (final IOException e) {
        e.printStackTrace();
      }
      prefs.put(RUNTIME_JJJAR, javaCCjarFile);
      prefs.put(RUNTIME_JTBJAR, jtbjarFile);
      prefs.put(SHOW_CONSOLE, "true"); //$NON-NLS-1$
      prefs.put(CLEAR_CONSOLE, "false"); //$NON-NLS-1$
      prefs.put(JJ_NATURE, "true"); //$NON-NLS-1$
      prefs.put(SUPPRESS_WARNINGS, "true"); //$NON-NLS-1$
      //      prefs.put(CHECK_SPELLING, "true"); //$NON-NLS-1$
      // set the nature directly
      JJNature.setJJNature(true, pro);

      try {
        prefs.flush();
      } catch (final BackingStoreException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Initializes file contents with a sample .jj or .jjt file.
   * 
   * @param aExtension "jj" or "jjt"
   * @param aPackageName the package name
   * @param aStaticFlag the static / non static flag
   * @return the file input stream
   */
  private InputStream openTemplateContentStream(final String aExtension, final String aPackageName,
                                                final boolean aStaticFlag) {
    final URL installURL = Activator.getDefault().getBundle().getEntry("/templates/"); //$NON-NLS-1$
    URL url;
    try {
      // the extension gives the right template
      final String filename = "new_file" + (aStaticFlag ? "_static" : "_non_static") + aExtension; //$NON-NLS-1$
      url = new URL(installURL, filename);
      // makeInputStream customizes the template
      return makeInputStream(url.openStream(), aPackageName);
    } catch (final MalformedURLException e) {
      e.printStackTrace();
    } catch (final IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * This could have been a FilterInputStream.
   * 
   * @param aInputStream the input stream
   * @param aPackageName the package name
   * @return stream the updated input stream
   */
  private InputStream makeInputStream(final InputStream aInputStream, final String aPackageName) {
    // MMa note : may be better to use a StringBuilder

    // read InputStream into byte buffer
    byte buffer[];
    try {
      buffer = Util.getInputStreamAsByteArray(aInputStream, -1);
      aInputStream.close();
    } catch (final IOException e) {
      Activator.log(Activator.getString("JJNewWizard.Reading_failed") + e.getMessage()); //$NON-NLS-1$
      return null;
    }

    // create a string from the byte buffer
    String str = new String(buffer);

    // instantiate the template file
    if (aPackageName.equals("")) { //$NON-NLS-1$
      // default package ? remove all
      str = str.replaceAll("<\\?package.*\\?>", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }
    else {
      // add lines
      str = str.replaceAll("<\\?package_declare\\?>", "package " + aPackageName + ";\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      str = str.replaceAll("<\\?package\\?>", aPackageName + "."); //$NON-NLS-1$ //$NON-NLS-2$
    }

    // return InputStream
    buffer = str.getBytes();
    final ByteArrayInputStream baif = new ByteArrayInputStream(buffer);
    return baif;
  }

  /**
   * @see NewElementWizard#finishPage(IProgressMonitor)
   */
  @SuppressWarnings("unused")
  @Override
  protected void finishPage(final IProgressMonitor aMonitor) throws InterruptedException, CoreException {
    // nothing done here
  }

  /**
   * @see NewElementWizard#getCreatedElement()
   */
  @Override
  public IJavaElement getCreatedElement() {
    return null;
  }
}