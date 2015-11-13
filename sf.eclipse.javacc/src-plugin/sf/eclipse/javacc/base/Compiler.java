package sf.eclipse.javacc.base;

import static sf.eclipse.javacc.base.IConstants.*;
import static sf.eclipse.javacc.base.JarLauncher.sJavaCmd;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * Compiler for .jj, .jjt and .jtb files.<br>
 * It is used by the build process and by the compile commands.<br>
 * 
 * @since 1.5.29 (from when extracted from {@link Builder})
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 */
public class Compiler {

  // MMa 01/2015 : created from Builder : see following history
  // MMa 11/2009 : javadoc and formatting revision ; changed jar names
  // MMa 02/2010 : formatting and javadoc revision ; fixed issue for JTB problems reporting
  // MMa 03/2010 : change on QN_GENERATED_FILE for bug 2965665 fix
  // MMa 08/2011 : added mark generated files as derived option RFE 3314103
  // MMa 08/2011 : added modification of the JavaCC file generated checksum if suppress warning option set
  // MMa 08/2011 : fixed use of deprecated method in Eclipse 3.6+
  // BF  06/2012 : documented empty block to prevent warning message
  // MMa 10/2012 : added JVM options option and keep deleted files in history option ; removed static methods
  //               renamed ; moved methods from Compile and JJDocCompile
  // MMa 11/2014 : refactored initializations ; changed markers ; some renamings ; enhanced output to Console
  //               modified some modifiers ; changed package
  // MMa 12/2014 : changed jars directory
  // MMa 01/2015 : added methods to display information for debugging problems in launching the compile commands
  //               refactored initialization ; changed from array to list the command passed to ProcessBuilder
  // MMa 11/2015 : improved addition of suppresswarning annotation on all classes in the derived classes

  /** The project */
  private IProject               jProject          = null;

  /** The java project */
  private IJavaProject           jJavaProject      = null;

  /** The project's JavaCC preferences */
  private IEclipsePreferences    jPrefs            = null;

  /** A flag to tell whether to mark derived files (from the preferences) */
  private boolean                jDerived          = false;

  /** A flag to tell whether to suppress warnings in the derived files (from the preferences) */
  private boolean                jSuppressWarnings = false;

  /** A buffer */
  private final StringBuilder    jSb               = new StringBuilder(10240);

  /** Compile level counter (for clearing or not the Console) */
  private int                    jCLC              = -1;

  /**
   * Regular expression to capture the class declaration, including potential @SuppressWarnings annotations.<br>
   * (?:@SuppressWarnings\\(\\\"(?:all|serial|unused)\\\"\\)..?)? : non capturing group, once or not at all.<br>
   * ((?:public )?(?:final )?(?:class|interface|enum)) : capturing group $1.<br>
   * This group $1 will by prefixed by a new line containing <code>@SuppressWarnings(\"all\")</code> and will
   * replace the whole string (group $0).
   */
  protected final static String  sClassDeclRegExpr = "^(?:@SuppressWarnings\\(\\\"(?:all|serial|unused)\\\"\\).?.?)?" //$NON-NLS-1$
                                                     + "((?:public )?(?:final )?(?:class|interface|enum))"; //NON-NLS-1$ //$NON-NLS-1$
  /** Corresponding pattern */
  protected final static Pattern sClassDeclPatt    = Pattern.compile(sClassDeclRegExpr, Pattern.MULTILINE
                                                                                        | Pattern.DOTALL);
  /** Replacement string */
  protected final static String  sReplStr          = "@SuppressWarnings(\"all\")" + LS + "$1";             //$NON-NLS-1$ //$NON-NLS-2$

