package sf.eclipse.javacc.base;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Find last added or modified files after a compilation of a .jjt or .jj file.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012
 * @author Bill Fenlason 2012
 */
public class DirList {

  // MMa 02/2010 : formatting and javadoc revision
  // BF  06/2012 : added required hashCode method to avoid warning message

  /** The last collection of files */
  static Collection<DatedFile> oldCol;

  /**
   * Adds all files found under a given root directory to a given collection.
   * 
   * @param aRoot - the root directory
   * @param aCol - the Collection to fill
   */
  protected static void listFiles(final File aRoot, final Collection<DatedFile> aCol) {
    final File[] f = aRoot.listFiles();
    for (int i = 0; i < f.length; i++) {
      if (f[i].isDirectory()) {
        listFiles(f[i], aCol);
      }
      else {
        final DatedFile df = new DatedFile(f[i]);
        aCol.add(df);
      }
    }
  }

  /**
   * Take a snapshot of files under a given directory.
   * 
   * @param aDir - a directory
   */
  public static void snapshot(final String aDir) {
    oldCol = new ArrayList<DatedFile>();
    listFiles(new File(aDir), oldCol);
  }

  /**
   * Finds differences between the last computed collection and the current one on a given directory and returns the last modified
   * files.
   * 
   * @param aDir - a directory
   * @return String[] the array of last modified files
   */
  public static String[] getDiff(final String aDir) {
    final Collection<DatedFile> newCol = new ArrayList<DatedFile>();
    listFiles(new File(aDir), newCol);
    if (oldCol == null) {
      return null;
    }
    newCol.removeAll(oldCol);
    if (newCol.isEmpty()) {
      return null;
    }
    final Object[] df = newCol.toArray();
    final String[] res = new String[df.length];
    for (int i = 0; i < df.length; i++) {
      res[i] = df[i].toString();
    }
    return res;
  }
}

/**
 * Class to compare files more accurately on their last modification date.
 */
class DatedFile {

  /** The file */
  private final File fFile;
  /** The file modification date */
  private final long fDate;

  /**
   * Standard constructor.
   * 
   * @param aFile - a file
   */
  DatedFile(final File aFile) {
    fFile = aFile;
    fDate = aFile.lastModified();
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(final Object aObj) {
    final DatedFile obj = (DatedFile) aObj;
    if (this.fDate != obj.fDate) {
      return false;
    }
    return this.fFile.equals(obj.fFile);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return fFile.toString();
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
