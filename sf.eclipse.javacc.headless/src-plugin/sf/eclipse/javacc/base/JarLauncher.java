package sf.eclipse.javacc.base;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Launcher for JavaCC. Uses Runtime.exec().
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012
 */
public class JarLauncher {

  // MMa 11/2009 : javadoc and formatting revision
  // MMa 02/2010 : formatting and javadoc revision

  /** The java command on the command line */
  private static String javaCmd;

  static {
    final String os = System.getProperty("os.name"); //$NON-NLS-1$
    if (os != null) {
      if (os.indexOf("win") >= 0) { //$NON-NLS-1$
        javaCmd = "javaw"; //$NON-NLS-1$
      }
      else {
        javaCmd = "java"; //$NON-NLS-1$
      }
    }
  }

  /**
   * A Thread to get Output from External Process.
   */
  public static class StreamGobbler extends Thread {

    /** The input stream */
    InputStream is;

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
        final BufferedReader br = new BufferedReader(isr);
        String line = null;
        while ((line = br.readLine()) != null) {
          System.out.println(line);
        }
        br.close();
      } catch (final Throwable ioe) {
        ioe.printStackTrace();
      }
    }
  }

  /**
   * Launches a command with Runtime.exec().
   * 
   * @param aCmd - the command to launch
   * @param aDir - the directory where to launch the command
   */
  public static void launch(final String[] aCmd, final String aDir) {
    final Runtime rt = Runtime.getRuntime();
    try {
      final Process proc = rt.exec(aCmd, null, new File(aDir));
      final StreamGobbler err = new StreamGobbler(proc.getErrorStream());
      final StreamGobbler out = new StreamGobbler(proc.getInputStream());
      err.start();
      out.start();
      proc.waitFor();
    } catch (final Throwable t) {
      t.printStackTrace();
    }
  }

  /**
   * Launches JavaCC with Runtime.exec(), i.e. launches java -classpath javaccJarFile JavaCC args.
   * 
   * @param aJavaCCJarFile - the jar file to use
   * @param aArgs - the arguments
   * @param aDir - the directory where to launch the command
   */
  public static void launchJavaCC(final String aJavaCCJarFile, final String[] aArgs, final String aDir) {
    final String[] cmd = new String[aArgs.length + 4];
    cmd[0] = javaCmd;
    cmd[1] = "-classpath"; //$NON-NLS-1$
    cmd[2] = aJavaCCJarFile;
    cmd[3] = "javacc"; //$NON-NLS-1$
    for (int i = 0; i < aArgs.length; i++) {
      cmd[i + 4] = aArgs[i];
    }
    launch(cmd, aDir);
  }

  /**
   * Launches JJTree with Runtime.exec(), i.e. launches java -classpath javaccJarFile JJTree args.
   * 
   * @param aJavaCCJarFile - the jar file to use
   * @param aArgs - the arguments
   * @param aDir - the directory where to launch the command
   */
  public static void launchJJTree(final String aJavaCCJarFile, final String[] aArgs, final String aDir) {
    final String[] cmd = new String[aArgs.length + 4];
    cmd[0] = javaCmd;
    cmd[1] = "-classpath"; //$NON-NLS-1$
    cmd[2] = aJavaCCJarFile;
    cmd[3] = "jjtree"; //$NON-NLS-1$
    for (int i = 0; i < aArgs.length; i++) {
      cmd[i + 4] = aArgs[i];
    }
    launch(cmd, aDir);
  }

  /**
   * Launches JJDoc with Runtime.exec(), i.e. launches java -classpath javaccJarFile JJDoc args.
   * 
   * @param aJavaCCJarFile - the jar file to use
   * @param aArgs - the arguments
   * @param aDir - the directory where to launch the command
   */
  public static void launchJJDoc(final String aJavaCCJarFile, final String[] aArgs, final String aDir) {
    final String[] cmd = new String[aArgs.length + 4];
    cmd[0] = javaCmd;
    cmd[1] = "-classpath"; //$NON-NLS-1$
    cmd[2] = aJavaCCJarFile;
    cmd[3] = "jjdoc"; //$NON-NLS-1$
    for (int i = 0; i < aArgs.length; i++) {
      cmd[i + 4] = aArgs[i];
    }
    launch(cmd, aDir);
  }

  /**
   * Launches JTB with Runtime.exec(), i.e. launches java -jar javaccJarFile args.
   * 
   * @param aJarfile - the jar file to use
   * @param aArgs - the arguments
   * @param aDir - the directory where to launch the command
   */
  public static void launchJTB(final String aJarfile, final String[] aArgs, final String aDir) {
    final String[] cmd = new String[aArgs.length + 3];
    cmd[0] = javaCmd;
    cmd[1] = "-jar"; //$NON-NLS-1$
    cmd[2] = aJarfile;
    for (int i = 0; i < aArgs.length; i++) {
      cmd[i + 3] = aArgs[i];
    }
    launch(cmd, aDir);
  }

  /**
   * Unit test.
   * 
   * @param aArgs - the arguments
   */
  public static void main(final String aArgs[]) {
    final String jarFile = "C:/java/javacc-3.0/bin/lib/javacc.jar"; //$NON-NLS-1$
    launchJavaCC(jarFile, new String[] {
      "C:/java/javacc-3.0/examples/JavaCCGrammar/JavaCC.jj" }, //$NON-NLS-1$
                 "."); //$NON-NLS-1$

    launchJJTree(jarFile, new String[] {
      "C:/java/javacc-3.0/examples/JJTreeExamples/eg1.jjt" }, //$NON-NLS-1$
                 "."); //$NON-NLS-1$
  }
}
