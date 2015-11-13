package sf.eclipse.javacc.base;

import java.io.PrintStream;

import org.eclipse.core.resources.IFile;

/**
 * Interface for a Console for JavaCC output.
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014-2015
 */
public interface IConsole {

  // MMa 11/2014 : renamed
  // MMa 01/2015 : added method for displaying output

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
   * @param aStr - the text to print
   * @param aCmdFlag - true to print with the console command style, false as normal text
   */
  public abstract void print(String aStr, boolean aCmdFlag);

  /**
   * Prints a string and a new line to Console.
   * 
   * @param aStr - the text to print
   * @param aCmdFlag - true to print with the console command style, false as normal text
   */
  public abstract void println(String aStr, boolean aCmdFlag);

  /**
   * Prints a new line to Console.
   */
  public abstract void println();

  /**
   * Displays a command output.
   */
  public abstract void displayOutput();

  /**
   * Displays and processes compilation report. Called when the compile commands have finished.
   * 
   * @param aFile - the file to report on
   * @param aIsJtb - true if file is a JTB one, false otherwise
   */
  public abstract void processReport(IFile aFile, boolean aIsJtb);

  /**
   * @return a formatted date/timestamp
   */
  public abstract String fmtTS();

}