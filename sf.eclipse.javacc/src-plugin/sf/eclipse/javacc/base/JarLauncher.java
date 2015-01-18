package sf.eclipse.javacc.base;

import static sf.eclipse.javacc.base.IConstants.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Launcher for JavaCC.<br>
 * Used only by {@link Compiler}.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 */
public class JarLauncher {

  // MMa 11/2009 : javadoc and formatting revision
  // MMa 02/2010 : formatting and javadoc revision
  // MMa 10/2012 : added JVM options option ; used ProcessBuilder instead of Runtime
  // MMa 11/2014 : removed some public modifiers
  // MMa 12/2014 : added test on interrupted waitFor
  // MMa 01/2015 : added methods for launching Java ; marked Runtime.exec() ones deprecated

  /** The java command on the command line */
  public static String        sJavaCmd;

  static {
    final String os = System.getProperty("os.name"); //$NON-NLS-1$
    if (os != null) {
      if (os.indexOf("win") >= 0) { //$NON-NLS-1$
        sJavaCmd = "javaw"; //$NON-NLS-1$
      }
      else {
        sJavaCmd = "java"; //$NON-NLS-1$
      }
    }
  }

  /** The regex to split the JVM options string in a array */
  public static final Pattern patt = Pattern.compile("\\s"); //$NON-NLS-1$

  /**
   * A Thread to get Output from External Process.
   */
  static class StreamGobbler extends Thread {

    /** The input stream */
    protected InputStream is;

    /**
     * Standard constructor.
     * 
     * @param aIs - the input stream
     */
    StreamGobbler(final InputStream aIs) {
      is = aIs;
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
      try {
        final InputStreamReader isr = new InputStreamReader(is);
        // JavaCC / JJTree / JTB use PrintStream (System.out) to print,
        //  in a JVM (like IBM 160 JVM) that may use the native code page(850 on Windows)
        //  (Sun 160 JVM seems to use Cp1252 to print)
        // Here the InputStreamReader use a default charset (usually Cp1252 on Eclipse)
        //  this may lead to characters conversion
        //  this can be seen with the IBM 160 JVM error messages (for wrong JVM options) on a French platform
        final BufferedReader br = new BufferedReader(isr);
        String line = null;
        while ((line = br.readLine()) != null) {
          System.out.println(line);
        }
        br.close();
      } catch (final Throwable t) {
        AbstractActivator.logBug(t);
      }
    }
  }

  /**
   * Launches a command with ProcessBuilder.start().
   * 
   * @param aCmd - the command to launch
   * @param aDir - the directory where to launch the command
   */
  static void pb_launch(final List<String> aCmd, final String aDir) {
    int rc = 0;
    try {
      final ProcessBuilder pb = new ProcessBuilder(aCmd);
      pb.redirectErrorStream(true);
      pb.directory(new File(aDir));
      final Process proc = pb.start();
      final StreamGobbler out = new StreamGobbler(proc.getInputStream());
      out.start();
      rc = proc.waitFor();
    } catch (final Throwable t) {
      if (rc == 0) {
        AbstractActivator.logInfo("proc.waitFor() interrupted and returned 0"); //$NON-NLS-1$
      }
      else {
        AbstractActivator.logErr("proc.waitFor() interrupted and returned " + rc); //$NON-NLS-1$
      }
    }
  }

  /**
   * @return the {@link ProcessBuilder} environment
   */
  static Map<String, String> getPbEnv() {
    final ProcessBuilder pb = new ProcessBuilder();
    return pb.environment();
  }

  /**
   * Launches a command with Runtime.exec().
   * 
   * @param aCmd - the command to launch
   * @param aDir - the directory where to launch the command
   */
  @Deprecated
  static void rt_launch(final String[] aCmd, final String aDir) {
    final Runtime rt = Runtime.getRuntime();
    try {
      final Process proc = rt.exec(aCmd, null, new File(aDir));
      final StreamGobbler err = new StreamGobbler(proc.getErrorStream());
      final StreamGobbler out = new StreamGobbler(proc.getInputStream());
      err.start();
      out.start();
      proc.waitFor();
    } catch (final Throwable t) {
      AbstractActivator.logBug(t);
    }
  }

  /**
   * Launches java for Runtime.exec().
   * 
   * @param aJvmOptions - the optional JVM options
   * @param aDir - the directory where to launch the command
   */
  @Deprecated
  public static void launchJava(final String aJvmOptions, final String aDir) {
    final String[] opt = patt.split(aJvmOptions);
    final String[] cmd = new String[opt.length + 1];
    int k = 0;
    cmd[k++] = sJavaCmd;
    for (int i = 0; i < opt.length; i++) {
      cmd[k++] = opt[i];
    }
    rt_launch(cmd, aDir);
  }

