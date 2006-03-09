package sf.eclipse.javacc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Find last added or modified files
 * after a compilation of a .jjt or .jj file
 * 
 * @author Remi Koutcherawy 2003-2006
 * CeCILL Licence http://www.cecill.info/index.en.html
 */
public class DirList {
  static Collection oldCol;
  
  /**
   * Add all files found under Directory root to collection col
   * @param File root (directory)
   * @param Collection col
   */
  protected static void listFiles(File root, Collection col) {
    File[] f = root.listFiles();
    for (int i = 0; i < f.length; i++) {
      if(f[i].isDirectory())
        listFiles(f[i], col);
      else{
        DatedFile df = new DatedFile(f[i]);
        col.add(df);
      }
    }
  }

  /**
   * Take a snapshot of files under root dir
   * @param dir
   */
  public static void snapshot(String dir) {
    oldCol = new ArrayList();
    listFiles(new File(dir), oldCol);
  }

  /**
   * Identify differences and return last modified files.
   * @param String dirname
   * @return String[] filename
   */
  public static String[] getDiff(String dir) {
    Collection newCol = new ArrayList();
    listFiles(new File(dir), newCol);
    if (oldCol == null) {
      return null;
    }
    newCol.removeAll(oldCol);
    if (newCol.isEmpty())
      return null;
    Object[] df = newCol.toArray();
    String[] res = new String[df.length];
    for(int i = 0; i < df.length; i++)
      res[i] = df[i].toString();
    return res;
  }
}

/**
 * Dated File to compare more accurately
 */
class DatedFile {
  private File f;
  private long date;
  
  DatedFile (File f) {
    this.f = f;
    this.date = f.lastModified();
  }

  public boolean equals(Object o) {
    DatedFile obj = (DatedFile) o;
    if (this.date != obj.date)
      return false;
    return this.f.equals(obj.f);
  }

  public String toString() {
    return f.toString();
  }
}

