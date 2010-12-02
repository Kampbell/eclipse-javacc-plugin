package sf.eclipse.javacc.base;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Builder for .jj and .jjt files. It is also used to compile files via static methods.<br>
 * Referenced by plugin.xml<br>
 * <extension point="org.eclipse.core.resources.builders">
 * 
 * @author Tim Hanson 2007
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010 http://www.cecill.info/index.en.html
 */
public class FileUtils {

  // MMa 02/2010 : formatting and javadoc revision

  /**
   * Reads a file contents.
   * 
   * @param aName a file name
   * @return the file contents
   */
  public static String getFileContents(final String aName) {
    Reader r = null;
    try {
      r = new BufferedReader(new InputStreamReader((new FileInputStream(aName))));
      final StringWriter w = new StringWriter();
      final char[] buf = new char[4096];
      int i;
      while ((i = r.read(buf)) > 0) {
        w.write(buf, 0, i);
      }
      return w.toString();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (r != null) {
        try {
          r.close();
        } catch (final IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Saves an current file's content to a new file.
   * 
   * @param aCurrFileName a current file's name
   * @param aNewFileName a new file's name
   */
  public static void saveFileContents(final String aCurrFileName, final String aNewFileName) {
    Writer w = null;
    try {
      w = new FileWriter(aCurrFileName);
      w.write(aNewFileName);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (w != null) {
        try {
          w.close();
        } catch (final IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