  /** Array of hexadecimal characters */
  protected final static char[]  HEX_DIGITS        = new char[] {
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

  /**
   * The algorithm used by JavaCC to compute the checksum.<br>
   * Duplicated from JavaCC 5.0 org.javacc.parser.OutputFile constant
   */
  protected static final String  MD5_ALGO          = "MD5";                                                //$NON-NLS-1$
  /**
   * Beginning of the checksum line generated by JavaCC.<br>
   * Duplicated from JavaCC 5.0 org.javacc.parser.OutputFile.MD5_LINE_PART_1
   */
  protected static final String  MD5_LINE_PART_1   = "/* JavaCC - OriginalChecksum=";                      //$NON-NLS-1$
  /**
   * End of the checksum line generated by JavaCC.<br>
   * Duplicated from JavaCC 5.0 org.javacc.parser.OutputFile.MD5_LINE_PART_2
   */
  protected static final String  MD5_LINE_PART_2   = " (do not edit this line) */";                        //$NON-NLS-1$

  /**
   * Instantiated by the the compile commands.
   */
  public Compiler() {
  }

  /**
   * Instantiated by the build process.
   * 
   * @param aProject - the project we are working on
   */
  public Compiler(final IProject aProject) {
    setProjectInfo(aProject);
  }

  /**
   * Sets different information regarding from the project.
   * 
   * @param aRes - the resource we are working on
   */
  private void setProjectInfo(final IResource aRes) {
    jProject = aRes.getProject();
    jJavaProject = JavaCore.create(jProject);
    jPrefs = new ProjectScope(jProject).getNode(PLUGIN_QN);
  }

  /**
   * Compiles a jj/jjt/jtb resource.
   * 
   * @param aRes - the resource
   */
  public void jj_compile(final IResource aRes) {
    if (aRes == null || !(aRes instanceof IFile)) {
      return;
    }
    setProjectInfo(aRes);
    try {
      // touch the file
      aRes.touch(null);
      // explicitly compile the file if the project has a nature, if the file is on the classpath and if
      //  the project is not in auto build mode (if the project is in auto build mode, touching it will
      //  trigger the build(...) method
      if (!("true").equals(jPrefs.get(NATURE, "false")) //$NON-NLS-1$ //$NON-NLS-2$
          || !isJJFileAndOnClasspath(aRes) || !aRes.getWorkspace().isAutoBuilding()) {
        compileResource(aRes, true);
      }
      // refresh the whole project to trigger compilation of generated .jj and .java files
      aRes.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
    } catch (final CoreException e) {
      AbstractActivator.logBug(e);
    }
    return;
  }

  /**
   * Compiles a .jj, .jjt or .jtb file given its {@link IResource}.<br>
   * Called by {@link Builder#visit(IResource)} and {@link #jj_compile(IResource)} and recursively.
   * 
   * @param aRes - the IResource to compile
   * @param aClearConsole - true to clear the console, false otherwise
   * @exception CoreException if this compile fails
   */
  void compileResource(final IResource aRes, final boolean aClearConsole) throws CoreException {
    if (!(aRes instanceof IFile) || !aRes.exists()) {
      return;
    }

    // the file, the project and the directory
    final IFile file = (IFile) aRes;
    final String projectDir = jProject.getLocation().toOSString();

    // delete problem markers
    try {
      file.deleteMarkers(IMarker.PROBLEM, false, IResource.DEPTH_ZERO);
    } catch (final CoreException e) {
      AbstractActivator.logBug(e);
    }

    // JavaCC / JTB are launched in the directory where the file is
    final String resPath = file.getLocation().toString();
    final String resName = resPath.substring(resPath.lastIndexOf("/") + 1); //$NON-NLS-1$
    final String resDir = resPath.substring(0, resPath.lastIndexOf("/")); //$NON-NLS-1$
    final String resExt = file.getFullPath().getFileExtension();
    final String resRelPath = resPath.substring(projectDir.length());

    final IConsole console = AbstractActivator.getDefault().getConsole();
    // have seen the case of null, but don't know why null can occur
    // one case is when launching Eclipse with a JJEditor on a grammar with problems
    Assert.isNotNull(console);
    // clear only once
    jCLC++;
    if (aClearConsole && jCLC == 0) {
      final String clearStr = jPrefs.get(CLEAR_CONSOLE, DEF_CLEAR_CONSOLE);
      final boolean clear = ("true").equals(clearStr); //$NON-NLS-1$
      if (clear) {
        console.clear();
      }
      else {
        console.println();
      }
    }

    // retrieve command line
    final String[] args = getArgs(file, resName);
    final String jarfile = getJarFile(file, null);
    if (jarfile == null) {
      console.print("Error !!! Unable to find jar file (for extension " + resExt //$NON-NLS-1$
                    + "), check the JavaCC options of the project", true); //$NON-NLS-1$
      console.println(console.fmtTS(), false);
      return;
    }
    // we trim the options to avoid empty tokens passed to the ProcessBuilder
    final String jvmOptions = getJvmOptions().trim();

    // save standard out and error streams
    final PrintStream orgOut = System.out;
    final PrintStream orgErr = System.err;
    // redirect out and error streams
    final PrintStream consolePS = console.getPrintStream();
    System.setOut(consolePS);
    System.setErr(consolePS);

    // build the command line
    final List<String> cmd = new ArrayList<String>(6 + args.length);
    cmd.add(sJavaCmd);
    if (jvmOptions.length() > 0) {
      cmd.add(jvmOptions);
    }
    if (resExt.equals("jj")) { //$NON-NLS-1$ 
      cmd.add(CLASSPATH_ARG);
      cmd.add(jarfile);
      cmd.add(JAVACC_ARG);
    }
    else if (resExt.equals("jjt")) { //$NON-NLS-1$
      cmd.add(CLASSPATH_ARG);
      cmd.add(jarfile);
      cmd.add(JJTREE_ARG);
    }
    else if (resExt.equals("jtb")) { //$NON-NLS-1$
      cmd.add(JAR_ARG);
      cmd.add(jarfile);
    }
    // args[0] is the file name
    for (int i = 0; i < args.length; i++) {
      cmd.add(args[i]);
    }
    displayCommand(console, cmd);

    // call JavaCC, JJTree or JTB
    DirList.snapshot(projectDir);
    final boolean isJtb = false;
    JarLauncher.pb_launch(cmd, resDir);

    // restore standard out and error streams
    System.setOut(orgOut);
    System.setErr(orgErr);

    // notify the console with the file the console should report errors to
    console.processReport(file, isJtb);

    // compile the generated .jj file if a .jjt or .jtb file was processed
    final String[] generatedFiles = DirList.getDiff(projectDir);
    if (generatedFiles != null) {
      // reread preferences only once a compilation launch
      jDerived = "true".equals(jPrefs.get(MARK_GEN_FILES_DERIVED, "true")); //$NON-NLS-1$ //$NON-NLS-2$
      jSuppressWarnings = "true".equals(jPrefs.get(SUPPRESS_WARNINGS, "false")); //$NON-NLS-1$ //$NON-NLS-2$
      final int prjdirlenp1 = projectDir.length() + 1;
      for (int i = 0; i < generatedFiles.length; i++) {
        final String genFileName = generatedFiles[i].substring(prjdirlenp1);
        IResource genFileRes = jProject.findMember(genFileName);
        if (genFileRes == null) {
          jProject.refreshLocal(IResource.DEPTH_INFINITE, null);
          genFileRes = jProject.findMember(genFileName);
        }
        // compile .jj only if .jjt or .jtb was compiled and .jj was generated
        if (genFileName.endsWith(".jj") && (resExt.equals("jjt") || resExt.equals("jtb"))) { //$NON-NLS-1$ //$NON-NLS-2$  //$NON-NLS-3$
          // compile .jj if project has not JavaCC Nature, i.e. no automatic build
          // well it seems Eclipse has a small bug here, it doesn't recompile ...
          //          if (!project.getDescription().hasNature(NATURE_ID) || !project.getWorkspace().isAutoBuilding()) {
          //          if (!("true").equals(prefs.get(NATURE, "false")) //$NON-NLS-1$ //$NON-NLS-2$
          //              || !project.getWorkspace().isAutoBuilding()) {
          compileResource(genFileRes, aClearConsole);
          //          }
        }
        // mark them with a decorating 'G' and alter .java files
        markAsDerivedAndAlter(resRelPath, genFileRes, genFileName.endsWith(".java")); //$NON-NLS-1$
      }
    }
    jCLC--;
  }

  /**
   * Marks the generated file as derived and adds the @SuppressWarnings annotation according to corresponding
   * preference.
   * 
   * @param aRelPath - the path to the grammar file (relative to the project) this resource is generated from
   * @param aRes - the IResource to mark and to correct
   * @param aAlter - true if alter the file, false otherwise
   * @throws CoreException - see {@link IResource#setDerived(boolean, IProgressMonitor)}
   */
  private void markAsDerivedAndAlter(final String aRelPath, final IResource aRes, final boolean aAlter)
                                                                                                       throws CoreException {
    // mark
    // the calls cost a lot, so don't do it if the properties are already set
    if (jDerived != aRes.isDerived()) {
      aRes.setDerived(jDerived, null);
    }
    if (!aRelPath.equals(aRes.getPersistentProperty(GEN_FILE_QN))) {
      aRes.setPersistentProperty(GEN_FILE_QN, aRelPath);
    }
    // alter if set in preferences
    final IJavaElement element = (IJavaElement) aRes.getAdapter(IJavaElement.class);
    if (jSuppressWarnings && aAlter && element instanceof ICompilationUnit) {
      // direct access to the file !
      final String filename = ((IFile) aRes).getLocation().toOSString();
      final String source = FileUtils.getFileContents(filename);
      final Matcher matcher = sClassDeclPatt.matcher(source);
      if (matcher.find()) {
        // add the annotation
        //        String newsource = matcher.replaceFirst(sReplStr);
        String newsource = matcher.replaceAll(sReplStr);
        final int ix = newsource.indexOf(MD5_LINE_PART_1);
        if (ix >= 0) {
          // strip the old checksum line
          newsource = newsource.substring(0, ix);
          // compute the new checksum and add a new checksum line
          final String chksum = computeCheksum(newsource);
          jSb.setLength(0);
          jSb.append(newsource).append(MD5_LINE_PART_1).append(chksum).append(MD5_LINE_PART_2).append(LS);
          newsource = jSb.toString();
        }
        // save the file
        FileUtils.saveFileContents(filename, newsource);
      }
    }
  }

  /**
   * Recomputes the last line checksum.<br>
   * Duplicated from JavaCC 5.0 org.javacc.parser.OutputFile.java OutputFile() constructor.
   * 
   * @param aSource - the source to modify
   * @return the modified source
   */
  private String computeCheksum(final String aSource) {
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance(MD5_ALGO);
    } catch (final NoSuchAlgorithmException e) {
      AbstractActivator.logErr("No MD5 implementation (should not happen as JavaCC should use it also)"); //$NON-NLS-1$
      // do not modify the source
      return aSource;
    }
    final DigestOutputStream dios = new DigestOutputStream(new NullOutputStream(), digest);
    final PrintWriter pw = new PrintWriter(dios);
    pw.print(aSource);
    pw.close();
    final byte[] digestStr = dios.getMessageDigest().digest();
    final String newChecksum = toHexString(digestStr);
    return newChecksum;
  }

