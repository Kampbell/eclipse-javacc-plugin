package sf.eclipse.javacc;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Launcher for JavaCC. Uses Runtime.exec().
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class JarLauncher {

  // MMa 11/2009 : javadoc and formatting revision ; added -fullversion
  // MMa 02/2010 : formatting and javadoc revision

  /** the java command on the command line */
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

    /** the input stream */
    InputStream is;

    /**
     * Standard constructor.
     * 
     * @param aIs the input stream
     */
    StreamGobbler(final InputStream aIs) {
      is = aIs;
    }

    /**
     * @see java.lang.Thread#run()
     */
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
   * @param cmd the command to launch
   * @param dir the directory where to launch the command
   */
  public static void launch(final String[] cmd, final String dir) {
    final Runtime rt = Runtime.getRuntime();
    try {
      final Process proc = rt.exec(cmd, null, new File(dir));
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
   * @param javaccJarFile the jar file to use
   * @param args the arguments
   * @param dir the directory where to launch the command
   */
  public static void launchJavaCC(final String javaccJarFile, final String[] args, final String dir) {
    final String[] cmd = new String[args.length + 4];
    cmd[0] = javaCmd;
    cmd[1] = "-classpath"; //$NON-NLS-1$
    cmd[2] = javaccJarFile;
    cmd[3] = "javacc"; //$NON-NLS-1$
    for (int i = 0; i < args.length; i++) {
      cmd[i + 4] = args[i];
    }
    launch(cmd, dir);
  }

  /**
   * Launches JJTree with Runtime.exec(), i.e. launches java -classpath javaccJarFile JJTree args.
   * 
   * @param javaccJarFile the jar file to use
   * @param args the arguments
   * @param dir the directory where to launch the command
   */
  public static void launchJJTree(final String javaccJarFile, final String[] args, final String dir) {
    final String[] cmd = new String[args.length + 4];
    cmd[0] = javaCmd;
    cmd[1] = "-classpath"; //$NON-NLS-1$
    cmd[2] = javaccJarFile;
    cmd[3] = "jjtree"; //$NON-NLS-1$
    for (int i = 0; i < args.length; i++) {
      cmd[i + 4] = args[i];
    }
    launch(cmd, dir);
  }

  /**
   * Launches JJDoc with Runtime.exec(), i.e. launches java -classpath javaccJarFile JJDoc args.
   * 
   * @param javaccJarFile the jar file to use
   * @param args the arguments
   * @param dir the directory where to launch the command
   */
  public static void launchJJDoc(final String javaccJarFile, final String[] args, final String dir) {
    final String[] cmd = new String[args.length + 4];
    cmd[0] = javaCmd;
    cmd[1] = "-classpath"; //$NON-NLS-1$
    cmd[2] = javaccJarFile;
    cmd[3] = "jjdoc"; //$NON-NLS-1$
    for (int i = 0; i < args.length; i++) {
      cmd[i + 4] = args[i];
    }
    launch(cmd, dir);
  }

  /**
   * Launches JTB with Runtime.exec(), i.e. launches java -jar javaccJarFile args.
   * 
   * @param jarfile the jar file to use
   * @param args the arguments
   * @param dir the directory where to launch the command
   */
  public static void launchJTB(final String jarfile, final String[] args, final String dir) {
    final String[] cmd = new String[args.length + 3];
    cmd[0] = javaCmd;
    cmd[1] = "-jar"; //$NON-NLS-1$
    cmd[2] = jarfile;
    for (int i = 0; i < args.length; i++) {
      cmd[i + 3] = args[i];
    }
    launch(cmd, dir);
  }

  /**
   * Unit test.
   * 
   * @param args the arguments
   */
  public static void main(final String args[]) {
    final String jarFile = "C:/java/javacc-3.0/bin/lib/javacc.jar"; //$NON-NLS-1$
    launchJavaCC(jarFile, new String[] {
      "C:/java/javacc-3.0/examples/JavaCCGrammar/JavaCC.jj" }, //$NON-NLS-1$
                 "."); //$NON-NLS-1$

    launchJJTree(jarFile, new String[] {
      "C:/java/javacc-3.0/examples/JJTreeExamples/eg1.jjt" }, //$NON-NLS-1$
                 "."); //$NON-NLS-1$
  }
}
