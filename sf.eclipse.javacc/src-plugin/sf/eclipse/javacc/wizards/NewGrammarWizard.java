package sf.eclipse.javacc.wizards;

import static sf.eclipse.javacc.base.IConstants.*;

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
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.osgi.service.prefs.BackingStoreException;

import sf.eclipse.javacc.base.AbstractActivator;
import sf.eclipse.javacc.base.Compiler;
import sf.eclipse.javacc.base.Nature;

/**
 * This wizard creates one file with the extension "jj", "jjt" or "jtb" based on the files in the 'templates'
 * directory.<br>
 * Referenced by plugin.xml.<br>
 * <extension point="org.eclipse.ui.newWizards"><br>
 * 
 * @author Remi Koutcherawy 2003-2009 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 * @author Bill Fenlason 2012
 */
public class NewGrammarWizard extends Wizard implements INewWizard {

  // MMa 04/2009 : formatting revision ; changed jar names
  // MMa 02/2010 : formatting and javadoc revision ; differentiate static / non static files ;
  //             : removed SHOW_CONSOLE preference ;
  // ... ....... : fixed NPE and added different checks for SR 2956977
  // MMa 02/2011 : fixed bug #3157017 (incorrect package handling)
  // BF  06/2012 : added NON-NLS tag
  // MMa 10/2012 : renamed
  // MMa 11/2014 : changed super class from org.eclipse.jdt.internal.ui.wizards.NewElementWizard
  //                to org.eclipse.jface.wizard.Wizard, renamed class, added parser name replacement,
  //                renamed methods and changed visibility
  // MMa 02/2015 : fixed jars directory

  /** The wizard page */
  protected NewGrammarWizardPage jPage;

  /** The current selection */
  private IStructuredSelection   jSelection;

  /** A size */
  private static final int       DEFAULT_READING_SIZE = 8192;

  /**
   * Constructor for WizPage. Provides the image, DialogSetting, and title.
   */
  public NewGrammarWizard() {
    super();
    final ImageDescriptor id = AbstractActivator.getImageDescriptor("jjnew_wiz.gif"); //$NON-NLS-1$
    setDefaultPageImageDescriptor(id);
    setDialogSettings(AbstractActivator.getDefault().getDialogSettings());
    setWindowTitle(AbstractActivator.getMsg("WizPage.Creates_jj_example_file")); //$NON-NLS-1$
  }

  /** {@inheritDoc} */
  @Override
  public void init(@SuppressWarnings("unused") final IWorkbench aWorkbench,
                   final IStructuredSelection aSelection) {
    jSelection = aSelection;
  }