  /**
   * Converts a byte array into an hexadecimal string.
   * 
   * @param aBytes - a byte array
   * @return the hexadecimal string
   */
  private String toHexString(final byte[] aBytes) {
    jSb.setLength(0);
    for (int i = 0; i < aBytes.length; i++) {
      final byte b = aBytes[i];
      jSb.append(HEX_DIGITS[(b & 0xF0) >> 4]).append(HEX_DIGITS[b & 0x0F]);
    }
    return jSb.toString();
  }

  /**
   * Compiles a resource.
   * 
   * @param aRes - the resource
   */
  public void jjdoc_compile(final IResource aRes) {
    if (aRes == null || !(aRes instanceof IFile)) {
      return;
    }
    setProjectInfo(aRes);
    // call GenDoc
    genDocForJJResource(aRes);
    // refreshing the whole project (just to show the generated .html)
    // has the side effect to clear the Console if automatic build is on
    try {
      aRes.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
    } catch (final CoreException e) {
      AbstractActivator.logBug(e);
    }
    return;
  }

  /**
   * Calls JJDoc for a .jj, .jjt or .jtb file given its IResource.
   * 
   * @param aRes - the IResource to JJDoc
   */
  private void genDocForJJResource(final IResource aRes) {
    if (!(aRes instanceof IFile)) {
      return;
    }
    final IFile file = (IFile) aRes;
    final IProject project = file.getProject();
    final String resDir = project.getLocation().toOSString();
    final String resName = file.getLocation().toString();
    final IConsole console = AbstractActivator.getDefault().getConsole();

    // retrieve command line
    final String[] args = getJJDocArgs(resName);
    final String jarfile = getJarFile(file, "jj"); //$NON-NLS-1$
    final String jvmOptions = getJvmOptions();

    // redirect standard and error streams
    final PrintStream orgOut = System.out;
    final PrintStream orgErr = System.err;
    final PrintStream consolePS = console.getPrintStream();
    System.setOut(consolePS);
    System.setErr(consolePS);

    // build the command line 
    final List<String> cmd = new ArrayList<String>(6);
    cmd.add(sJavaCmd);
    cmd.add(jvmOptions);
    cmd.add(CLASSPATH_ARG);
    cmd.add(jarfile);
    cmd.add(JJDOC_ARG);
    // args[0] is the file name
    for (int i = 0; i < args.length; i++) {
      cmd.add(args[i]);
    }
    displayCommand(console, cmd);

    // call JJDoc
    //    JarLauncher.launchJJDoc(jvmOptions, jarfile, args, resDir);
    JarLauncher.pb_launch(cmd, resDir);
    console.println();

    // restore standard and error streams
    System.setOut(orgOut);
    System.setErr(orgErr);
    consolePS.close();

    // notify Console with the file the Console should report errors to
    console.processReport(file, false);
  }

