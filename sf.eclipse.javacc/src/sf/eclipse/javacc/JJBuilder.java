package sf.eclipse.javacc;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sf.eclipse.javacc.options.OptionSet;

/**
 * Builder for .jj and .jjt files
 * It extends IncrementalProjectBuilder and is referenced by plugin.xml.
 * It is also used to compile files via static methods. 
 * 
 * @author Remi Koutcherawy 2003 
 * GPL Licence www.gnu.org/licenses/gpl.txt
 */
public class JJBuilder extends IncrementalProjectBuilder
  implements IResourceDeltaVisitor, IResourceVisitor, IConstants {
    
  static IProgressMonitor monitor;
  static boolean useJarLoader = false;
  
  /** 
   * Constructor
   */
  public JJBuilder() {
    useJarLoader = "true".equals(JavaccPlugin.getResourceString("JJBuilder.Use_JarLoader")); //$NON-NLS-1$ //$NON-NLS-2$
  }
  
 /**
  * Invoked in response to a call to one of the <code>IProject.build</code>
  * @see org.eclipse.core.internal.events.InternalBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
  */
  protected IProject[] build(int kind, Map args, IProgressMonitor mon)
    throws CoreException {
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
    JJBuilder.monitor = mon;
    getProject().accept(this);
  }
  
  /**
   * Do an incremental Build or a Full build if no delta is available 
   * @param monitor
   * @throws CoreException
   */
  protected void incrementalBuild(IProgressMonitor mon)throws CoreException {
    clearConsole();
    JJBuilder.monitor = mon;
    
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
    IPath path = res.getFullPath();
    if ("jj".equals(path.getFileExtension()) //$NON-NLS-1$
      || "jjt".equals(path.getFileExtension())) //$NON-NLS-1$
      CompileJJResource(res);
    // This exludes traversing the bin directory
    return !res.toString().endsWith("bin"); //$NON-NLS-1$
  }

  /**
   * Compile a .jj or .jjt file given its IResource
   * @param res IResource to compile
   */
  public static void CompileJJResource(IResource res) throws CoreException {
    if (!(res instanceof IFile) || !res.exists())
      return;
    IFile file = (IFile) res;
    IProject pro = res.getProject();
    String dir = pro.getLocation().toOSString();
    String name = file.getLocation().toString();
    String shortName = name.substring(dir.length() + 1);
    
    if (!(name.endsWith(".jj") || name.endsWith(".jjt"))) //$NON-NLS-1$ //$NON-NLS-2$
      return;
    JJConsole console = JavaccPlugin.getConsole();
    if (DEBUG) System.out.println("Compile " + res); //$NON-NLS-1$

    // Retrieves runtime options
    boolean projectOverride = false;
    boolean showConsole = false;
    try {
      projectOverride = ("true").equals(pro.getPersistentProperty(//$NON-NLS-1$
        QN_PROJECT_OVERRIDE));
      showConsole = ("true").equals(pro.getPersistentProperty(//$NON-NLS-1$
        QN_SHOW_CONSOLE));
    } catch (CoreException e) {
      e.printStackTrace();
    }
           
    // Retrieves command line
    String[] args = getArgs(res, name, projectOverride);
    String jarfile = getJavaCCJarFile(res);
    if (useJarLoader == false && jarfile == null) {
      IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
      MessageDialog.openInformation(
        w.getShell(),
        JavaccPlugin.getResourceString("JJBuilder.JJBuilder"), //$NON-NLS-1$
        JavaccPlugin.getResourceString("JJBuilder.Please_set_the_jar_file")); //$NON-NLS-1$
      return;
    }
    
    // Redirects standart and error streams
    PrintStream orgOut = System.out;
    PrintStream orgErr = System.err;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
    System.setErr(new PrintStream(baos));
    
    // Calls JavaCC
    DirList.snapshot(dir);
    if (name.endsWith(".jj")) { //$NON-NLS-1$
      if (useJarLoader)
        JarLoader.launchJavaCC(jarfile, args, dir);
      else
        JarLauncher.launchJavaCC(jarfile, args, dir);
      if (console != null && showConsole)
        console.addRedText(">javacc "); //$NON-NLS-1$
    } else if (name.endsWith(".jjt")) { //$NON-NLS-1$
      if (useJarLoader)
        JarLoader.launchJJTree(jarfile, args, dir);
      else
        JarLauncher.launchJJTree(jarfile, args, dir);
      if (console != null && showConsole)
        console.addRedText(">jjtree "); //$NON-NLS-1$
    }
    
    // Recalls args on console
    if (console != null && showConsole) {
      for (int i = 0; i < args.length; i++)
        console.addRedText(args[i] + " "); //$NON-NLS-1$
      console.addText("\n"); //$NON-NLS-1$
    }
    
    // Restores standard and error streams
    System.setOut(orgOut);
    System.setErr(orgErr);
          
    // Sends result to console
    String result = baos.toString();
    try {
      baos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (console != null && showConsole) {
      console.addText(result);
      console.addText("\n"); //$NON-NLS-1$
      console.show();
    }
    
    // Reports Errors
    markErrors(res, result);

    // Compiles Generated .jj File if a .jjt file was processed
    String[] jjgenerated = DirList.getDiff(dir);
    if (jjgenerated != null) {
      for (int i = 0; i < jjgenerated.length; i++) {
        jjgenerated[i] = jjgenerated[i].substring(dir.length() + 1);
        IResource resgenerated = pro.findMember(jjgenerated[i]);
        if (resgenerated == null) {
          pro.refreshLocal(IResource.DEPTH_INFINITE, monitor);
          resgenerated = pro.findMember(jjgenerated[i]);
        }
        if (resgenerated != null) {
          resgenerated.setDerived(true);
          resgenerated.setPersistentProperty(QN_GENERATED_FILE, shortName);
        }
        if (name.endsWith(".jjt") && jjgenerated[i].endsWith(".jj")) { //$NON-NLS-1$ //$NON-NLS-2$
          // Compile .jj if project has not Javacc Nature, ie no automtic build
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
    IProject pro = res.getProject();
    String dir = pro.getLocation().toOSString();
    String name = file.getLocation().toString();
    // String shortName = name.substring(dir.length() + 1);
    
    if (!(name.endsWith(".jj") || name.endsWith(".jjt"))) //$NON-NLS-1$ //$NON-NLS-2$
      return;
    JJConsole console = JavaccPlugin.getConsole();
    if (DEBUG) System.out.println("JJDoc " + res); //$NON-NLS-1$
        
    // Retrieves runtime options
    boolean projectOverride = false;
    boolean showConsole = false;
    try {
      projectOverride = ("true").equals(pro.getPersistentProperty( //$NON-NLS-1$
        QN_PROJECT_OVERRIDE));
      showConsole = ("true").equals(pro.getPersistentProperty( //$NON-NLS-1$
        QN_SHOW_CONSOLE));   
    } catch (CoreException e) {
      e.printStackTrace();
    }
           
    // Retrieves command line
    String[] args = getJJDocArgs(res, name, projectOverride);
    String jarfile = getJavaCCJarFile(res);
    if (useJarLoader == false && jarfile == null) {
      IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
      MessageDialog.openInformation(
        w.getShell(),
        JavaccPlugin.getResourceString("JJBuilder.JJBuilder"), //$NON-NLS-1$
        JavaccPlugin.getResourceString("JJBuilder.Please_set_the_jar_file")); //$NON-NLS-1$
      return;
    }

    // Redirects standart and error streams
    PrintStream orgOut = System.out;
    PrintStream orgErr = System.err;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
    System.setErr(new PrintStream(baos));
    
    // Calls JJDoc
    if (useJarLoader)
      JarLoader.launchJJDoc(jarfile, args, dir);
    else
      JarLauncher.launchJJDoc(jarfile, args, dir);
          
    // Restores standart and error streams
    System.setOut(orgOut);
    System.setErr(orgErr);
          
    // Recall command line on console
    if (console != null && showConsole) {
        console.addRedText(">jjdoc "); //$NON-NLS-1$
      for (int i = 0; i < args.length; i++)
        console.addRedText(args[i] + " "); //$NON-NLS-1$
      console.addText("\n"); //$NON-NLS-1$
    }
    
    // Sends result to console
    String result = baos.toString();
    try {
      baos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (console != null && showConsole) {
      console.addText(result);
      console.addText("\n"); //$NON-NLS-1$
      console.show();
    }
    
    // Reports Errors
    markErrors(res, result);
  }
  
  /**
   * Make args[] of options to call JavaCC Compiler
   * @param res, resource to get the options from
   * @param name, file name
   * @param projectOverride, true if the properties is from project
   * @return String[] of options to call JavaCC compiler with
   */
  protected static String[] getArgs(IResource res, String name, boolean projectOverride) {
    String[] args = null;
    try {
      String options = null;
      if (name.endsWith(".jj")) { //$NON-NLS-1$
        // Try for resource property
        options = res.getPersistentProperty(QN_JAVACC_OPTIONS);
        // Else take Project Property
        if (options == null || projectOverride)
          options = res.getProject().getPersistentProperty(QN_JAVACC_OPTIONS);
        // Else takes defaults
        if (options == null) {
          options = ""; //$NON-NLS-1$
        }
      }
      else if (name.endsWith(".jjt")) { //$NON-NLS-1$
        options = res.getPersistentProperty(QN_JJTREE_OPTIONS);
        if (options == null || projectOverride)
          options =  res.getProject().getPersistentProperty(QN_JJTREE_OPTIONS);
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
   * @param res, the resource to get the properties from
   * @param projectOverride, true if the properties is from project
   * @return String[] of options to call JavaCC compiler with
   */
  protected static String[] getJJDocArgs(IResource res, String name, boolean projectOverride) {
    String[] args = null;
    try {
      String options = null;
      // Try for resource property
      options = res.getPersistentProperty(QN_JJDOC_OPTIONS);
      // Else take Project Property
      if (options == null || projectOverride)
        options = res.getProject().getPersistentProperty(QN_JJDOC_OPTIONS);
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
   * @param res, the resource to get the property from
   * @return String path to javacc.jar
   */
  protected static String getJavaCCJarFile(IResource res) {
    String jarfile = null;
    try {
      // Maybe one can imagine to compile 2 files with different JavaCC versions.
      // For now we use the same version for all files in the project.
      jarfile = res.getProject().getPersistentProperty(QN_RUNTIME_JAR);
      if (jarfile == null || jarfile.equals("")) //$NON-NLS-1$
        jarfile = null;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return jarfile;
  }
  
  /**
   * Decode output to catch lines reporting errors
   */
  static void markErrors(IResource file, String res) {
    try {
      file.deleteMarkers(IMarker.PROBLEM, false, IResource.DEPTH_ZERO);
    } catch (CoreException e) {
      e.printStackTrace();
    }
    // We get outputs like : Error at line 14, column 15
//    Matcher m = Pattern.compile("(.+)[lL]ine (\\d+), [cC]olumn (\\d+)[^\r\n]+").matcher(res);
//    while (m.find()) {
//      m.group();
//      int line = Integer.parseInt(m.group(2));
//      int col = Integer.parseInt(m.group(3));
//      System.out.println("line " + line + " col " + col);
//      markError(file, m.group(0), IMarker.SEVERITY_ERROR, line);
//    }
    // Substitution for Regexp to use only Java 1.3
    try {
      StringReader sr = new StringReader(res);
      BufferedReader br = new BufferedReader(sr);
      String lineStr = null;
      while ((lineStr = br.readLine()) != null) {
        int line = parseLine(lineStr);
        if (line != -1) {
          if (lineStr.indexOf("Warning") != -1) //$NON-NLS-1$
            markError(file, lineStr, IMarker.SEVERITY_WARNING, line);
          else
            markError(file, lineStr, IMarker.SEVERITY_ERROR, line);
        }
      }
      br.close();
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }
  
  /**
   * Look for Errors reported by JavaCC
   */
  public static int parseLine(String res){
    int l1=-1, l2=-1;
    l1 = res.indexOf("line"); //$NON-NLS-1$
    l2 = res.indexOf("Line"); //$NON-NLS-1$
    if (l1 == -1) l1 = l2;
    int c1=-1, c2=-1;
    c1 = res.indexOf("column"); //$NON-NLS-1$
    c2 = res.indexOf("Column"); //$NON-NLS-1$
    if (c1 == -1) c1 = c2;
    if (l1 != -1 && c1 != -1)
      return Integer.parseInt(res.substring(l1+5,res.indexOf(","))); //$NON-NLS-1$
    else return -1;
  }
  
  /**
   * Add a marker to signal an error or a warning
   */
  static void markError(IResource res, String s, int type, int line) {
    try {
      if (res != null) {
        IMarker im =
          res.createMarker("org.eclipse.core.resources.problemmarker"); //$NON-NLS-1$
        String attNames[] = { "message", "severity", "lineNumber" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        Object attValues[] = { s, new Integer(type), new Integer(line)};
        im.setAttributes(attNames, attValues);
      }
    } catch (CoreException ex) {
      System.err.println(JavaccPlugin.getResourceString("JJBuilder.Exception_setting_marker") + ex); //$NON-NLS-1$
      ex.printStackTrace();
    }
  }
  
  /**
   * Clear Console if a Console is available
   * @throws CoreException
   */
  protected void clearConsole() throws CoreException {
    boolean clr = ("true").equals(getProject().getPersistentProperty(//$NON-NLS-1$
      QN_CLEAR_CONSOLE));
    if (clr) {
      JJConsole console = JavaccPlugin.getConsole();
      if (console != null)
        console.clear();
    }
  }
}
