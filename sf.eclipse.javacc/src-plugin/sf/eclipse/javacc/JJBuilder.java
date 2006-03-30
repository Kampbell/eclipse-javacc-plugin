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
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sf.eclipse.javacc.options.OptionSet;

/**
 * Builder for .jj and .jjt files
 * Referenced by plugin.xml
 *  <extension point="org.eclipse.core.resources.builders">
 * It is also used to compile files via static methods. 
 * 
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */
public class JJBuilder extends IncrementalProjectBuilder
  implements IResourceDeltaVisitor, IResourceVisitor, IJJConstants {
  
  // Needed to test if the resource is on class path
  protected IJavaProject javaProject;
  protected IPath outputFolder;
 
 /**
  * Invoked in response to a call to one of the <code>IProject.build</code>
  * Look at org.eclipse.core.internal.events.InternalBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
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
  public void incrementalBuild(IProgressMonitor mon)throws CoreException {
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
    boolean okToCompile = javaProject.isOnClasspath(res);
    if (okToCompile) {
      String extension = res.getFullPath().getFileExtension();
      if ("jj".equals(extension) || "jjt".equals(extension)) //$NON-NLS-1$ //$NON-NLS-2$
	CompileJJResource(res);
      if ("jtb".equals(extension)) //$NON-NLS-1$
	CompileJTBResource(res);
    }
    // This prevents traversing output directories
    boolean isOut = res.getProjectRelativePath().equals(outputFolder);
    return !isOut;
  }

  /**
   * Compile a .jj jjt or .jtb file given its IResource
   * @param res IResource to compile
   */
  public static void CompileJJResource(IResource res) throws CoreException {
    if (!(res instanceof IFile) || !res.exists())
      return;

    IFile file = (IFile) res;
    IProject pro = file.getProject();
    String dir = pro.getLocation().toOSString();
    
    // Delete markers
    try {
      file.deleteMarkers(IMarker.PROBLEM, false, IResource.DEPTH_ZERO);
    } catch (CoreException e) {
      e.printStackTrace();
    }
    
    // JavaCC is now launched in the directory where .jj is.
    String resdir = file.getLocation().toString();
    String name = resdir.substring(resdir.lastIndexOf("/")+1); //$NON-NLS-1$
    resdir = resdir.substring(0, resdir.lastIndexOf("/")); //$NON-NLS-1$
    
    JJConsole console = Activator.getConsole();
    
    // Retrieves runtime options
    boolean projectOverride = false;
    try {
      projectOverride = ("true").equals(pro.getPersistentProperty(QN_PROJECT_OVERRIDE)); //$NON-NLS-1$
    } catch (CoreException e) {
      e.printStackTrace();
    }
    
    // Retrieves command line
    String[] args = getArgs(file, name, projectOverride);
    String jarfile = getJavaCCJarFile(file);
    
    // Redirects out and error streams
    PrintStream orgOut = System.out;
    PrintStream orgErr = System.err;
    PrintStream outConsole = console.getPrintStream();
    System.setOut(outConsole);
    System.setErr(outConsole);
    
    // Recalls Command line on console
    if (name.endsWith(".jjt")) //$NON-NLS-1$
      console.print(">jjtree "); //$NON-NLS-1$
    else 
      console.print(">javacc "); //$NON-NLS-1$
    for (int i = 0; i < args.length; i++)
      console.print(args[i] + " "); //$NON-NLS-1$
    System.out.println();
    
    // Calls JavaCC
    DirList.snapshot(dir);
    if (name.endsWith(".jjt")) //$NON-NLS-1$
      JarLauncher.launchJJTree(jarfile, args, resdir);
    else
      JarLauncher.launchJavaCC(jarfile, args, resdir);
    System.out.println();
    
    // Restores standard and error streams
    System.setOut(orgOut);
    System.setErr(orgErr);
    
    // Notify Console with the File the Console should report Errors to
    console.endReport(file);
    
    // Compiles Generated .jj File if a .jjt file was processed
    String[] jjgenerated = DirList.getDiff(dir);
    if (jjgenerated != null) {
      for (int i = 0; i < jjgenerated.length; i++) {
        jjgenerated[i] = jjgenerated[i].substring(dir.length() + 1);
        IResource resgenerated = pro.findMember(jjgenerated[i]);
        if (resgenerated == null) {
          pro.refreshLocal(IResource.DEPTH_INFINITE, null);
          resgenerated = pro.findMember(jjgenerated[i]);
        }
        // Take the opportunity to mark them with a 'G'
        if (resgenerated != null) {
          resgenerated.setDerived(true);
          resgenerated.setPersistentProperty(QN_GENERATED_FILE, name);
        }
        if (name.endsWith(".jjt") && jjgenerated[i].endsWith(".jj")) { //$NON-NLS-1$ //$NON-NLS-2$
          // Compile .jj if project has not Javacc Nature, ie no automatic build
          // Well seems Eclipse has a small bug here, it doesn't recompile...
          // if (!pro.getDescription().hasNature(JJ_NATURE_ID))
          CompileJJResource(resgenerated);
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
    
    // Delete markers
    try {
      file.deleteMarkers(IMarker.PROBLEM, false, IResource.DEPTH_ZERO);
    } catch (CoreException e) {
      e.printStackTrace();
    }
    
    JJConsole console = Activator.getConsole();
    
    // Retrieves runtime options
    boolean projectOverride = false;
    try {
      projectOverride = ("true").equals(pro.getPersistentProperty( //$NON-NLS-1$
          QN_PROJECT_OVERRIDE));
    } catch (CoreException e) {
      e.printStackTrace();
    }
    
    // Retrieves command line
    String[] args = getJJDocArgs(file, name, projectOverride);
    String jarfile = getJavaCCJarFile(file);
    if (jarfile == null) {
      IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
      MessageDialog.openInformation(
          w.getShell(),
          Activator.getString("JJBuilder.JJBuilder"), //$NON-NLS-1$
          Activator.getString("JJBuilder.Please_set_the_jtb_jar_file")); //$NON-NLS-1$
      return;
    }
    
    // Redirects standard and error streams
    PrintStream orgOut = System.out;
    PrintStream orgErr = System.err;
    PrintStream outConsole = console.getPrintStream();
    System.setOut(outConsole);
    System.setErr(outConsole);
    
    // Recall command line on console
    console.print(">jjdoc "); //$NON-NLS-1$
    for (int i = 0; i < args.length; i++)
      console.print(args[i] + " "); //$NON-NLS-1$
    System.out.println();
    
    // Calls JJDoc
    JarLauncher.launchJJDoc(jarfile, args, dir);
    System.out.println();
    
    // Restores standard and error streams
    System.setOut(orgOut);
    System.setErr(orgErr);
    
    // Notify Console with the File the Console should report Errors to
    console.endReport(file);
  }
  
  /**
   * Call JTB for a .jtb file given its IResource
   * @param res IResource to compile
   */
  public static void CompileJTBResource(IResource res) throws CoreException {
    if (!(res instanceof IFile))
      return;
    
    IFile file = (IFile) res;
    IProject pro = file.getProject();
    String dir = pro.getLocation().toOSString();
    
    // Launch in the directory where .jtb is.
    String resdir = file.getLocation().toString();
    String name = resdir.substring(resdir.lastIndexOf("/")+1); //$NON-NLS-1$
    resdir = resdir.substring(0, resdir.lastIndexOf("/")); //$NON-NLS-1$
    
    // Delete markers
    try {
      file.deleteMarkers(IMarker.PROBLEM, false, IResource.DEPTH_ZERO);
    } catch (CoreException e) {
      e.printStackTrace();
    }
    
    JJConsole console = Activator.getConsole();
    
    // Retrieves runtime options
    boolean projectOverride = false;
    try {
      projectOverride = ("true").equals(pro.getPersistentProperty( //$NON-NLS-1$
          QN_PROJECT_OVERRIDE));
    } catch (CoreException e) {
      e.printStackTrace();
    }
    
    // Retrieves command line
    String[] args = getJTBArgs(file, name, projectOverride);
    String jarfile = getJTBJarFile(file);
    if (jarfile == null) {
      IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
      MessageDialog.openInformation(
          w.getShell(),
          Activator.getString("JJBuilder.JJBuilder"), //$NON-NLS-1$
          Activator.getString("JJBuilder.Please_set_the_jar_file")); //$NON-NLS-1$
      return;
    }
    
    // Redirects standard and error streams
    PrintStream orgOut = System.out;
    PrintStream orgErr = System.err;
    PrintStream outConsole = console.getPrintStream();
    System.setOut(outConsole);
    System.setErr(outConsole);
    
    // Recall command line on console
    String jarname = jarfile.substring(jarfile.lastIndexOf("/")+1); //$NON-NLS-1$
    console.print(">java -jar "+jarname+" "); //$NON-NLS-1$ //$NON-NLS-2$
    for (int i = 0; i < args.length; i++)
      console.print(args[i] + " "); //$NON-NLS-1$
    System.out.println();
    
    // Calls JTB
    DirList.snapshot(dir);
    JarLauncher.launchJTB(jarfile, args, resdir);
    System.out.println();
    
    // Restores standard and error streams
    System.setOut(orgOut);
    System.setErr(orgErr);
    
    // Notify Console with the File the Console should report Errors to
    console.endReport(file);
    
    // Compiles Generated .jj File
    String[] jjgenerated = DirList.getDiff(dir);
    if (jjgenerated != null) {
      for (int i = 0; i < jjgenerated.length; i++) {
        jjgenerated[i] = jjgenerated[i].substring(dir.length() + 1);
        IResource resgenerated = pro.findMember(jjgenerated[i]);
        if (resgenerated == null) {
          pro.refreshLocal(IResource.DEPTH_INFINITE, null);
          resgenerated = pro.findMember(jjgenerated[i]);
        }
        // Take the opportunity to mark them with a 'G'
        if (resgenerated != null) {
          resgenerated.setDerived(true);
          resgenerated.setPersistentProperty(QN_GENERATED_FILE, name);
        }
        if (name.endsWith(".jtb") && jjgenerated[i].endsWith(".jj")) { //$NON-NLS-1$ //$NON-NLS-2$
          // Compile .jj if project has not Javacc Nature, ie no automatic build
          // Well seems Eclipse has a small bug here, it doesn't recompile...
          // if (!pro.getDescription().hasNature(JJ_NATURE_ID))
          CompileJJResource(resgenerated);
        }
      }
    }
  }
  
  /**
   * Make args[] of options to call JavaCC Compiler
   * @param file, resource to get the options from
   * @param name, file name
   * @param projectOverride, true if the properties is from project
   * @return String[] of options to call JavaCC compiler with
   */
  protected static String[] getArgs(IFile file, String name, boolean projectOverride) {
    String[] args = null;
    try {
      String options = null;
      if (name.endsWith(".jj")) { //$NON-NLS-1$
        // Try for resource property
        options = file.getPersistentProperty(QN_JAVACC_OPTIONS);
        // Else take Project Property
        if (options == null || projectOverride)
          options = file.getProject().getPersistentProperty(QN_JAVACC_OPTIONS);
        // Else takes defaults
        if (options == null) {
          options = ""; //$NON-NLS-1$
        }
      }
      else if (name.endsWith(".jjt")) { //$NON-NLS-1$
        options = file.getPersistentProperty(QN_JJTREE_OPTIONS);
        if (options == null || projectOverride)
          options =  file.getProject().getPersistentProperty(QN_JJTREE_OPTIONS);
        if (options == null) {
          options = ""; //$NON-NLS-1$
        }
      }
      // Adds target ie file to compile
      options = options +" \""+name+"\""; //$NON-NLS-1$ //$NON-NLS-2$
      // Gets tokens
      args = OptionSet.tokenize(options);      
    } catch (Exception e) {
      e.printStackTrace();
    }
    return args;
  }
  
  /**
   * Make args[] of options to call JJDoc
   * @param file, resource to get the options from
   * @param name, file name
   * @param projectOverride, true if the properties is from project
   * @return String[] of options to call JavaCC compiler with
   */
  protected static String[] getJJDocArgs(IResource file, String name, boolean projectOverride) {
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
   * Make args[] of options to call JTB
   * @param file, resource to get the options from
   * @param name, file name
   * @param projectOverride, true if the properties is from project
   * @return String[] of options to call JTB compiler with
   */
  protected static String[] getJTBArgs(IFile file, String name, boolean projectOverride) {
    String[] args = null;
    try {
      String options = null;
      // Try for resource property
      options = file.getPersistentProperty(QN_JTB_OPTIONS);
      // Else take Project Property
      if (options == null || projectOverride)
        options = file.getProject().getPersistentProperty(QN_JTB_OPTIONS);
      // Else take default
      if (options == null) {
        options = ""; //$NON-NLS-1$
      }
      // Adds target ie file to compile
      options = options +" \""+name+"\"";  //$NON-NLS-1$ //$NON-NLS-2$
      // Gets tokens
      args = OptionSet.tokenize(options);      
    } catch (Exception e) {
      e.printStackTrace();
    }
    // The JTB syntax is "-o" "foo" and not "-o=foo"
    int nb = 0;
    for(int i = 0; i < args.length; i++)
      if (args[i].indexOf('=') != -1)
        nb++;
    String[] nargs = new String[args.length+nb];
    nb = 0;
    for(int i = 0; i < args.length; i++) {
      if (args[i].indexOf('=') != -1) {
        nargs[i+nb] = args[i].substring(0, args[i].indexOf('=') );
        nb++;
        nargs[i+nb] = args[i].substring(args[i].indexOf('=')+1 );
      }
      else
        nargs[i+nb] = args[i];
    }
    return nargs;
  }
  
  /**
   * Provide the path to javacc.jar
   * @param file, the resource to get the property from
   * @return String path to javacc.jar
   */
  protected static String getJavaCCJarFile(IResource file) {
    String jarfile = null;
    try {
      // If the user has given a path, we use it
      jarfile = file.getProject().getPersistentProperty(QN_RUNTIME_JAR);
      // else we use the jar in the plugin
      if (jarfile == null || jarfile.equals("") || jarfile.startsWith("-")) {//$NON-NLS-1$ //$NON-NLS-2$
	URL installURL = Activator.getDefault().getBundle().getEntry("/"); //$NON-NLS-1$
	// Eclipse 3.1 way. Deprecated in 3.2
	URL resolvedURL = Platform.resolve(installURL);
	String home = Platform.asLocalURL(resolvedURL).getFile();
	// Eclipse 3.2 way. Only available in Eclipse 3.2
//	  URL resolvedURL = org.eclipse.core.runtime.FileLocator.resolve(installURL);
//	  String home = org.eclipse.core.runtime.FileLocator.toFileURL(resolvedURL).getFile();
	// Same for both
	jarfile = home + "javacc.jar"; //$NON-NLS-1$
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    return jarfile;
  }
  
  /**
   * Provide the path to JTB.jar
   * @param file, the resource to get the property from
   * @return String path to jtb132.jar
   */
  protected static String getJTBJarFile(IFile file) {
    String jarfile = null;
    try {
      // If the user has given a path, we use it
      jarfile = file.getProject().getPersistentProperty(QN_RUNTIME_JTBJAR);
      // else we use the jar in the plugin
      if (jarfile == null || jarfile.equals("") || jarfile.startsWith("-")) {//$NON-NLS-1$ //$NON-NLS-2$
	URL installURL = Activator.getDefault().getBundle().getEntry("/"); //$NON-NLS-1$
	// Eclipse 3.1 way. Deprecated in 3.2
	URL resolvedURL = Platform.resolve(installURL);
	String home = Platform.asLocalURL(resolvedURL).getFile();
	// Eclipse 3.2 way. Only available in Eclipse 3.2
//	  URL resolvedURL = org.eclipse.core.runtime.FileLocator.resolve(installURL);
//	  String home = org.eclipse.core.runtime.FileLocator.toFileURL(resolvedURL).getFile();
	// Same for both
	jarfile = home + "jtb132.jar"; //$NON-NLS-1$
	if (jarfile.startsWith("/"))
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