  /**
   * Builds the array of options to call the JavaCC, JJTree or JTB Compiler with.
   * 
   * @param aFile - the resource to get the options for
   * @param aName - the file name to compile
   * @return the options to call the JavaCC / JJTree / JTB compiler with
   */
  private String[] getArgs(final IFile aFile, final String aName) {
    String[] args = null;
    final String extension = aFile.getFullPath().getFileExtension();
    String options = null;
    try {
      if (extension.equals("jj")) { //$NON-NLS-1$
        options = jPrefs.get(JAVACC_OPTIONS, ""); //$NON-NLS-1$
      }
      else if (extension.equals("jjt")) { //$NON-NLS-1$
        options = jPrefs.get(JJTREE_OPTIONS, ""); //$NON-NLS-1$
      }
      else if (extension.equals("jtb")) { //$NON-NLS-1$
        options = jPrefs.get(JTB_OPTIONS, ""); //$NON-NLS-1$
      }
      if (options == null) {
        options = ""; //$NON-NLS-1$
      }
    } catch (final Exception e) {
      AbstractActivator.logBug(e);
      return null;
    }

    // add target file to compile
    options = options + " \"" + aName + "\""; //$NON-NLS-1$ //$NON-NLS-2$

    // get tokens
    args = OptionSet.tokenize(options);

    if (!extension.equals("jtb")) { //$NON-NLS-1$
      // finished for .jj & .jjt
      return args;
    }

    // the JTB syntax is "-o" "foo" and not "-o=foo"
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
   * @param aName - the file name to compile
   * @return the array of options to call the JJDoc Compiler with
   */
  private String[] getJJDocArgs(final String aName) {
    String[] args = null;
    try {
      String options = null;
      // read project properties
      options = jPrefs.get(JJDOC_OPTIONS, ""); //$NON-NLS-1$
      // else take default
      if (options == null) {
        options = ""; //$NON-NLS-1$
      }
      // add target i.e. file to compile
      options = options + " \"" + aName + " \""; //$NON-NLS-1$ //$NON-NLS-2$
      // get tokens
      args = OptionSet.tokenize(options);
    } catch (final Exception e) {
      AbstractActivator.logBug(e);
    }
    return args;
  }

  /**
   * @return the full path to the jars directory or null in case of error
   */
  public static String getJarsDir() {
    String dir = null;
    try {
      final URL installURL = AbstractActivator.getDefault().getBundle().getEntry(JARS_DIR);
      final URL resolvedURL = FileLocator.resolve(installURL);
      dir = FileLocator.toFileURL(resolvedURL).getFile();
      // returned String is like "/C:/workspace/sf.eclipse.javacc/jtb132.jar"
      if (dir.startsWith("/") && dir.startsWith(":", 2)) { //$NON-NLS-1$ //$NON-NLS-2$
        dir = dir.substring(1);
      }
    } catch (final IOException e) {
      AbstractActivator.logBug(e, "unable to find the jars directory"); //$NON-NLS-1$
    }
    return dir;
  }

  /**
   * Retrieves the path to the jar file (from the preferences or from the plug-in).
   * 
   * @param aFile - the IResource to get the jar file for
   * @param aExtension - if not null, the overriding extension (for jtb files)
   * @return the path to the jar file, or null in case of error
   */
  private String getJarFile(final IResource aFile, final String aExtension) {
    final String extension = (aExtension == null ? aFile.getFullPath().getFileExtension() : aExtension);
    return getJarFile(extension);
  }

  /**
   * Retrieves the path to the jar file (from the preferences or from the plug-in).
   * 
   * @param aExtension - the extension (must be not null)
   * @return the path to the jar file, or null in case of error
   */
  private String getJarFile(final String aExtension) {
    String jarfile = null;
    // use the path in preferences
    if (aExtension.equals("jj") || aExtension.equals("jjt")) {//$NON-NLS-1$ //$NON-NLS-2$
      jarfile = jPrefs.get(RUNTIME_JJJAR, ""); //$NON-NLS-1$
    }
    else if (aExtension.equals("jtb")) { //$NON-NLS-1$
      jarfile = jPrefs.get(RUNTIME_JTBJAR, ""); //$NON-NLS-1$
    }
    // else we use the plugin's jars
    if (jarfile == null || jarfile.equals("")) {//$NON-NLS-1$
      jarfile = getDefaultJarFile(aExtension);
    }
    if (jarfile == null) {
      // should not occur
      return null;
    }
    String jarfile2 = null;
    try {
      jarfile2 = VariablesPlugin.getDefault().getStringVariableManager()
                                .performStringSubstitution(jarfile, true);
      // on Windows this returns "/C:/workspace/sf.eclipse.javacc/jtb132.jar"
      // as this will fails, we remove the first "/" if there is ":" at index 2
      if (jarfile2.startsWith("/") && jarfile2.startsWith(":", 2)) { //$NON-NLS-1$ //$NON-NLS-2$
        jarfile2 = jarfile2.substring(1);
      }
    } catch (final CoreException e) {
      AbstractActivator.logBug(e, "couldn't resolve jar file", jarfile); //$NON-NLS-1$ 
      // jarfile2 will keep it's previous value, which will fail in launch()
    }
    return jarfile2;
  }

  /**
   * Retrieves the path to the default jar file (from the plug-in).
   * 
   * @param aExtension - the extension (must be not null)
   * @return the path to the default jar file, or null in case of error
   */
  public static String getDefaultJarFile(final String aExtension) {
    final String jarsdir = getJarsDir();
    if (jarsdir == null) {
      return null;
    }
    if (aExtension.equals("jj") || aExtension.equals("jjt")) { //$NON-NLS-1$ //$NON-NLS-2$
      return jarsdir + JAVACC_JAR_NAME;
    }
    else if (aExtension.equals("jtb")) { //$NON-NLS-1$
      return jarsdir + JTB_JAR_NAME;
    }
    // should not occur
    return null;
  }

  /**
   * Retrieves the JVM options as a String (from the preferences or from the plug-in).
   * 
   * @return the JVM options
   */
  private String getJvmOptions() {
    String jvmOptions = ""; //$NON-NLS-1$
    try {
      jvmOptions = jPrefs.get(RUNTIME_JVMOPTIONS, ""); //$NON-NLS-1$
    } catch (final Exception e) {
      AbstractActivator.logBug(e);
    }
    return jvmOptions;
  }

  /**
   * Checks if the resource is a .jj / .jjt / .jtb file and is on classpath.
   * 
   * @param aRes - the resource
   * @return true if all is OK, false otherwise
   */
  private boolean isJJFileAndOnClasspath(final IResource aRes) {
    boolean gen = false;
    final String ext = aRes.getFileExtension();
    if ("jj".equals(ext) || "jjt".equals(ext) || "jtb".equals(ext)) { //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
      gen = jJavaProject.isOnClasspath(aRes);
    }
    return gen;
  }

  /**
   * Prints different information and runs java commands to help debug launch problems.
   * 
   * @param aRes - the resource
   */
  public void print_launch_info(final IResource aRes) {
    if (aRes == null || !(aRes instanceof IFile) || !aRes.exists()) {
      return;
    }
    setProjectInfo(aRes);
    // the file, the project and the directory
    final IFile file = (IFile) aRes;

    // JavaCC is launched in the directory where the file is
    final String resPath = file.getLocation().toString();
    final String resDir = resPath.substring(0, resPath.lastIndexOf("/")); //$NON-NLS-1$

    final IConsole console = AbstractActivator.getDefault().getConsole();
    // have seen the case of null, but don't know why null can occur
    // one case is when launching Eclipse with a JJEditor on a grammar with problems
    Assert.isNotNull(console);
    final String clearStr = jPrefs.get(CLEAR_CONSOLE, DEF_CLEAR_CONSOLE);
    final boolean clear = ("true").equals(clearStr); //$NON-NLS-1$
    if (clear) {
      console.clear();
    }
    else {
      console.println();
    }

    // build command line
    final String jvmOptions = getJvmOptions().trim();
    final String javaccJarfile = getJarFile(file, "jj"); //$NON-NLS-1$
    final String jtbJarFile = getJarFile(file, "jtb"); //$NON-NLS-1$
    final String defJavaccJarFile = getDefaultJarFile("jj"); //$NON-NLS-1$
    final String defJtbJarFile = getDefaultJarFile("jtb"); //$NON-NLS-1$

    // save standard out and error streams
    final PrintStream orgOut = System.out;
    final PrintStream orgErr = System.err;
    // redirect out and error streams
    final PrintStream consolePS = console.getPrintStream();
    System.setOut(consolePS);
    System.setErr(consolePS);

    console.print("~~~ ProcessBuilder / System environment ~~~", true); //$NON-NLS-1$
    console.println(console.fmtTS(), false);
    final TreeMap<String, String> env = new TreeMap<String, String>(JarLauncher.getPbEnv());
    final Iterator<String> ite = env.keySet().iterator();
    while (ite.hasNext()) {
      final String key = ite.next();
      console.print(key + " : ", true); //$NON-NLS-1$
      console.println(env.get(key), false);
    }
    console.println();

    console.print("~~~ System properties ~~~", true); //$NON-NLS-1$
    console.println(console.fmtTS(), false);
    final TreeMap<Object, Object> props = new TreeMap<Object, Object>(System.getProperties());
    final Iterator<Object> itp = props.keySet().iterator();
    while (itp.hasNext()) {
      final Object key = itp.next();
      if (key instanceof String) {
        final Object val = props.get(key);
        if (val instanceof String) {
          console.print(key + " : ", true); //$NON-NLS-1$
          console.println((String) val, false);
        }
      }
    }
    console.println();

    console.print("~~~ Project's JavaCC jar file ~~~", true); //$NON-NLS-1$
    console.println(console.fmtTS(), false);
    console.println(javaccJarfile, false);
    console.println();

    console.print("~~~ Plug-in's default JavaCC jar file ~~~", true); //$NON-NLS-1$
    console.println(console.fmtTS(), false);
    console.println(defJavaccJarFile, false);
    console.println();

    console.print("~~~ Project's JTB jar file ~~~", true); //$NON-NLS-1$
    console.println(console.fmtTS(), false);
    console.println(jtbJarFile, false);
    console.println();

    console.print("~~~ Plug-in's default JTB jar file ~~~", true); //$NON-NLS-1$
    console.println(console.fmtTS(), false);
    console.println(defJtbJarFile, false);
    console.println();

    console.print("~~~ Project's JVM options (trimmed) ~~~", true); //$NON-NLS-1$
    console.println(console.fmtTS(), false);
    console.println("<" + jvmOptions + ">", false); //$NON-NLS-1$ //$NON-NLS-2$
    console.println();

    final List<String> cmd = new ArrayList<String>(6);

    console.println("~~~ Java command + version option ---> Java version ~~~", false); //$NON-NLS-1$
    cmd.add(sJavaCmd);
    cmd.add("-version"); //$NON-NLS-1$ 
    displayCommand(console, cmd);
    JarLauncher.pb_launch(cmd, resDir);
    console.displayOutput();
    console.println();

    console.println("~~~ JavaCC command + trimmed jvmoptions if not empty & nogrammarfile ---> JavaCC help ~~~", false); //$NON-NLS-1$
    cmd.clear();
    cmd.add(sJavaCmd);
    if (jvmOptions.length() > 0) {
      cmd.add(jvmOptions);
    }
    cmd.add(CLASSPATH_ARG);
    cmd.add(javaccJarfile);
    cmd.add(JAVACC_ARG);
    displayCommand(console, cmd);
    JarLauncher.pb_launch(cmd, resDir);
    console.displayOutput();
    console.println();

    console.println("~~~ JTB command + trimmed jvmoptions if not empty & nogrammarfile ---> JTB help ~~~", false); //$NON-NLS-1$
    cmd.clear();
    cmd.add(sJavaCmd);
    if (jvmOptions.length() > 0) {
      cmd.add(jvmOptions);
    }
    cmd.add(JAR_ARG);
    cmd.add(jtbJarFile);
    displayCommand(console, cmd);
    JarLauncher.pb_launch(cmd, resDir);
    console.displayOutput();
    console.println();

    // restore standard out and error streams
    System.setOut(orgOut);
    System.setErr(orgErr);
    consolePS.close();

    return;
  }

  /**
   * @param aConsole - the console to print on
   * @param aCmd - the command to print on the console
   */
  private static void displayCommand(final IConsole aConsole, final List<String> aCmd) {
    aConsole.print(">", true); //$NON-NLS-1$
    for (final String c : aCmd) {
      aConsole.print(c + " ", true); //$NON-NLS-1$
    }
    aConsole.println(aConsole.fmtTS(), false);
  }

  /**
   * A custom OutputStream.
   */
  class NullOutputStream extends OutputStream {

    /** {@inheritDoc} */
    @Override
    public void write(@SuppressWarnings("unused") final byte[] arg0,
                      @SuppressWarnings("unused") final int arg1, @SuppressWarnings("unused") final int arg2) {
      // No action
    }

    /** {@inheritDoc} */
    @Override
    public void write(@SuppressWarnings("unused") final byte[] arg0) {
      // No action
    }

    /** {@inheritDoc} */
    @Override
    public void write(@SuppressWarnings("unused") final int arg0) {
      // No action
    }
  }

}
