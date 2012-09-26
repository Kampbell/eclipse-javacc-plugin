package sf.eclipse.javacc.head;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import sf.eclipse.javacc.base.DirList;
import sf.eclipse.javacc.base.FileUtils;
import sf.eclipse.javacc.base.IJJConsole;
import sf.eclipse.javacc.base.IJJConstants;
import sf.eclipse.javacc.base.JarLauncher;
import sf.eclipse.javacc.base.OptionSet;

/**
 * Builder for .jj, .jjt and .jtb files for normal usage (ie non headless builds).<br>
 * It is also used to compile files via static methods.<br>
 * Referenced by plugin.xml<br>
 * <extension point="org.eclipse.core.resources.builders">.<br>
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012
 * @author Bill Fenlason 2012
 */
public class JJBuilder extends IncrementalProjectBuilder implements IResourceDeltaVisitor, IResourceVisitor,
                                                        IJJConstants {

  // MMa 11/2009 : javadoc and formatting revision ; changed jar names
  // MMa 02/2010 : formatting and javadoc revision ; fixed issue for JTB problems reporting
  // MMa 03/2010 : change on QN_GENERATED_FILE for bug 2965665 fix
  // MMa 08/2011 : added mark generated files as derived option RFE 3314103
  // MMa 08/2011 : added modification of the JavaCC file generated checksum if suppress warning option set
  // MMa 08/2011 : fixed use of deprecated method in Eclipse 3.6+
  // BF  06/2012 : documented empty block to prevent warning message

  /** The java project (needed to test if the resource is on class path) */
  protected IJavaProject jJavaProject;
  /** The output folder */
  protected IPath        jOutputFolder;

  /**
   * Invoked in response to a call to one of the <code>IProject.build</code>.
   * <p>
   * {@inheritDoc}
   */
  @Override
  protected IProject[] build(final int aKind, @SuppressWarnings({
      "unused", "rawtypes" }) final Map aArgs, final IProgressMonitor aMonitor) throws CoreException {
    // these are Constants on the build
    jJavaProject = JavaCore.create(getProject());
    jOutputFolder = jJavaProject.getOutputLocation().removeFirstSegments(1);
    // clear only once
    clearConsole();

    if (aKind == IncrementalProjectBuilder.FULL_BUILD) {
      fullBuild(aMonitor);
    }
    else if (aKind == IncrementalProjectBuilder.INCREMENTAL_BUILD
             || aKind == IncrementalProjectBuilder.AUTO_BUILD) {
      incrementalBuild(aMonitor);
    }
    else if (aKind == IncrementalProjectBuilder.CLEAN_BUILD) {
      clean(aMonitor);
    }
    // refresh the whole project
    getProject().refreshLocal(IResource.DEPTH_INFINITE, aMonitor);
    return null;
  }

  /**
   * Performs a full build.
   * 
   * @param aMonitor - a progress monitor, or <code>null</code> if progress reporting and cancellation are not
   *          desired
   * @exception CoreException if this build fails
   */
  protected void fullBuild(@SuppressWarnings("unused") final IProgressMonitor aMonitor) throws CoreException {
    getProject().accept(this);
  }

  /**
   * Performs an incremental build or a full build if no delta is available.
   * 
   * @param aMonitor - a progress monitor, or <code>null</code> if progress reporting and cancellation are not
   *          desired
   * @exception CoreException if this build fails
   */
  public void incrementalBuild(final IProgressMonitor aMonitor) throws CoreException {
    final IResourceDelta delta = getDelta(getProject());
    if (delta != null) {
      delta.accept(this);
    }
    else {
      fullBuild(aMonitor);
    }
  }

  /**
   * Cleans all generated files.
   * 
   * @param aMonitor - a progress monitor, or <code>null</code> if progress reporting and cancellation are not
   *          desired
   * @exception CoreException if this build fails
   */
  @Override
  protected void clean(final IProgressMonitor aMonitor) throws CoreException {
    super.clean(aMonitor);
    final IResource[] members = getProject().members();
    clean(members, aMonitor);
  }

  /**
   * Deletes recursively generated AND derived files. A modified generated file, marked as not derived, shall
   * not be deleted.
   * 
   * @param aMembers - the IResource[] to delete
   * @param aMonitor - a progress monitor, or <code>null</code> if progress reporting and cancellation are not
   *          desired
   * @exception CoreException if this build fails
   */
  private void clean(final IResource[] aMembers, final IProgressMonitor aMonitor) throws CoreException {
    for (final IResource res : aMembers) {
      if (res.getType() == IResource.FOLDER) {
        clean(((IFolder) res).members(), aMonitor);
      }
      else if (res.isDerived() && res.getPersistentProperty(QN_GENERATED_FILE) != null) {
        res.delete(IResource.KEEP_HISTORY, aMonitor);
      }
      else {
        res.deleteMarkers(IMarker.PROBLEM, false, IResource.DEPTH_ZERO);
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean visit(final IResourceDelta aDelta) throws CoreException {
    return visit(aDelta.getResource());
  }

  /** {@inheritDoc} */
  @Override
  public boolean visit(final IResource aRes) throws CoreException {
    if (aRes == null) {
      return false;
    }
    final String ext = aRes.getFileExtension();
    final boolean okToCompile = jJavaProject.isOnClasspath(aRes) && ext != null
                                && (ext.equals("jj") || ext.equals("jjt") || ext.equals("jtb")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    if (okToCompile) {
      CompileResource(aRes);
    }

    // this prevents traversing output directories
    final boolean isOut = aRes.getProjectRelativePath().equals(jOutputFolder)
                          & jOutputFolder.toString().length() != 0;
    return !isOut;
  }

  /**
   * Compiles a .jj, .jjt or .jtb file given its IResource.
   * 
   * @param aRes - the IResource to compile
   * @exception CoreException if this compile fails
   */
  public static void CompileResource(final IResource aRes) throws CoreException {
    if (!(aRes instanceof IFile) || !aRes.exists()) {
      return;
    }

    // the file, the project and the directory
    final IFile file = (IFile) aRes;
    final IProject project = file.getProject();
    final String projectDir = project.getLocation().toOSString();

    // delete problem markers
    try {
      file.deleteMarkers(IMarker.PROBLEM, false, IResource.DEPTH_ZERO);
    } catch (final CoreException e) {
      e.printStackTrace();
    }

    // JavaCC is launched in the directory where the file is
    final String resPath = file.getLocation().toString();
    final String resName = resPath.substring(resPath.lastIndexOf("/") + 1); //$NON-NLS-1$
    final String resDir = resPath.substring(0, resPath.lastIndexOf("/")); //$NON-NLS-1$
    final String resExt = file.getFullPath().getFileExtension();
    final String resRelPath = resPath.substring(projectDir.length());

    final IJJConsole console = Activator.getConsole();

    // retrieve command line
    final String[] args = getArgs(file, resName);
    final String jarfile = getJarFile(file);

    // save standard out and error streams
    final PrintStream orgOut = System.out;
    final PrintStream orgErr = System.err;
    // redirect out and error streams
    final PrintStream outConsole = console.getPrintStream();
    System.setOut(outConsole);
    System.setErr(outConsole);

    // display the command line on the console
    if (resExt.equals("jj")) { //$NON-NLS-1$
      console.print(">java -classpath " + jarfile + " javacc "); //$NON-NLS-1$ //$NON-NLS-2$
    }
    else if (resExt.equals("jjt")) { //$NON-NLS-1$
      console.print(">java -classpath " + jarfile + " jjtree "); //$NON-NLS-1$ //$NON-NLS-2$
    }
    else if (resExt.equals("jtb")) { //$NON-NLS-1$
      console.print(">java -jar " + jarfile + " "); //$NON-NLS-1$ //$NON-NLS-2$
    }
    for (int i = 0; i < args.length; i++) {
      console.print(args[i] + " "); //$NON-NLS-1$
    }
    System.out.println();

    // call JavaCC, JJTree or JTB
    DirList.snapshot(projectDir);
    boolean isJtb = false;
    if (resExt.equals("jjt")) { //$NON-NLS-1$
      JarLauncher.launchJJTree(jarfile, args, resDir);
    }
    else if (resExt.equals("jj")) { //$NON-NLS-1$
      JarLauncher.launchJavaCC(jarfile, args, resDir);
    }
    else if (resExt.equals("jtb")) { //$NON-NLS-1$
      JarLauncher.launchJTB(jarfile, args, resDir);
      isJtb = true;
    }
    System.out.println();

    // restore standard out and error streams
    System.setOut(orgOut);
    System.setErr(orgErr);

    // notify the console with the file the console should report errors to
    console.endReport(file, isJtb);

    // compile the generated .jj file if a .jjt or .jtb file was processed
    final String[] generatedFiles = DirList.getDiff(projectDir);
    if (generatedFiles != null) {
      for (int i = 0; i < generatedFiles.length; i++) {
        final String genFileName = generatedFiles[i].substring(projectDir.length() + 1);
        IResource genFileRes = project.findMember(genFileName);
        if (genFileRes == null) {
          project.refreshLocal(IResource.DEPTH_INFINITE, null);
          genFileRes = project.findMember(genFileName);
        }

        // compile .jj only if .jjt or .jtb was compiled and .jj was generated
        if (genFileName.endsWith(".jj") && (resExt.equals("jjt") || resExt.equals("jtb"))) { //$NON-NLS-1$ //$NON-NLS-2$  //$NON-NLS-3$
          // compile .jj if project has not JavaCC Nature, i.e. no automatic build
          // well seems Eclipse has a small bug here, it doesn't recompile ...
          //          if (!project.getDescription().hasNature(JJ_NATURE_ID))
          CompileResource(genFileRes);
        }

        // mark them with a 'G' and correct .java files
        markAndAlter(project, resRelPath, genFileRes);
      }
    }
  }

  /** The platform line separator */
  private static final String  LS               = System.getProperty("line.separator");                 //$NON-NLS-1$
  /**
   * Regular expression to capture the class declaration, including potential @SuppressWarnings annotations.<br>
   * (?:@SuppressWarnings\\(\\\"(?:all|serial)\\\"\\)..?)? : non capturing group, once or not at all.<br>
   * ((?:public )?(?:final )?(?:class|interface|enum)) : capturing group $1.<br>
   * This group $1 will by prefixed by a new line containing <code>@SuppressWarnings(\"all\")</code> and will
   * replace the whole string (group $0).
   */
  private final static String  classDeclRegExpr = "^(?:@SuppressWarnings\\(\\\"(?:all|serial)\\\"\\)..?)?" //$NON-NLS-1$
                                                  + "((?:public )?(?:final )?(?:class|interface|enum))"; //NON-NLS-1$ //$NON-NLS-1$
  /** Corresponding pattern */
  private final static Pattern classDeclPatt    = Pattern.compile(classDeclRegExpr, Pattern.MULTILINE
                                                                                    | Pattern.DOTALL);
  /** Replacement string */
  private final static String  replStr          = "@SuppressWarnings(\"all\")" + LS + "$1";             //$NON-NLS-1$ //$NON-NLS-2$

  /**
   * Marks the generated file as derived and suppresses the @SuppressWarnings annotation according to
   * corresponding preference.
   * 
   * @param aProject - the IProject the resource belongs to
   * @param aRelPath - the path to the grammar file (relative to the project) this resource is generated from
   * @param aRes - the IResource to mark and to correct
   * @throws CoreException - see {@link IResource#setDerived(boolean, IProgressMonitor)}
   */
  public static void markAndAlter(final IProject aProject, final String aRelPath, final IResource aRes)
                                                                                                       throws CoreException {
    final IEclipsePreferences prefs = new ProjectScope(aProject).getNode(IJJConstants.ID);
    // mark
    aRes.setDerived("true".equals(prefs.get(MARK_GEN_FILES_AS_DERIVED, "true")), null); //$NON-NLS-1$ //$NON-NLS-2$
    aRes.setPersistentProperty(QN_GENERATED_FILE, aRelPath);
    // alter if set in preferences
    final IJavaElement element = (IJavaElement) aRes.getAdapter(IJavaElement.class);
    if ("true".equals(prefs.get(SUPPRESS_WARNINGS, "false")) //$NON-NLS-1$ //$NON-NLS-2$
        && element instanceof ICompilationUnit) {
      // direct access to the file !
      final String filename = ((IFile) aRes).getLocation().toOSString();
      final String source = FileUtils.getFileContents(filename);
      final Matcher matcher = classDeclPatt.matcher(source);
      if (matcher.find()) {
        // add the annotation
        String newsource = matcher.replaceFirst(replStr);
        final int ix = newsource.indexOf(MD5_LINE_PART_1);
        if (ix >= 0) {
          // strip the old checksum line
          newsource = newsource.substring(0, ix);
          // compute the new checksum and add a new checksum line
          newsource = newsource + MD5_LINE_PART_1 + computeCheksum(newsource) + MD5_LINE_PART_2 + LS;
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
  private static String computeCheksum(final String aSource) {
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance(MD5_ALGO);
    } catch (final NoSuchAlgorithmException e) {
      Activator.logErr("No MD5 implementation (should not happen as JavaCC should use it also)"); //$NON-NLS-1$
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
  private static final String toHexString(final byte[] aBytes) {
    final StringBuffer sb = new StringBuffer(32);
    for (int i = 0; i < aBytes.length; i++) {
      final byte b = aBytes[i];
      sb.append(HEX_DIGITS[(b & 0xF0) >> 4]).append(HEX_DIGITS[b & 0x0F]);
    }
    return sb.toString();
  }

  /** Array of hexadecimal characters */
  private final static char[] HEX_DIGITS      = new char[] {
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

  /**
   * The algorithm used by JavaCC to compute the checksum.<br>
   * Duplicated from JavaCC 5.0 org.javacc.parser.OutputFile constant
   */
  private static final String MD5_ALGO        = "MD5";                                 //$NON-NLS-1$
  /**
   * Beginning of the checksum line generated by JavaCC.<br>
   * Duplicated from JavaCC 5.0 org.javacc.parser.OutputFile.MD5_LINE_PART_1
   */
  private static final String MD5_LINE_PART_1 = "/* JavaCC - OriginalChecksum=";       //$NON-NLS-1$
  /**
   * End of the checksum line generated by JavaCC.<br>
   * Duplicated from JavaCC 5.0 org.javacc.parser.OutputFile.MD5_LINE_PART_2
   */
  private static final String MD5_LINE_PART_2 = " (do not edit this line) */";         //$NON-NLS-1$

  /**
   * A custom OutputStream.
   */
  static class NullOutputStream extends OutputStream {

    /** {@inheritDoc} */
    @SuppressWarnings("unused")
    @Override
    public void write(final byte[] arg0, final int arg1, final int arg2) throws IOException {
      // No action
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unused")
    @Override
    public void write(final byte[] arg0) throws IOException {
      // No action
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unused")
    @Override
    public void write(final int arg0) throws IOException {
      // No action
    }
  }

  /**
   * Calls JJDoc for a .jj, .jjt or .jtb file given its IResource.
   * 
   * @param aRes - the IResource to JJDoc
   */
  public static void GenDocForJJResource(final IResource aRes) {
    if (!(aRes instanceof IFile)) {
      return;
    }

    final IFile file = (IFile) aRes;
    final IProject project = file.getProject();
    final String resDir = project.getLocation().toOSString();
    final String resName = file.getLocation().toString();

    final IJJConsole console = Activator.getConsole();

    // retrieve command line
    final String[] args = getJJDocArgs(file, resName);
    final String jarfile = getJarFile(file);

    // redirect standard and error streams
    final PrintStream orgOut = System.out;
    final PrintStream orgErr = System.err;
    final PrintStream outConsole = console.getPrintStream();
    System.setOut(outConsole);
    System.setErr(outConsole);

    // recall command line on console
    console.print(">java -classpath " + jarfile + " jjdoc "); //$NON-NLS-1$ //$NON-NLS-2$
    for (int i = 0; i < args.length; i++) {
      console.print(args[i] + " "); //$NON-NLS-1$
    }
    System.out.println();

    // call JJDoc
    JarLauncher.launchJJDoc(jarfile, args, resDir);
    System.out.println();

    // restore standard and error streams
    System.setOut(orgOut);
    System.setErr(orgErr);

    // notify Console with the file the Console should report errors to
    console.endReport(file, false);
  }

  /**
   * Builds the array of options to call the JavaCC, JJTree or JTB Compiler with.
   * 
   * @param aFile - the resource to get the options for
   * @param aName - the file name to compile
   * @return String[] the options to call the JavaCC / JJTree / JTB compiler with
   */
  @SuppressWarnings("null")
  protected static String[] getArgs(final IFile aFile, final String aName) {
    String[] args = null;
    final String extension = aFile.getFullPath().getFileExtension();
    try {
      String options = null;
      final IEclipsePreferences prefs = new ProjectScope(aFile.getProject()).getNode(IJJConstants.ID);

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

      // add target file to compile
      options = options + " \"" + aName + "\""; //$NON-NLS-1$ //$NON-NLS-2$

      // get tokens
      args = OptionSet.tokenize(options);
    } catch (final Exception e) {
      e.printStackTrace();
    }

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
   * @param aFile - the resource to get the options for
   * @param aName - the file name to compile
   * @return String[] the array of options to call the JJDoc Compiler with
   */
  protected static String[] getJJDocArgs(final IResource aFile, final String aName) {
    String[] args = null;
    try {
      final IEclipsePreferences prefs = new ProjectScope(aFile.getProject()).getNode(IJJConstants.ID);
      String options = null;
      // read project properties
      options = prefs.get(JJDOC_OPTIONS, ""); //$NON-NLS-1$
      // else take default
      if (options == null) {
        options = ""; //$NON-NLS-1$
      }
      // add target i.e. file to compile
      options = options + " \"" + aName + " \""; //$NON-NLS-1$ //$NON-NLS-2$
      // get tokens
      args = OptionSet.tokenize(options);
    } catch (final Exception e) {
      e.printStackTrace();
    }
    return args;
  }

  /**
   * Retrieves the path to the jar file (from the preferences or from the plug-in).
   * 
   * @param aFile - the IResource to get the jar file for
   * @return String the path to the jar file
   */
  protected static String getJarFile(final IResource aFile) {
    String jarfile = null;
    final String extension = aFile.getFullPath().getFileExtension();
    try {
      final IEclipsePreferences prefs = new ProjectScope(aFile.getProject()).getNode(IJJConstants.ID);
      // use the path in preferences
      if (extension.equals("jj") || extension.equals("jjt")) {//$NON-NLS-1$ //$NON-NLS-2$ 
        jarfile = prefs.get(RUNTIME_JJJAR, ""); //$NON-NLS-1$
      }
      else if (extension.equals("jtb")) { //$NON-NLS-1$
        jarfile = prefs.get(RUNTIME_JTBJAR, ""); //$NON-NLS-1$
      }
      // else we use the jar in the plug-in
      if (jarfile == null || jarfile.equals("") || jarfile.startsWith("-")) {//$NON-NLS-1$ //$NON-NLS-2$ 
        final URL installURL = Activator.getDefault().getBundle().getEntry("/"); //$NON-NLS-1$
        // Eclipse 3.2 way. Only available in Eclipse 3.2
        final URL resolvedURL = org.eclipse.core.runtime.FileLocator.resolve(installURL);
        final String home = org.eclipse.core.runtime.FileLocator.toFileURL(resolvedURL).getFile();

        // same for both
        if (extension.equals("jj") || extension.equals("jjt")) { //$NON-NLS-1$ //$NON-NLS-2$
          jarfile = home + JAVACC_JAR_NAME;
        }
        else if (extension.equals("jtb")) { //$NON-NLS-1$
          jarfile = home + JTB_JAR_NAME;
        }
      }
      try {
        jarfile = VariablesPlugin.getDefault().getStringVariableManager()
                                 .performStringSubstitution(jarfile, true);
        // on Windows this returns "/C:/workspace/sf.eclipse.javacc/jtb132.jar"
        // as this will fails, we remove the first "/" if there is ":" at index 2
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
    final String str = new ProjectScope(getProject()).getNode(IJJConstants.ID).get(CLEAR_CONSOLE, "false"); //$NON-NLS-1$
    final boolean clr = ("true").equals(str); //$NON-NLS-1$
    if (clr) {
      final IJJConsole console = Activator.getConsole();
      if (console != null) {
        console.clear();
      }
    }
  }
}
