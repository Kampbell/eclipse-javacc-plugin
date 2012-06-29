package sf.eclipse.javacc.base;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
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
 * @author Marc Mazas 2009-2010-2011-2012
 */
public class FileUtils {

  // MMa 02/2010 : formatting and javadoc revision
  // MMa 08/2011 : javadoc revision
  // MMa 08/2011 : added getFileContentsSB() (but unused)

  /**
   * Reads a file contents.
   * 
   * @param aFileName - a file name
   * @return the file contents
   */
  public static StringBuilder getFileContentsSB(final String aFileName) {
    final File f = new File(aFileName);
    final int len = (int) f.length();
    Reader r = null;
    try {
      r = new BufferedReader(new FileReader(f));
      final StringBuilder sb = new StringBuilder(len);
      final char[] buf = new char[len];
      r.read(buf);
      sb.append(buf);
      return sb;
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
   * Reads a file contents.
   * 
   * @param aFileName - a file name
   * @return the file contents
   */
  public static String getFileContents(final String aFileName) {
    Reader r = null;
    try {
      r = new BufferedReader(new InputStreamReader((new FileInputStream(aFileName))));
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
   * Saves a string to a file.
   * 
   * @param aFileName - a file name
   * @param aStr - a string
   */
  public static void saveFileContents(final String aFileName, final String aStr) {
    Writer w = null;
    try {
      w = new FileWriter(aFileName);
      w.write(aStr);
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

  /**
   * Appends a string to a file.
   * 
   * @param aFileName - a file name
   * @param aStr - a string
   */
  public static void appendFileContents(final String aFileName, final String aStr) {
    Writer w = null;
    try {
      w = new FileWriter(aFileName);
      w.append(aStr);
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