  /**
   * Adds the page to the wizard and initialize it with selection.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public void addPages() {
    jPage = new NewGrammarWizardPage();
    addPage(jPage);
    jPage.init(jSelection);
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

      /** {@inheritDoc} */
      @Override
      public void run(final IProgressMonitor monitor) {
        try {
          doFinish(srcdir, fileName, extension, packageName, staticFlag, monitor);
        } catch (final CoreException e) {
          AbstractActivator.logBug(e);
        } finally {
          monitor.done();
        }
      }
    };
    try {
      getContainer().run(true, false, op);
    } catch (final InterruptedException e) {
      AbstractActivator.logBug(e);
      return false;
    } catch (final InvocationTargetException e) {
      AbstractActivator.logBug(e);
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

    aMonitor.beginTask(AbstractActivator.getMsg("WizPage.Creating") + " " + aFileName + aExtension, 2); //$NON-NLS-1$ //$NON-NLS-2$

    // first: look for the srcDir/package 
    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    if (root == null || !root.exists()) {
      AbstractActivator.logErr(AbstractActivator.getMsg("WizPage.Root_problem"), new Exception(), //$NON-NLS-1$
                               (root == null ? "null" : root.getName())); //$NON-NLS-1$
      return;
    }
    String resName = aSrcDir;
    if (aPackageName != null && !"".equals(aPackageName)) { //$NON-NLS-1$
      resName += "/" + aPackageName.replace('.', '/'); //$NON-NLS-1$
    }
    final IResource res = root.findMember(new Path(resName));
    if (res == null || !res.exists() || !(res instanceof IContainer)) {
      AbstractActivator.logErr(AbstractActivator.getMsg("WizPage.Srcpkgdir_doesnot_exist"), new Exception(), //$NON-NLS-1$
                               resName);
      return;
    }
    // second: create the file
    final IContainer container = (IContainer) res;
    final String fileNameWithExt = aFileName + aExtension;
    final Path path = new Path(fileNameWithExt);
    final IFile file = container.getFile(path);
    try {
      final InputStream stream = getNewContent(aExtension, aPackageName, aFileName, aStaticFlag);
      if (stream == null) {
        // log is within openTemplateContentStreams
        return;
      }
      if (file.exists()) {
        file.setContents(stream, true, true, aMonitor);
        AbstractActivator.logInfo("File " + file + " updated"); //$NON-NLS-1$ //$NON-NLS-2$
      }
      else {
        file.create(stream, true, aMonitor);
        AbstractActivator.logInfo("File " + file + " created"); //$NON-NLS-1$ //$NON-NLS-2$
      }
      stream.close();
    } catch (final IOException e) {
      AbstractActivator.logBug(e,
                               AbstractActivator.getMsg("WizPage.Creation_of") + " (" + file + ") " + AbstractActivator.getMsg("WizPage.failed") + " : "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
      return;
    }
    aMonitor.worked(1);
    aMonitor.setTaskName(AbstractActivator.getMsg("WizPage.Opening_file_for_editing") + " (" + file + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    final Shell wizShell = getShell();
    if (wizShell == null) {
      AbstractActivator.logErr(AbstractActivator.getMsg("WizPage.Wizardshell_problem")); //$NON-NLS-1$ 
      return;
    }
    wizShell.getDisplay().asyncExec(new Runnable() {

      /** {@inheritDoc} */
      @Override
      public void run() {
        final IWorkbenchWindow iww = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (iww == null) {
          AbstractActivator.logErr(AbstractActivator.getMsg("WizPage.Workbenchwindow_problem")); //$NON-NLS-1$ 
          return;
        }
        final IWorkbenchPage wpage = iww.getActivePage();
        if (wpage == null) {
          AbstractActivator.logInfo(AbstractActivator.getMsg("WizPage.Activepage_null")); //$NON-NLS-1$ 
          return;
        }
        try {
          IDE.openEditor(wpage, file, true);
        } catch (final PartInitException e) {
          AbstractActivator.logBug(e,
                                   AbstractActivator.getMsg("WizPage.Opening_of") + " " + file + " " + AbstractActivator.getMsg("WizPage.failed") + " : "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        }
      }
    });
    aMonitor.worked(1);

    // initialize properties to get automatically a full build
    final IProject project = res.getProject();
    // force the nature in case the project has just been created
    Nature.setNature(true, project);
    final IEclipsePreferences prefs = new ProjectScope(project).getNode(PLUGIN_QN);
    if (prefs == null) {
      AbstractActivator.logErr(AbstractActivator.getMsg("WizPage.Prefs_null")); //$NON-NLS-1$ 
      return;
    }
    if (prefs.get(RUNTIME_JJJAR, null) == null) {
      // initializing properties do get automatically a full build
      AbstractActivator.logInfo(AbstractActivator.getMsg("WizPage.Initializing_preferences")); //$NON-NLS-1$ 
      // use the jar(s) in the plugin
      final String dir = Compiler.getJarsDir();
      final String javaCCJarFile = dir + DEF_JAVACC_JAR_NAME;
      final String jtbJarFile = dir + DEF_JTB_JAR_NAME;
      prefs.put(RUNTIME_JJJAR, javaCCJarFile);
      prefs.put(RUNTIME_JTBJAR, jtbJarFile);
      prefs.put(CLEAR_CONSOLE, "false"); //$NON-NLS-1$
      prefs.put(NATURE, "true"); //$NON-NLS-1$
      prefs.put(SUPPRESS_WARNINGS, "false"); //$NON-NLS-1$
    }

    try {
      prefs.flush();
    } catch (final BackingStoreException e) {
      AbstractActivator.logBug(e, AbstractActivator.getMsg("WizPage.Backingstore_problem") + " : "); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }
  }

  /**
   * Opens the appropriate template (jj/jjt/jtb, static or not) and makes the replacements.
   * 
   * @param aExtension - the template file extension
   * @param aPackageName - the package name
   * @param aFileName - the (grammar) file name / parser name
   * @param aStaticFlag - the static / non static flag
   * @return the modified template input stream
   */
  static InputStream getNewContent(final String aExtension, final String aPackageName,
                                   final String aFileName, final boolean aStaticFlag) {
    // the extension and the flag give the right template
    final String template = TEMPLATE_PREFIX + (aStaticFlag ? TEMPLATE_STATIC : TEMPLATE_NON_STATIC)
                            + aExtension;
    try {
      final URL installURL = AbstractActivator.getDefault().getBundle().getEntry(TEMPLATES_FOLDER);
      final URL url = new URL(installURL, template);
      // makeInputStream customizes the template
      return makeReplacements(url.openStream(), aPackageName, aFileName);
    } catch (final MalformedURLException e) {
      AbstractActivator.logBug(e);
    } catch (final IOException e) {
      AbstractActivator.logBug(e);
    }
    AbstractActivator.logErr(AbstractActivator.getMsg("WizPage.Template_problem") + " (" + template + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    return null;
  }

  /**
   * Makes the template's replacements.
   * 
   * @param aInputStream - the input stream
   * @param aPackageName - the package name
   * @param aParserName - the parser name
   * @return the updated input stream
   */
  static InputStream makeReplacements(final InputStream aInputStream, final String aPackageName,
                                      final String aParserName) {

    // read InputStream into byte buffer
    byte buffer[];
    try {
      //      buffer = Util.getInputStreamAsByteArray(aInputStream, -1);
      buffer = getInputStreamAsByteArray(aInputStream, -1);
      aInputStream.close();
    } catch (final IOException e) {
      AbstractActivator.logErr(AbstractActivator.getMsg("WizPage.Reading_failed") + " : " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
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
      str = str.replaceAll("<\\?parser_name\\?>", aParserName); //$NON-NLS-1$ 
    }

    // return InputStream
    buffer = str.getBytes();
    final ByteArrayInputStream baif = new ByteArrayInputStream(buffer);
    return baif;
  }

  /**
   * Returns the given input stream's contents as a byte array. If a length is specified (i.e. if length !=
   * -1), only length bytes are returned. Otherwise all bytes in the stream are returned. Note this doesn't
   * close the stream.<br>
   * Copied and adapted from org.eclipse.jdt.internal.compiler.util.Util
   * 
   * @param aStream - a stream
   * @param aLength - a length
   * @return - the byte array
   * @throws IOException if a problem occurred reading the stream.
   */
  static byte[] getInputStreamAsByteArray(final InputStream aStream, final int aLength) throws IOException {
    byte[] contents;
    if (aLength == -1) {
      contents = new byte[0];
      int contentsLength = 0;
      int amountRead = -1;
      do {
        final int amountRequested = Math.max(aStream.available(), DEFAULT_READING_SIZE); // read at least 8K

        // resize contents if needed
        if (contentsLength + amountRequested > contents.length) {
          System.arraycopy(contents, 0, contents = new byte[contentsLength + amountRequested], 0,
                           contentsLength);
        }

        // read as many bytes as possible
        amountRead = aStream.read(contents, contentsLength, amountRequested);

        if (amountRead > 0) {
          // remember length of contents
          contentsLength += amountRead;
        }
      } while (amountRead != -1);

      // resize contents if necessary
      if (contentsLength < contents.length) {
        System.arraycopy(contents, 0, contents = new byte[contentsLength], 0, contentsLength);
      }
    }
    else {
      contents = new byte[aLength];
      int len = 0;
      int readSize = 0;
      while ((readSize != -1) && (len != aLength)) {
        // See PR 1FMS89U
        // We record first the read size. In this case len is the actual read size.
        len += readSize;
        readSize = aStream.read(contents, len, aLength - len);
      }
    }

    return contents;
  }

}