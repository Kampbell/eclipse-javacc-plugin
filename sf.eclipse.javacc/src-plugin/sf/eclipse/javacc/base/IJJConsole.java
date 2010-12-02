package sf.eclipse.javacc.base;

import java.io.PrintStream;

import org.eclipse.core.resources.IFile;

/**
 * Interface for a Console for JavaCC output.<br>
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public interface IJJConsole {

  /**
   * Clears the console.
   */
  public abstract void clear();

  /**
   * @return the PrintStream to write to Console
   */
  public abstract PrintStream getPrintStream();

  /**
   * Prints a string to Console.
   * 
   * @param aStr the text to print
   */
  public abstract void print(String aStr);

  /**
   * Ends reporting. Called when JJBuilder has finished.
   * 
   * @param aFile the file to report on
   * @param aIsJtb true if file is a JTB one, false otherwise.
   */
  public abstract void endReport(IFile aFile, boolean aIsJtb);

}