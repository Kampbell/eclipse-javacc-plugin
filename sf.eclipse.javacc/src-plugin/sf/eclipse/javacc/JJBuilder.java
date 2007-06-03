package sf.eclipse.javacc;

import java.io.PrintStream;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import sf.eclipse.javacc.options.OptionSet;

/**
 * Builder for .jj and .jjt files Referenced by plugin.xml <extension
 * point="org.eclipse.core.resources.builders"> It is also used to compile files
 * via static methods.
 * 
 * @author Remi Koutcherawy 2003-2006 CeCILL Licence
 *         http://www.cecill.info/index.en.html
 */
public class JJBuilder extends IncrementalProjectBuilder implements
    IResourceDeltaVisitor, IResourceVisitor, IJJConstants {

  // Needed to test if the resource is on class path
  protected IJavaProject javaProject;
  protected IPath outputFolder;

  /**
   * Invoked in response to a call to one of the <code>IProject.build</code>
   * Look at org.eclipse.core.internal.events.InternalBuilder#build(int,
   * java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
   */
  protected IProject[] build(int kind, Map args, IProgressMonitor mon)
      throws CoreException {
    // These are Contants on the build
    javaProject = JavaCore.create(getProject());
    outputFolder = javaProject.getOutputLocation().removeFirstSegments(1);

    if (kind == IncrementalProjectBuilder.FULL_BUILD) {
      fullBuild(mon);
    } else {
      incrementalBuild(mon);
    }
    // Refresh the whole project
    getProject().refreshLocal(IResource.DEPTH_INFINITE, mon);
    return null;
  }

  /**
   * Do a Full Build
   * @param monitor
   * @throws CoreException
   */
  protected void fullBuild(IProgressMonitor mon) throws CoreException {
    clearConsole();
    getProject().accept(this);
  }

  /**
   * Do an incremental Build or a Full build if no delta is available
   * @param monitor
   * @throws CoreException
   */
  public void incrementalBuild(IProgressMonitor mon) throws CoreException {
    clearConsole();
    IResourceDelta delta = getDelta(getProject());
    if (delta != null)
      delta.accept(this);
    else
      fullBuild(mon);
  }

  /**
   * Visit the given resource delta
   * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
   */
  public boolean visit(IResourceDelta delta) throws CoreException {
    return visit(delta.getResource());
  }

  /**
   * Visit the given resource
   * @see org.eclipse.core.resources.IResourceVisitor#visit(org.eclipse.core.resources.IResource)
   */
  public boolean visit(IResource res) throws CoreException {
    boolean okToCompile = javaProject.isOnClasspath(res) 
     && (res.getFileExtension() == null // a directory
         || res.getFileExtension().equals("jj")  //$NON-NLS-1$
         || res.getFileExtension().equals("jtb")  //$NON-NLS-1$
         || res.getFileExtension().equals("jjt"));  //$NON-NLS-1$
    if (okToCompile)
      CompileResource(res);

    // This prevents traversing output directories
    boolean isOut = res.getProjectRelativePath().equals(outputFolder) & outputFolder.toString().length() != 0;
    return !isOut;
  }

  /**
   * Compile a .jj jjt or .jtb file given its IResource
   * @param res IResource to compile
   */
  public static void CompileResource(IResource res) throws CoreException {
    if (!(res instanceof IFile) || !res.exists())
      return;

    // The file, the project and the directory
    IFile file = (IFile) res;
    IProject pro = file.getProject();
    String dir = pro.getLocation().toOSString();

    // Delete markers
    try {
      file.deleteMarkers(IMarker.PROBLEM, false, IResource.DEPTH_ZERO);
    } catch (CoreException e) {
      e.printStackTrace();
    }

    // JavaCC is launched in the directory where the file is.
    String resdir = file.getLocation().toString();
    String name = resdir.substring(resdir.lastIndexOf("/") + 1); //$NON-NLS-1$
    resdir = resdir.substring(0, resdir.lastIndexOf("/")); //$NON-NLS-1$
    String extension = file.getFullPath().getFileExtension();
    
    JJConsole console = Activator.getConsole();

    // Retrieves runtime options
    boolean projectOverride = false;
    try {
      projectOverride = ("true").equals(pro.getPersistentProperty(QN_PROJECT_OVERRIDE)); //$NON-NLS-1$
    } catch (CoreException e) {
      e.printStackTrace();
    }

    // Retrieve command line
    String[] args = getArgs(file, name, projectOverride);
    String jarfile = getJarFile(file);

    // Redirect out and error streams
    PrintStream orgOut = System.out;
    PrintStream orgErr = System.err;
    PrintStream outConsole = console.getPrintStream();
    System.setOut(outConsole);
    System.setErr(outConsole);

    // Recall Command line on console
    if (extension.equals("jjt")) //$NON-NLS-1$
      console.print(">java -classpath " + jarfile + " jjtree "); //$NON-NLS-1$ //$NON-NLS-2$
    else if (extension.equals("jj")) //$NON-NLS-1$
      console.print(">java -classpath " + jarfile + " javacc "); //$NON-NLS-1$ //$NON-NLS-2$
    else if (extension.equals("jtb")) //$NON-NLS-1$
      console.print(">java -jar " + jarfile + " "); //$NON-NLS-1$ //$NON-NLS-2$
    for (int i = 0; i < args.length; i++)
      console.print(args[i] + " "); //$NON-NLS-1$
    System.out.println();

    // Call JavaCC, JJTree or JTB
    DirList.snapshot(dir);
    if (extension.equals("jjt")) //$NON-NLS-1$
      JarLauncher.launchJJTree(jarfile, args, resdir);
    else if (extension.equals("jj")) //$NON-NLS-1$
      JarLauncher.launchJavaCC(jarfile, args, resdir);
    else if (extension.equals("jtb")) //$NON-NLS-1$
      JarLauncher.launchJTB(jarfile, args, resdir);
    System.out.println();

    // Restore standard and error streams
    System.setOut(orgOut);
    System.setErr(orgErr);

    // Notify Console with the File the Console should report Errors to
    console.endReport(file);

    // Compile Generated .jj File if a .jjt or .jtb file was processed
    String[] jjgenerated = DirList.getDiff(dir);
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
   * Call JJDoc for a .jj or .jjt file given its IResource
   * @param res IResource to compile
   */
  public static void GenDocForJJResource(IResource res) {
    if (!(res instanceof IFile))
      return;

    IFile file = (IFile) res;
    IProject pro = file.getProject();
    String dir = pro.getLocation().toOSString();
    String name = file.getLocation().toString();

    JJConsole console = Activator.getConsole();

    // Retrieve runtime options
    boolean projectOverride = false;
    try {
      projectOverride = ("true").equals(pro.getPersistentProperty( //$NON-NLS-1$
          QN_PROJECT_OVERRIDE));
    } catch (CoreException e) {
      e.printStackTrace();
    }

    // Retrieve command line
    String[] args = getJJDocArgs(file, name, projectOverride);
    String jarfile = getJarFile(file);

    // Redirect standard and error streams
    PrintStream orgOut = System.out;
    PrintStream orgErr = System.err;
    PrintStream outConsole = console.getPrintStream();
    System.setOut(outConsole);
    System.setErr(outConsole);

    // Recall command line on console
    console.print(">java -classpath " + jarfile + " jjdoc "); //$NON-NLS-1$ //$NON-NLS-2$
    for (int i = 0; i < args.length; i++)
      console.print(args[i] + " "); //$NON-NLS-1$
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
   * Make args[] of options to call JavaCC JJTree or JTB Compiler
   * @param file resource to get the options from
   * @param name file name
   * @param projectOverride true if the properties is from project
   * @return String[] of options to call JavaCC compiler with
   */
  protected static String[] getArgs(IFile file, String name,
      boolean projectOverride) {
    String[] args = null;
    String extension = file.getFullPath().getFileExtension();
    try {
      String options = null;
      if (extension.equals("jj")) { //$NON-NLS-1$
        // Try for resource property
        options = file.getPersistentProperty(QN_JAVACC_OPTIONS);
        // Else take Project Property
        if (options == null || projectOverride)
          options = file.getProject().getPersistentProperty(QN_JAVACC_OPTIONS);
      } else if (extension.equals("jjt")) { //$NON-NLS-1$
        options = file.getPersistentProperty(QN_JJTREE_OPTIONS);
        if (options == null || projectOverride)
          options = file.getProject().getPersistentProperty(QN_JJTREE_OPTIONS);
      } else if (extension.equals("jtb")) { //$NON-NLS-1$
        options = file.getPersistentProperty(QN_JTB_OPTIONS);
        if (options == null || projectOverride)
          options = file.getProject().getPersistentProperty(QN_JTB_OPTIONS);
      }        
      if (options == null)
        options = ""; //$NON-NLS-1$
        
      // Add target ie file to compile
      options = options + " \"" + name + "\""; //$NON-NLS-1$ //$NON-NLS-2$
      
      // Get tokens
      args = OptionSet.tokenize(options);
    } catch (Exception e) {
      e.printStackTrace();
    }
    // Standart case for .jj .jjt
    if (! extension.equals("jtb")) //$NON-NLS-1$
      return args;
      
    // The JTB syntax is "-o" "foo" and not "-o=foo"
    int nb = 0;
    for (int i = 0; i < args.length; i++)
      if (args[i].indexOf('=') != -1)
        nb++;
    String[] nargs = new String[args.length + nb];
    nb = 0;
    for (int i = 0; i < args.length; i++) {
      if (args[i].indexOf('=') != -1) {
        nargs[i + nb] = args[i].substring(0, args[i].indexOf('='));
        nb++;
        nargs[i + nb] = args[i].substring(args[i].indexOf('=') + 1);
      } else
        nargs[i + nb] = args[i];
    }
    return nargs;
  }

  /**
   * Make args[] of options to call JJDoc
   * @param file resource to get the options from
   * @param name file name
   * @param projectOverride true if the properties is from project
   * @return String[] of options to call JavaCC compiler with
   */
  protected static String[] getJJDocArgs(IResource file, String name,
      boolean projectOverride) {
    String[] args = null;
    try {
      String options = null;
      // Try for resource property
      options = file.getPersistentProperty(QN_JJDOC_OPTIONS);
      // Else take Project Property
      if (options == null || projectOverride)
        options = file.getProject().getPersistentProperty(QN_JJDOC_OPTIONS);
      // Else take default
      if (options == null)
        options = ""; //$NON-NLS-1$
      // Adds target ie file to compile
      options = options + " \"" + name + " \""; //$NON-NLS-1$ //$NON-NLS-2$
      // Gets tokens
      args = OptionSet.tokenize(options);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return args;
  }

  /**
   * Provide the path to javacc.jar
   * @param file  the resource to get the property from
   * @return String path to javacc.jar or jtb132.jar
   */
  protected static String getJarFile(IResource file) {
    String jarfile = null;
    String extension = file.getFullPath().getFileExtension();
    try {
      // If the user has given a path, we use it
      if (extension.equals("jj") || extension.equals("jjt")) //$NON-NLS-1$ //$NON-NLS-2$
        jarfile = file.getProject().getPersistentProperty(QN_RUNTIME_JAR);
      else if (extension.equals("jtb")) //$NON-NLS-1$
        jarfile = file.getProject().getPersistentProperty(QN_RUNTIME_JTBJAR);
      
      // Else we use the jar in the plugin
      if (jarfile == null || jarfile.equals("") || jarfile.startsWith("-")) {//$NON-NLS-1$ //$NON-NLS-2$
        URL installURL = Activator.getDefault().getBundle().getEntry("/"); //$NON-NLS-1$
        // Eclipse 3.1 way. Deprecated in 3.2
//        URL resolvedURL = org.eclipse.core.runtime.Platform.resolve(installURL);
//        String home = org.eclipse.core.runtime.Platform.asLocalURL(resolvedURL).getFile();
        // Eclipse 3.2 way. Only available in Eclipse 3.2
         URL resolvedURL =
         org.eclipse.core.runtime.FileLocator.resolve(installURL);
         String home =
         org.eclipse.core.runtime.FileLocator.toFileURL(resolvedURL).getFile();
        
        // Same for both
        if (extension.equals("jj") || extension.equals("jjt")) //$NON-NLS-1$ //$NON-NLS-2$
          jarfile = home + "javacc.jar"; //$NON-NLS-1$
        else if (extension.equals("jtb")) //$NON-NLS-1$
          jarfile = home + "jtb132.jar"; //$NON-NLS-1$

        if (jarfile.startsWith("/") && jarfile.startsWith(":", 2)) //$NON-NLS-1$ //$NON-NLS-2$
          jarfile = jarfile.substring(1);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return jarfile;
  }

  /**
   * Clear Console if a Console is available
   * @throws CoreException
   */
  protected void clearConsole() throws CoreException {
    boolean clr = ("true").equals(getProject().getPersistentProperty(QN_CLEAR_CONSOLE)); //$NON-NLS-1$
    if (clr) {
      JJConsole console = Activator.getConsole();
      if (console != null)
        console.clear();
    }
  }
}