  /**
   * Launches JavaCC for Runtime.exec().
   * 
   * @param aJvmOptions - the optional JVM options
   * @param aJavaCCJarFile - the jar file to use
   * @param aArgs - the arguments
   * @param aDir - the directory where to launch the command
   */
  @Deprecated
  public static void launchJavaCC(final String aJvmOptions, final String aJavaCCJarFile,
                                  final String[] aArgs, final String aDir) {
    final String[] opt = patt.split(aJvmOptions);
    final String[] cmd = new String[aArgs.length + opt.length + 4];
    int k = 0;
    cmd[k++] = sJavaCmd;
    for (int i = 0; i < opt.length; i++) {
      cmd[k++] = opt[i];
    }
    cmd[k++] = CLASSPATH_ARG;
    cmd[k++] = aJavaCCJarFile;
    cmd[k++] = JAVACC_ARG;
    for (int i = 0; i < aArgs.length; i++) {
      cmd[k++] = aArgs[i];
    }
    rt_launch(cmd, aDir);
  }

  /**
   * Launches JJTree for Runtime.exec().
   * 
   * @param aJvmOptions - the optional JVM options
   * @param aJavaCCJarFile - the jar file to use
   * @param aArgs - the arguments
   * @param aDir - the directory where to launch the command
   */
  @Deprecated
  public static void launchJJTree(final String aJvmOptions, final String aJavaCCJarFile,
                                  final String[] aArgs, final String aDir) {
    final String[] opt = patt.split(aJvmOptions);
    final String[] cmd = new String[aArgs.length + opt.length + 4];
    int k = 0;
    cmd[k++] = sJavaCmd;
    for (int i = 0; i < opt.length; i++) {
      cmd[k++] = opt[i];
    }
    cmd[k++] = CLASSPATH_ARG;
    cmd[k++] = aJavaCCJarFile;
    cmd[k++] = JJTREE_ARG;
    for (int i = 0; i < aArgs.length; i++) {
      cmd[k++] = aArgs[i];
    }
    rt_launch(cmd, aDir);
  }

  /**
   * Launches JJDoc for Runtime.exec().
   * 
   * @param aJvmOptions - the optional JVM options
   * @param aJavaCCJarFile - the jar file to use
   * @param aArgs - the arguments
   * @param aDir - the directory where to launch the command
   */
  @Deprecated
  public static void launchJJDoc(final String aJvmOptions, final String aJavaCCJarFile, final String[] aArgs,
                                 final String aDir) {
    final String[] opt = patt.split(aJvmOptions);
    final String[] cmd = new String[aArgs.length + opt.length + 4];
    int k = 0;
    cmd[k++] = sJavaCmd;
    for (int i = 0; i < opt.length; i++) {
      cmd[k++] = opt[i];
    }
    cmd[k++] = CLASSPATH_ARG;
    cmd[k++] = aJavaCCJarFile;
    cmd[k++] = JJDOC_ARG;
    for (int i = 0; i < aArgs.length; i++) {
      cmd[k++] = aArgs[i];
    }
    rt_launch(cmd, aDir);
  }

  /**
   * Launches JTB with Runtime.exec(), i.e. launches java [jvm_options] -jar javaccJarFile arguments.
   * 
   * @param aJvmOptions - the optional JVM options
   * @param aJarfile - the jar file to use
   * @param aArgs - the arguments
   * @param aDir - the directory where to launch the command
   */
  @Deprecated
  public static void launchJTB(final String aJvmOptions, final String aJarfile, final String[] aArgs,
                               final String aDir) {
    final String[] opt = patt.split(aJvmOptions);
    final String[] cmd = new String[aArgs.length + opt.length + 3];
    int k = 0;
    cmd[k++] = sJavaCmd;
    for (int i = 0; i < opt.length; i++) {
      cmd[k++] = opt[i];
    }
    cmd[k++] = JAR_ARG;
    cmd[k++] = aJarfile;
    for (int i = 0; i < aArgs.length; i++) {
      cmd[k++] = aArgs[i];
    }
    rt_launch(cmd, aDir);
  }

  //  /**
  //   * Unit test.
  //   * 
  //   * @param aArgs - the arguments
  //   */
  //  public static void main(final String aArgs[]) {
  //    final String jarFile = "C:/java/javacc-3.0/bin/lib/javacc.jar"; //$NON-NLS-1$
  //    launchJavaCC(jarFile, new String[] {
  //      "C:/java/javacc-3.0/examples/JavaCCGrammar/JavaCC.jj" }, //$NON-NLS-1$
  //                 "."); //$NON-NLS-1$
  //
  //    launchJJTree(jarFile, new String[] {
  //      "C:/java/javacc-3.0/examples/JJTreeExamples/eg1.jjt" }, //$NON-NLS-1$
  //                 "."); //$NON-NLS-1$
  //  }

}
