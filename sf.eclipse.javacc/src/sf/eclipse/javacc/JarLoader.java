package sf.eclipse.javacc;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * A class loader for loading jar files
 * Not used anymore, 
 * replaced by JarLauncher using Runtime.exec()
 * (Except if JJBuilder.Use_JarLoader=true; in plugin.properties)
 * 
 * Typical use :
 * String libName = "C:/java/javacc3.0/bin/lib/javacc.jar";
 * String[] args = new String[1];
 * args[0] = "C:/java/javacc3.0/examples/JavaCCGrammar/JavaCC.jj";
 * ClassLoader cl = libName.getClass().getClassLoader();
 * URL url = new URL("file", null, libName);
 * JarLoader runtime = new JarLoader(url, cl); 
 * Class run = runtime.loadClass("org.netbeans.javacc.parser.Main");
 * Method m = run.getMethod("mainProgram", new Class[] { args.getClass()});
 * Object obj = c.newInstance();
 * m.invoke(obj, new Object[] { args });
 * 
 * Beware of working directory, which cannot be changed.
 * 
 * @author Remi Koutcherawy 2003 
 * GPL Licence www.gnu.org/licenses/gpl.txt
 */

class JarLoader extends URLClassLoader {
  //Cached instance of Loader
  private static JarLoader loader = null;

  /**
   * Creates a new JarClassLoader for the specified url.
   * @param url the url of the jar file
   */
  public JarLoader(URL url, ClassLoader parent) {
    super(new URL[] { url }, parent);
  }
  
  /**
   * Called from JJBuilder, launch JavaCC with
   * @param String jarfile : the javacc.jar
   * @param String[] args : the command line arguments
   * @param String dir : the directory to launch JavaCC froùmm 
   */
  public static void launchJavaCC(String jarfile, String[] args, String dir) {
    // Lauching via a ClassLoader
    try {
      // Cached instance of loader
      if (loader == null) {
        // Load Jar file
        ClassLoader cl = jarfile.getClass().getClassLoader();
        URL url = new URL("file", null, jarfile);
        loader = new JarLoader(url, cl);
      }
      
      // Change OutputDirectory from default to dir 
      args = checkArgs(args, dir);
      
      // Invoke JavaCC
      Class c = loader.loadClass("javacc");
      Method m = c.getMethod("main", new Class[] { args.getClass()});
      Object obj = c.newInstance();
      m.invoke(obj, new Object[] { args });
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Called from JJBuilder, launch JJTree with
   * @param String jarfile : the javacc.jar
   * @param String[] args : the command line arguments
   * @param String dir : the directory to launch JavaCC froùmm 
   */
  public static void launchJJTree(String jarfile, String[] args, String dir) {
    // Lauching via a ClassLoader
    try {
      // Cached instance of Loader
      if (loader == null) { 
        // Load Jar file
        ClassLoader cl = jarfile.getClass().getClassLoader();
        URL url = new URL("file", null, jarfile);
        loader = new JarLoader(url, cl);
      }
      
      // Change OutputDirectory from default to dir 
      args = checkArgs(args, dir);
      
      // Invoke JJTree
      Class c = loader.loadClass("jjtree");
      Method m = c.getMethod("main", new Class[] { args.getClass()});
      Object obj = c.newInstance();
      m.invoke(obj, new Object[] { args });
    } catch (Exception e) {
      System.out.println("JarLoader jarfile :"+jarfile);
      e.printStackTrace();
    }
  }


  /**
   * Called from JJBuilder, launch JJDoc with
   * @param String jarfile : the javacc.jar
   * @param String[] args : the command line arguments
   * @param String dir : the directory to launch JavaCC froùmm 
   */
  public static void launchJJDoc(String jarfile, String[] args, String dir) {
    // Lauching via a ClassLoader
    try {
      // Cached instance of Loader
      if (loader == null) { 
        // Load Jar file
        ClassLoader cl = jarfile.getClass().getClassLoader();
        URL url = new URL("file", null, jarfile);
        loader = new JarLoader(url, cl);
      }
      
      // Change OutputDirectory from default to dir 
      args = checkArgs(args, dir);
        
      // Invoke JJDoc
      // BEWARE org.javacc.jjdoc.JJDocMain.main()
      // is full of System.exit() which close Eclise !!!
      // You need to remove them and rebuild javacc.jar
      Class c = loader.loadClass("jjdoc");
      Method m = c.getMethod("main", new Class[] { args.getClass()});
      Object obj = c.newInstance();
      m.invoke(obj, new Object[] { args });
    } catch (Throwable e) {
      System.out.println("JarLoader jarfile :"+jarfile);
      e.printStackTrace();
    }   
  }
  
  /**
   * To call JavaCC from the project directory.
   * We cannot change "user.dir"
   *  which is where Eclipse was launched from.
   * So we modify or add -OUTPUT_DIRECTORY
   * @param args the JavaCC arguments
   * @param dir the project directory where JavaCC shall be launched
   * @return new command line args
   */
  protected static String[] checkArgs(String[] args, String dir) {
    boolean found = false;
    for (int i = 0; i < args.length; i++){
      if (args[i].startsWith("-OUTPUT_DIRECTORY")){
        String path = args[i].substring(18);
        File file = new File(path);
        if (!file.isAbsolute())
          args[i] =
            args[i].substring(0, 18)
              + dir
              + File.separatorChar
              + args[i].substring(18);
        found = true;
      }
      else if (args[i].startsWith("-OUTPUT_FILE")){
        String path = args[i].substring(13);
        File file = new File(path);
        if (!file.isAbsolute())
          args[i] =
            args[i].substring(0, 13)
              + dir
              + File.separatorChar
              + args[i].substring(13);
      }
    }
    // By Default JavaCC takes "user.dir" 
    // which is where eclipse has been launched.
    // We fix it to projectdir
    if (!found){
      String[] newargs = new String[args.length+1];
      for (int i = 0; i < args.length; i++){
        newargs[i] = args[i];
      }
      newargs[args.length-1] = "-OUTPUT_DIRECTORY="+dir;
      newargs[args.length] = args[args.length-1];
      args = newargs;
    }
    return args;
  }
  
  /**
   *  Unit test
   */
  public static void main(String[] args) {
    String[] arg = {"-OUTPUT_FILE=C:\\foo"};
    String dir = System.getProperty("user.dir");
    for (int i = 0; i < arg.length; i++)
      System.out.println("arg[i] "+arg[i]);
    arg = checkArgs(arg, dir);
    for (int i = 0; i < arg.length; i++)
      System.out.println("arg[i] "+arg[i]);
      
    String jarFile = "C:/java/javacc-3.1/bin/lib/javacc.jar";
    launchJavaCC(
      jarFile,
      new String[] { "C:/java/javacc-3.1/examples/JavaCCGrammar/JavaCC.jj"},
      "foo"
      );
    launchJJTree(
      jarFile,
      new String[] { "C:/java/javacc-3.1/examples/JJTreeExamples/eg1.jjt"},
      "foo"
      );  
  }
}
