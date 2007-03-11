package sf.eclipse.javacc;

import java.io.*;

/**
 * Builder for .jj and .jjt files Referenced by plugin.xml <extension
 * point="org.eclipse.core.resources.builders"> It is also used to compile files
 * via static methods.
 * 
 * @author Tim Hanson 2007 CeCILL Licence
 *         http://www.cecill.info/index.en.html
 */
public class FileUtils {

  public static String getFileContents(String filename) {
    Reader r = null;
    try {
      r = new BufferedReader(new InputStreamReader((new FileInputStream(filename))));
      StringWriter w = new StringWriter();
      char[] buf = new char[4096];
      int i;
      while ((i = r.read(buf)) > 0)
        w.write(buf, 0, i);
      return w.toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (r != null)
        try {
          r.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
    }
  }

  public static void saveFileContents(String filename, String newsource) {
    Writer w = null;
    try {
      w = new FileWriter(filename);
      w.write(newsource);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (w != null)
        try {
          w.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
    }
  }
}
