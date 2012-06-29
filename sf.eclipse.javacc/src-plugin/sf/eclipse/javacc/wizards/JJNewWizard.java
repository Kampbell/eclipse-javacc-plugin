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
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.osgi.service.prefs.BackingStoreException;

import sf.eclipse.javacc.base.IJJConstants;
import sf.eclipse.javacc.head.Activator;
import sf.eclipse.javacc.head.JJNature;

/**
 * This wizard creates one file with the extension "jj", "jjt" or "jtb" based on files in templates directory
 * Referenced by plugin.xml <extension point="org.eclipse.ui.newWizards">
 * 
 * @author Remi Koutcherawy 2003-2009 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012
 * @author Bill Fenlason 2012
 */
@SuppressWarnings("restriction")
public class JJNewWizard extends NewElementWizard implements IJJConstants {

  // MMa 04/2009 : formatting revision ; changed jar names
  // MMa 02/2010 : formatting and javadoc revision ; differentiate static / non static files ;
  //             : removed SHOW_CONSOLE preference ;
  // ... ....... : fixed NPE and added different checks for SR 2956977
  // MMa 02/2011 : fixed bug #3157017 (incorrect package handling)
  // BF  06/2012 : added NON-NLS tag

  /** The wizard page */
  private JJNewJJPage jPage;

  /**
   * Constructor for JJNewWizard. Provides the image, DialogSetting, and title.
   */
  public JJNewWizard() {
    super();
    final ImageDescriptor id = Activator.getImageDescriptor("jjnew_wiz.gif"); //$NON-NLS-1$
    setDefaultPageImageDescriptor(id);
    setDialogSettings(Activator.getDefault().getDialogSettings());
    setWindowTitle(Activator.getString("JJNewWizard.Creates_jj_example_file")); //$NON-NLS-1$
  }

  /**
   * Adds the page to the wizard and initialize it with selection.
   */
  @Override
  public void addPages() {
    jPage = new JJNewJJPage();
    addPage(jPage);
    jPage.init(getSelection());
  }

  /** {@inheritDoc} */
  @Override
  public boolean performFinish() {
    final String srcdir = jPage.getSrcDir();
    final String packageName = jPage.getPackage();
    final String fileName = jPage.getFileNameWithoutExtension();
    final String extension = jPage.getExtension();
    final boolean staticFlag = jPage.getStaticFalg();

    final IRunnableWithProgress op = new IRunnableWithProgress() {

      @Override
      public void run(final IProgressMonitor monitor) {
        try {
          doFinish(srcdir, fileName, extension, packageName, staticFlag, monitor);
        } catch (final CoreException e) {
          e.printStackTrace();
          Activator.logErr("doFinish problem : " + e.getMessage()); //$NON-NLS-1$
        } finally {
          monitor.done();
        }
      }
    };
    try {
      getContainer().run(true, false, op);
    } catch (final InterruptedException e) {
      e.printStackTrace();
      Activator.logErr("getContainer IE problem : " + e.getMessage()); //$NON-NLS-1$
      return false;
    } catch (final InvocationTargetException e) {
      e.printStackTrace();
      Activator.logErr("getContainer ITE problem : " + e.getMessage()); //$NON-NLS-1$
      return false;
    }
    return true;
  }

