package sf.eclipse.javacc;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Launcher for JavaCC Uses Runtime.exec()
 * 
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */
public class JarLauncher {
  private static String javaCmd;

  static {
    String os = System.getProperty("os.name"); //$NON-NLS-1$
    if (os != null) {
      if (os.indexOf("win") >= 0) //$NON-NLS-1$
        javaCmd = "javaw"; //$NON-NLS-1$
      else
        javaCmd = "java"; //$NON-NLS-1$
    }
  }

  /**
   * A Thread to get Output from External Process
   */
  public static class StreamGobbler extends Thread {
    InputStream is;

    StreamGobbler(InputStream is) {
      this.is = is;
    }

    public void run() {
      try {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        while ((line = br.readLine()) != null)
          System.out.println(line);
        br.close();
      } catch (Throwable ioe) {
        ioe.printStackTrace();
      }
    }
  }

  /**
   * Launches a command with Runtime.exec()
   * 
   * @param cmd
   */
  public static void launch(String[] cmd, String dir) {
    Runtime rt = Runtime.getRuntime();
    try {
      Process proc = rt.exec(cmd, null, new File(dir));
      StreamGobbler err = new StreamGobbler(proc.getErrorStream());
      StreamGobbler out = new StreamGobbler(proc.getInputStream());
      err.start();
      out.start();
      proc.waitFor();
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  /**
   * Launches JavaCC with Runtime.exec() ie launches java -classpath
   * javaccJarFile javacc args
   * 
   * @param javaccJarFile
   * @param args
   */
  public static void launchJavaCC(String javaccJarFile, String[] args,
      String dir) {
    String[] cmd = new String[args.length + 4];
    cmd[0] = javaCmd;
    cmd[1] = "-classpath"; //$NON-NLS-1$
    cmd[2] = javaccJarFile;
    cmd[3] = "javacc"; //$NON-NLS-1$
    for (int i = 0; i < args.length; i++)
      cmd[i + 4] = args[i];
    launch(cmd, dir);
  }

  /**
   * Launches JJTree with Runtime.exec() ie launches java -classpath
   * javaccJarFile jjtree args
   * 
   * @param javaccJarFile
   * @param args
   */
  public static void launchJJTree(String javaccJarFile, String[] args,
      String dir) {
    String[] cmd = new String[args.length + 4];
    cmd[0] = javaCmd;
    cmd[1] = "-classpath"; //$NON-NLS-1$
    cmd[2] = javaccJarFile;
    cmd[3] = "jjtree"; //$NON-NLS-1$
    for (int i = 0; i < args.length; i++)
      cmd[i + 4] = args[i];
    launch(cmd, dir);
  }

  /**
   * Launches JJDoc with Runtime.exec() ie launches java -classpath
   * javaccJarFile jjdoc args
   * 
   * @param javaccJarFile
   * @param args
   */
  public static void launchJJDoc(String javaccJarFile, String[] args, String dir) {
    String[] cmd = new String[args.length + 4];
    cmd[0] = javaCmd;
    cmd[1] = "-classpath"; //$NON-NLS-1$
    cmd[2] = javaccJarFile;
    cmd[3] = "jjdoc"; //$NON-NLS-1$
    for (int i = 0; i < args.length; i++)
      cmd[i + 4] = args[i];
    launch(cmd, dir);
  }

  public static void launchJTB(String jarfile, String[] args, String dir) {
    String[] cmd = new String[args.length + 3];
    cmd[0] = javaCmd;
    cmd[1] = "-jar"; //$NON-NLS-1$
    cmd[2] = jarfile;
    for (int i = 0; i < args.length; i++)
      cmd[i + 3] = args[i];
    launch(cmd, dir);
  }
  
  /**
   * Unit test
   */
  public static void main(String args[]) {
    String jarFile = "C:/java/javacc-3.0/bin/lib/javacc.jar"; //$NON-NLS-1$
    launchJavaCC(jarFile, 
        new String[] { "C:/java/javacc-3.0/examples/JavaCCGrammar/JavaCC.jj" }, //$NON-NLS-1$
        "."); //$NON-NLS-1$

    launchJJTree(jarFile,
        new String[] { "C:/java/javacc-3.0/examples/JJTreeExamples/eg1.jjt" }, //$NON-NLS-1$
        "."); //$NON-NLS-1$
  }
}
