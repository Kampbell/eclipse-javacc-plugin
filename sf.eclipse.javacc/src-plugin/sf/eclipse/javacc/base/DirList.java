package sf.eclipse.javacc.base;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Find last added or modified files after a compilation of a .jjt or .jj file.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 * @author Bill Fenlason 2012
 */
public class DirList {

  // MMa 02/2010 : formatting and javadoc revision
  // BF  06/2012 : added required hashCode method to avoid warning message
  // MMa 11/2014 : some renamings

  /** The last collection of files */
  private static Collection<DatedFile> sLastColl;

  /**
   * Adds all files found under a given root directory to a given collection.
   * 
   * @param aRoot - the root directory
   * @param aColl - the Collection to fill
   */
  static void listFiles(final File aRoot, final Collection<DatedFile> aColl) {
    final File[] f = aRoot.listFiles();
    for (int i = 0; i < f.length; i++) {
      if (f[i].isDirectory()) {
        listFiles(f[i], aColl);
      }
      else {
        final DatedFile df = new DatedFile(f[i]);
        aColl.add(df);
      }
    }
  }

  /**
   * Take a snapshot of files under a given directory.
   * 
   * @param aDir - a directory
   */
  public static void snapshot(final String aDir) {
    final int sz = sLastColl == null ? 20 : 5 + sLastColl.size();
    sLastColl = new ArrayList<DatedFile>(sz);
    listFiles(new File(aDir), sLastColl);
  }

  /**
   * Finds differences between the last computed collection and the current one on a given directory and
   * returns the last modified files.
   * 
   * @param aDir - a directory
   * @return String[] the array of last modified files
   */
  public static String[] getDiff(final String aDir) {
    final int sz = sLastColl == null ? 20 : 5 + sLastColl.size();
    final Collection<DatedFile> newCol = new ArrayList<DatedFile>(sz);
    listFiles(new File(aDir), newCol);
    if (sLastColl == null) {
      return null;
    }
    newCol.removeAll(sLastColl);
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
  protected final File jFile;
  /** The file modification date */
  protected final long jDate;

  /**
   * Standard constructor.
   * 
   * @param aFile - a file
   */
  DatedFile(final File aFile) {
    jFile = aFile;
    jDate = aFile.lastModified();
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(final Object aObj) {
    final DatedFile obj = (DatedFile) aObj;
    if (this.jDate != obj.jDate) {
      return false;
    }
    return this.jFile.equals(obj.jFile);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return jFile.toString();
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
