package sf.eclipse.javacc;

import java.io.PrintStream;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import sf.eclipse.javacc.options.OptionSet;

/**
 * Builder for .jj, .jjt and .jtb files. Referenced by plugin.xml<br>
 * <extension point="org.eclipse.core.resources.builders">.<br>
 * It is also used to compile files via static methods.
 * 
 * @author Remi Koutcherawy 2003-2009 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009
 */
public class JJBuilder extends IncrementalProjectBuilder implements IResourceDeltaVisitor, IResourceVisitor,
                                                        IJJConstants {

  // MMa 11/09 : javadoc and formatting revision ; changed jar names

  /** the java project (needed to test if the resource is on class path) */
  protected IJavaProject javaProject;
  /** the output folder */
  protected IPath        outputFolder;

  /**
   * Invoked in response to a call to one of the <code>IProject.build</code>.
   * 
   * @see IncrementalProjectBuilder#build(int, Map, IProgressMonitor)
   * @param args a table of builder-specific arguments keyed by argument name (key type: <code>String</code>,
   *          value type: <code>String</code>); <code>null</code> is equivalent to an empty map
   * @param monitor a progress monitor, or <code>null</code> if progress reporting and cancellation are not
   *          desired
   * @return the list of projects for which this builder would like deltas the next time it is run or
   *         <code>null</code> if none
   * @exception CoreException if this build fails.
   * @see IProject#build(int, String, Map, IProgressMonitor)
   */
  @SuppressWarnings("unchecked")
  @Override
  protected IProject[] build(final int kind, @SuppressWarnings("unused") final Map args,
                             final IProgressMonitor monitor) throws CoreException {
    // These are Constants on the build
    javaProject = JavaCore.create(getProject());
    outputFolder = javaProject.getOutputLocation().removeFirstSegments(1);
    // Clear only once
    clearConsole();

    if (kind == IncrementalProjectBuilder.FULL_BUILD) {
      fullBuild(monitor);
    }
    else if (kind == IncrementalProjectBuilder.INCREMENTAL_BUILD
             || kind == IncrementalProjectBuilder.AUTO_BUILD) {
      incrementalBuild(monitor);
    }
    else if (kind == IncrementalProjectBuilder.CLEAN_BUILD) {
      clean(monitor);
    }
    // Refresh the whole project
    getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
    return null;
  }

  /**
   * Performs a full build.
   * 
   * @param monitor a progress monitor, or <code>null</code> if progress reporting and cancellation are not
   *          desired
   * @exception CoreException if this build fails
   */
  protected void fullBuild(@SuppressWarnings("unused") final IProgressMonitor monitor) throws CoreException {
    getProject().accept(this);
  }

  /**
   * Performs an incremental build or a full build if no delta is available.
   * 
   * @param monitor a progress monitor, or <code>null</code> if progress reporting and cancellation are not
   *          desired
   * @exception CoreException if this build fails
   */
  public void incrementalBuild(final IProgressMonitor monitor) throws CoreException {
    final IResourceDelta delta = getDelta(getProject());
    if (delta != null) {
      delta.accept(this);
    }
    else {
      fullBuild(monitor);
    }
  }

  /**
   * Cleans all generated files.
   * 
   * @param monitor a progress monitor, or <code>null</code> if progress reporting and cancellation are not
   *          desired
   * @exception CoreException if this build fails
   */
  @Override
  protected void clean(final IProgressMonitor monitor) throws CoreException {
    super.clean(monitor);
    final IResource[] members = getProject().members();
    clean(members, monitor);
  }

  /**
   * Deletes recursively generated AND derived files. A modified generated file, marked as not derived, shall
   * not be deleted.
   * 
   * @param members the IResource[] to delete
   * @param monitor a progress monitor, or <code>null</code> if progress reporting and cancellation are not
   *          desired
   * @exception CoreException if this build fails
   */
  private void clean(final IResource[] members, final IProgressMonitor monitor) throws CoreException {
    for (final IResource res : members) {
      if (res.getType() == IResource.FOLDER) {
        clean(((IFolder) res).members(), monitor);
      }
      else if (res.isDerived() && res.getPersistentProperty(QN_GENERATED_FILE) != null) {
        res.delete(IResource.KEEP_HISTORY, monitor);
      }
      else {
        res.deleteMarkers(IMarker.PROBLEM, false, IResource.DEPTH_ZERO);
      }
    }
  }

  /**
   * Visits the given resource delta.
   * 
   * @exception CoreException if this visit fails
   * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
   */
  public boolean visit(final IResourceDelta delta) throws CoreException {
    return visit(delta.getResource());
  }

  /**
   * Visits the given resource.
   * 
   * @param resource the IResource to visit
   * @return <code>true</code> if the resource's members should be visited; <code>false</code> if they should
   *         be skipped
   * @exception CoreException if this visit fails
   * @see org.eclipse.core.resources.IResourceVisitor#visit(org.eclipse.core.resources.IResource)
   */
  public boolean visit(final IResource resource) throws CoreException {
    final boolean okToCompile = javaProject.isOnClasspath(resource) && resource.getFileExtension() != null
                                && (resource.getFileExtension().equals("jj") //$NON-NLS-1$
                                    || resource.getFileExtension().equals("jjt") //$NON-NLS-1$
                                || resource.getFileExtension().equals("jtb")); //$NON-NLS-1$
    if (okToCompile) {
      CompileResource(resource);
    }

    // This prevents traversing output directories
    final boolean isOut = resource.getProjectRelativePath().equals(outputFolder)
                          & outputFolder.toString().length() != 0;
    return !isOut;
  }

  /**
   * Compiles a .jj, .jjt or .jtb file given its IResource.
   * 
   * @param resource the IResource to compile
   * @exception CoreException if this compile fails
   */
  public static void CompileResource(final IResource resource) throws CoreException {
    if (!(resource instanceof IFile) || !resource.exists()) {
      return;
    }

    // The file, the project and the directory
    final IFile file = (IFile) resource;
    final IProject pro = file.getProject();
    final String dir = pro.getLocation().toOSString();

    // Delete markers
    try {
      file.deleteMarkers(IMarker.PROBLEM, false, IResource.DEPTH_ZERO);
    } catch (final CoreException e) {
      e.printStackTrace();
    }

    // JavaCC is launched in the directory where the file is.
    String resdir = file.getLocation().toString();
    final String name = resdir.substring(resdir.lastIndexOf("/") + 1); //$NON-NLS-1$
    resdir = resdir.substring(0, resdir.lastIndexOf("/")); //$NON-NLS-1$
    final String extension = file.getFullPath().getFileExtension();

    final JJConsole console = Activator.getConsole();

    // Retrieve command line
    final String[] args = getArgs(file, name);
    final String jarfile = getJarFile(file);

    // Redirect out and error streams
    final PrintStream orgOut = System.out;
    final PrintStream orgErr = System.err;
    final PrintStream outConsole = console.getPrintStream();
    System.setOut(outConsole);
    System.setErr(outConsole);

    // Recall Command line on console
    if (extension.equals("jj")) { //$NON-NLS-1$
      console.print(">java -classpath " + jarfile + " javacc "); //$NON-NLS-1$ //$NON-NLS-2$
    }
    else if (extension.equals("jjt")) { //$NON-NLS-1$
      console.print(">java -classpath " + jarfile + " jjtree "); //$NON-NLS-1$ //$NON-NLS-2$
    }
    else if (extension.equals("jtb")) { //$NON-NLS-1$
      console.print(">java -jar " + jarfile + " "); //$NON-NLS-1$ //$NON-NLS-2$
    }
    for (int i = 0; i < args.length; i++) {
      console.print(args[i] + " "); //$NON-NLS-1$
    }
    System.out.println();

    // Call JavaCC, JJTree or JTB
    DirList.snapshot(dir);
    if (extension.equals("jjt")) { //$NON-NLS-1$
      JarLauncher.launchJJTree(jarfile, args, resdir);
    }
    else if (extension.equals("jj")) { //$NON-NLS-1$
      JarLauncher.launchJavaCC(jarfile, args, resdir);
    }
    else if (extension.equals("jtb")) { //$NON-NLS-1$
      JarLauncher.launchJTB(jarfile, args, resdir);
    }
    System.out.println();

    // Restore standard and error streams
    System.setOut(orgOut);
    System.setErr(orgErr);

    // Notify Console with the File the Console should report Errors to
    console.endReport(file);

    // Compile Generated .jj File if a .jjt or .jtb file was processed
    final String[] jjgenerated = DirList.getDiff(dir);
    if (jjgenerated != null) {
      for (int i = 0; i < jjgenerated.length; i++) {
        jjgenerated[i] = jjgenerated[i].substring(dir.length() + 1);
        IResource resgenerated = pro.findMember(jjgenerated[i]);
        if (resgenerated == null) {
          pro.refreshLocal(IResource.DEPTH_INFINITE, null);
          resgenerated = pro.findMember(jjgenerated[i]);
        }

        // Take the opportunity to mark them with a 'G' and to correct .java files
        MarkAndCorrect.markAndCorrect(pro, name, resgenerated);

        // Compile .jj only if .jjt or .jtb was compiled and .jj was generated
        if ((extension.equals("jjt") || extension.equals("jtb")) //$NON-NLS-1$ //$NON-NLS-2$
            && jjgenerated[i].endsWith(".jj")) { //$NON-NLS-1$ 
          // Compile .jj if project has not Javacc Nature, ie no automatic build
          // Well seems Eclipse has a small bug here, it doesn't recompile...
          // if (!pro.getDescription().hasNature(JJ_NATURE_ID))
          CompileResource(resgenerated);
        }
      }
    }
  }

  /**
   * Calls JJDoc for a .jj, .jjt or .jtb file given its IResource.
   * 
   * @param ressource the IResource to JJDoc
   */
  public static void GenDocForJJResource(final IResource ressource) {
    if (!(ressource instanceof IFile)) {
      return;
    }

    final IFile file = (IFile) ressource;
    final IProject pro = file.getProject();
    final String dir = pro.getLocation().toOSString();
    final String name = file.getLocation().toString();

    final JJConsole console = Activator.getConsole();

    // Retrieve command line
    final String[] args = getJJDocArgs(file, name);
    final String jarfile = getJarFile(file);

    // Redirect standard and error streams
    final PrintStream orgOut = System.out;
    final PrintStream orgErr = System.err;
    final PrintStream outConsole = console.getPrintStream();
    System.setOut(outConsole);
    System.setErr(outConsole);

    // Recall command line on console
    console.print(">java -classpath " + jarfile + " jjdoc "); //$NON-NLS-1$ //$NON-NLS-2$
    for (int i = 0; i < args.length; i++) {
      console.print(args[i] + " "); //$NON-NLS-1$
    }
    System.out.println();

    // Call JJDoc
    JarLauncher.launchJJDoc(jarfile, args, dir);
    System.out.println();

    // Restores standard and error streams
    System.setOut(orgOut);
    System.setErr(orgErr);

    // Notify Console with the File the Console should report Errors to
    console.endReport(file);
  }

  /**
   * Builds the array of options to call the JavaCC, JJTree or JTB Compiler with.
   * 
   * @param file the resource to get the options for
   * @param name the file name to compile
   * @return String[] the options to call the JavaCC / JJTree / JTB compiler with
   */
  @SuppressWarnings("null")
  protected static String[] getArgs(final IFile file, final String name) {
    String[] args = null;
    final String extension = file.getFullPath().getFileExtension();
    try {
      String options = null;
      final IEclipsePreferences prefs = new ProjectScope(file.getProject()).getNode(IJJConstants.ID);

      if (extension.equals("jj")) { //$NON-NLS-1$
        options = prefs.get(JAVACC_OPTIONS, ""); //$NON-NLS-1$
      }
      else if (extension.equals("jjt")) { //$NON-NLS-1$
        options = prefs.get(JJTREE_OPTIONS, ""); //$NON-NLS-1$
      }
      else if (extension.equals("jtb")) { //$NON-NLS-1$
        options = prefs.get(JTB_OPTIONS, ""); //$NON-NLS-1$
      }
      if (options == null) {
        options = ""; //$NON-NLS-1$
      }

      // Add target ie file to compile
      options = options + " \"" + name + "\""; //$NON-NLS-1$ //$NON-NLS-2$

      // Get tokens
      args = OptionSet.tokenize(options);
    } catch (final Exception e) {
      e.printStackTrace();
    }

    if (!extension.equals("jtb")) { //$NON-NLS-1$
      // finished for .jj & .jjt
      return args;
    }

    // The JTB syntax is "-o" "foo" and not "-o=foo"
    int nb = 0;
    for (int i = 0; i < args.length; i++) {
      if (args[i].indexOf('=') != -1) {
        nb++;
      }
    }
    final String[] nargs = new String[args.length + nb];
    nb = 0;
    for (int i = 0; i < args.length; i++) {
      if (args[i].indexOf('=') != -1) {
        nargs[i + nb] = args[i].substring(0, args[i].indexOf('='));
        nb++;
        nargs[i + nb] = args[i].substring(args[i].indexOf('=') + 1);
      }
      else {
        nargs[i + nb] = args[i];
      }
    }
    return nargs;
  }

  /**
   * Builds the array of options to call the JJDoc Compiler with.
   * 
   * @param file the resource to get the options for
   * @param name the file name to compile
   * @return String[] the array of options to call the JJDoc Compiler with
   */
  protected static String[] getJJDocArgs(final IResource file, final String name) {
    String[] args = null;
    try {
      final IEclipsePreferences prefs = new ProjectScope(file.getProject()).getNode(IJJConstants.ID);
      String options = null;
      // Read project properties
      options = prefs.get(JJDOC_OPTIONS, ""); //$NON-NLS-1$
      // Else take default
      if (options == null) {
        options = ""; //$NON-NLS-1$
      }
      // Adds target ie file to compile
      options = options + " \"" + name + " \""; //$NON-NLS-1$ //$NON-NLS-2$
      // Gets tokens
      args = OptionSet.tokenize(options);
    } catch (final Exception e) {
      e.printStackTrace();
    }
    return args;
  }

  /**
   * Retrieves the path to the jar file (from the preferences or from the plug-in).
   * 
   * @param file the IResource to get the jar file for
   * @return String the path to the jar file
   */
  protected static String getJarFile(final IResource file) {
    String jarfile = null;
    final String extension = file.getFullPath().getFileExtension();
    try {
      final IEclipsePreferences prefs = new ProjectScope(file.getProject()).getNode(IJJConstants.ID);
      // Use the path in preferences
      if (extension.equals("jj") || extension.equals("jjt")) {//$NON-NLS-1$ //$NON-NLS-2$
        jarfile = prefs.get(RUNTIME_JJJAR, ""); //$NON-NLS-1$
      }
      else if (extension.equals("jtb")) { //$NON-NLS-1$
        jarfile = prefs.get(RUNTIME_JTBJAR, ""); //$NON-NLS-1$
      }
      // Else we use the jar in the plugin
      if (jarfile == null || jarfile.equals("") || jarfile.startsWith("-")) {//$NON-NLS-1$ //$NON-NLS-2$
        final URL installURL = Activator.getDefault().getBundle().getEntry("/"); //$NON-NLS-1$
        // Eclipse 3.2 way. Only available in Eclipse 3.2
        final URL resolvedURL = org.eclipse.core.runtime.FileLocator.resolve(installURL);
        final String home = org.eclipse.core.runtime.FileLocator.toFileURL(resolvedURL).getFile();

        // Same for both
        if (extension.equals("jj") || extension.equals("jjt")) {//$NON-NLS-1$ //$NON-NLS-2$
          jarfile = home + JAVACC_JAR_NAME;
        }
        else if (extension.equals("jtb")) {//$NON-NLS-1$
          jarfile = home + JTB_JAR_NAME;
        }
      }
      try {
        jarfile = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(jarfile,
                                                                                                    true);
        // On Windows this returns "/C:/workspace/sf.eclipse.javacc/jtb132.jar"
        // As this will fails, we remove the first "/" if there is ":" at index 2
        if (jarfile.startsWith("/") && jarfile.startsWith(":", 2)) { //$NON-NLS-1$ //$NON-NLS-2$
          jarfile = jarfile.substring(1);
        }
      } catch (final CoreException e) {
        System.out.println("Warning: couldn't resolve JAR file: " + e.getMessage()); //$NON-NLS-1$
        // jarfile will keep it's previous value, which will fail in launch()
      }
    } catch (final Exception e) {
      e.printStackTrace();
    }
    return jarfile;
  }

  /**
   * Clears the Console if a Console is available.
   * 
   * @exception CoreException if this clear fails
   */
  protected void clearConsole() throws CoreException {
    final boolean clr = ("true").equals(new ProjectScope(getProject()).getNode(IJJConstants.ID).get(CLEAR_CONSOLE, "false")); //$NON-NLS-1$ //$NON-NLS-2$
    if (clr) {
      final JJConsole console = Activator.getConsole();
      if (console != null) {
        console.clear();
      }
    }
  }
}