  /**
   * Creates the file and open the editor on the file.
   * 
   * @param aSrcDir - the source directory
   * @param aFileName - the file name
   * @param aExtension - the file extension
   * @param aPackageName - the package name
   * @param aStaticFlag - the static / non static flag
   * @param aMonitor - the progress monitor
   * @throws CoreException - if any problem
   */
  void doFinish(final String aSrcDir, final String aFileName, final String aExtension,
                final String aPackageName, final boolean aStaticFlag, final IProgressMonitor aMonitor)
                                                                                                      throws CoreException {

    aMonitor.beginTask(Activator.getString("JJNewWizard.Creating") + " " + aFileName + aExtension, 2); //$NON-NLS-1$ //$NON-NLS-2$

    // first: look for the srcDir/package 
    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    if (root == null || !root.exists()) {
      final String msg = Activator.getString("JJNewWizard.Root_problem"); //$NON-NLS-1$
      new Exception(msg).printStackTrace();
      Activator.logErr(msg + " (" + root + ")"); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }
    String resName = aSrcDir;
    if (aPackageName != null && !"".equals(aPackageName)) { //$NON-NLS-1$
      resName += "/" + aPackageName.replace('.', '/'); //$NON-NLS-1$
    }
    final IResource res = root.findMember(new Path(resName));
    if (res == null || !res.exists() || !(res instanceof IContainer)) {
      final String msg = Activator.getString("JJNewWizard.Srcpkgdir_doesnot_exist"); //$NON-NLS-1$
      new Exception(msg).printStackTrace();
      Activator.logErr(msg + " (" + resName + ")"); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }
    // second: create the file
    final IContainer container = (IContainer) res;
    final String fileNameWithExt = aFileName + aExtension;
    final Path path = new Path(fileNameWithExt);
    final IFile file = container.getFile(path);
    try {
      final InputStream stream = openTemplateContentStream(aExtension, aPackageName, aStaticFlag);
      if (stream == null) {
        // log is within openTemplateContentStreams
        return;
      }
      if (file.exists()) {
        file.setContents(stream, true, true, aMonitor);
        Activator.logInfo("File " + file + " updated"); //$NON-NLS-1$ //$NON-NLS-2$
      }
      else {
        file.create(stream, true, aMonitor);
        Activator.logInfo("File " + file + " created"); //$NON-NLS-1$ //$NON-NLS-2$
      }
      stream.close();
    } catch (final IOException e) {
      e.printStackTrace();
      Activator.logErr(Activator.getString("JJNewWizard.Creation_of") + " (" + file + ") " + Activator.getString("JJNewWizard.failed") + " : " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
      return;
    }
    aMonitor.worked(1);
    aMonitor.setTaskName(Activator.getString("JJNewWizard.Opening_file_for_editing") + " (" + file + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    final Shell wizShell = getShell();
    if (wizShell == null) {
      Activator.logErr(Activator.getString("JJNewWizard.Wizardshell_problem")); //$NON-NLS-1$ 
      return;
    }
    wizShell.getDisplay().asyncExec(new Runnable() {

      @Override
      public void run() {
        final IWorkbenchWindow iww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (iww == null) {
          Activator.logErr(Activator.getString("JJNewWizard.Workbenchwindow_problem")); //$NON-NLS-1$ 
          return;
        }
        final IWorkbenchPage wpage = iww.getActivePage();
        if (wpage == null) {
          Activator.logInfo(Activator.getString("JJNewWizard.Activepage_null")); //$NON-NLS-1$ 
          return;
        }
        try {
          IDE.openEditor(wpage, file, true);
        } catch (final PartInitException e) {
          e.printStackTrace();
          Activator.logErr(Activator.getString("JJNewWizard.Opening_of") + " " + file + " " + Activator.getString("JJNewWizard.failed") + " : " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        }
      }
    });
    aMonitor.worked(1);

    // initialize properties to get automatically a full build
    final IProject project = res.getProject();
    // force the nature in case the project has just been created
    JJNature.setJJNature(true, project);
    final IScopeContext projectScope = new ProjectScope(project);
    final IEclipsePreferences prefs = projectScope.getNode(IJJConstants.ID);

    if (prefs == null) {
      Activator.logErr(Activator.getString("JJNewWizard.Prefs_null")); //$NON-NLS-1$ 
      return;
    }

    if (prefs.get(RUNTIME_JJJAR, null) == null) {
      // initializing properties do get automatically a full build
      Activator.logInfo(Activator.getString("JJNewWizard.Initializing_preferences")); //$NON-NLS-1$ 
      // use the jar(s) in the plugin
      String javaCCJarFile = ""; //$NON-NLS-1$
      String jtbJarFile = ""; //$NON-NLS-1$
      final URL installURL = Activator.getDefault().getBundle().getEntry("/"); //$NON-NLS-1$
      try {
        final URL resolvedURL = FileLocator.resolve(installURL);
        String home = FileLocator.toFileURL(resolvedURL).getFile();
        // return string is "/C:/workspace/sf.eclipse.javacc/"
        if (home.startsWith("/") && home.startsWith(":", 2)) { //$NON-NLS-1$ //$NON-NLS-2$
          home = home.substring(1);
        }
        javaCCJarFile = home + JAVACC_JAR_NAME;
        jtbJarFile = home + JTB_JAR_NAME;
      } catch (final IOException e) {
        e.printStackTrace();
        Activator.logErr(Activator.getString("JJNewWizard.Rootbundle_notfound") + " : " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
        return;
      }
      prefs.put(RUNTIME_JJJAR, javaCCJarFile);
      prefs.put(RUNTIME_JTBJAR, jtbJarFile);
      prefs.put(CLEAR_CONSOLE, "false"); //$NON-NLS-1$
      prefs.put(JJ_NATURE, "true"); //$NON-NLS-1$
      prefs.put(SUPPRESS_WARNINGS, "false"); //$NON-NLS-1$
    }

    try {
      prefs.flush();
    } catch (final BackingStoreException e) {
      e.printStackTrace();
      Activator.logErr(Activator.getString("JJNewWizard.Backingstore_problem") + " : " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }
  }

  /**
   * Initializes file contents with a sample .jj or .jjt file.
   * 
   * @param aExtension - "jj" or "jjt"
   * @param aPackageName - the package name
   * @param aStaticFlag - the static / non static flag
   * @return the file input stream
   */
  private InputStream openTemplateContentStream(final String aExtension, final String aPackageName,
                                                final boolean aStaticFlag) {
    // the extension and the flag give the right template
    final String filename = "New_file" + (aStaticFlag ? "_static" : "_non_static") + aExtension; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    try {
      final URL installURL = Activator.getDefault().getBundle().getEntry("/templates/"); //$NON-NLS-1$
      final URL url = new URL(installURL, filename);
      // makeInputStream customizes the template
      return makeInputStream(url.openStream(), aPackageName);
    } catch (final MalformedURLException e) {
      e.printStackTrace();
    } catch (final IOException e) {
      e.printStackTrace();
    }
    Activator.logErr(Activator.getString("JJNewWizard.Template_problem") + " (" + filename + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    return null;
  }

  /**
   * This could have been a FilterInputStream.
   * 
   * @param aInputStream - the input stream
   * @param aPackageName - the package name
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
      Activator.logErr(Activator.getString("JJNewWizard.Reading_failed") + " : " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
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
      str = str.replaceAll("<\\?package_decl\\?>", "package " + aPackageName + ";\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      str = str.replaceAll("<\\?package_dot\\?>", aPackageName + "."); //$NON-NLS-1$ //$NON-NLS-2$ 
      str = str.replaceAll("<\\?package\\?>", aPackageName); //$NON-NLS-1$ 
    }

    // return InputStream
    buffer = str.getBytes();
    final ByteArrayInputStream baif = new ByteArrayInputStream(buffer);
    return baif;
  }

  /** {@inheritDoc} */
  @Override
  protected void finishPage(@SuppressWarnings("unused") final IProgressMonitor aMonitor) /*throws InterruptedException, CoreException*/{
    // nothing done here
  }

  /** {@inheritDoc} */
  @Override
  public IJavaElement getCreatedElement() {
    return null;
  }
}