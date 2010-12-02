package sf.eclipse.javacc.headless;

import java.io.PrintStream;

import org.eclipse.core.resources.IFile;

import sf.eclipse.javacc.base.IJJConsole;

/**
 * Console for JavaCC output for headless builds.<br>
 * Referenced by plugin.xml<br>
 * <extension point="org.eclipse.ui.views"><br>
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class BasicConsole implements IJJConsole {

  /**
   * Clears the console.
   */
  public void clear() {
    /* nothing to do */
  }

  /**
   * Ends reporting. Called when JJBuilder has finished.
   * 
   * @param aFile the file to report on
   * @param aIsJtb true if file is a JTB one, false otherwise.
   */
  public void endReport(@SuppressWarnings("unused") final IFile aFile,
                        @SuppressWarnings("unused") final boolean aIsJtb) {
    /* nothing to do */
  }

  /**
   * @return the PrintStream to write to Console
   */
  public PrintStream getPrintStream() {
    return System.out;
  }

  /**
   * Prints a string to Console.
   * 
   * @param aStr the text to print
   */
  public void print(final String aStr) {
    System.out.print(aStr);
  }
}
