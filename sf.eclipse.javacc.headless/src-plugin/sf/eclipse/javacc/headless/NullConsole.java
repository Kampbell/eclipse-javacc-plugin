package sf.eclipse.javacc.headless;

import java.io.PrintStream;

import org.eclipse.core.resources.IFile;

import sf.eclipse.javacc.base.IConsole;

/**
 * Console for JavaCC output for headless builds.<br>
 * Referenced by plugin.xml<br>
 * <extension point="org.eclipse.ui.views"><br>
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014
 */
public class NullConsole implements IConsole {

  // MMa 11/2014 : renamed

  /** {@inheritDoc} */
  @Override
  public void clear() {
    /* no console, nothing done */
  }

  /** {@inheritDoc} */
  @Override
  public void processReport(@SuppressWarnings("unused") final IFile aFile, @SuppressWarnings("unused") final boolean aIsJtb) {
    /* no console, nothing done */
  }

  /** {@inheritDoc} */
  @Override
  public PrintStream getPrintStream() {
    return System.out;
  }

  /** {@inheritDoc} */
  @Override
  public void print(@SuppressWarnings("unused") final String aStr, @SuppressWarnings("unused") final boolean aCmdFlag) {
    /* no console, nothing done */
  }

  /** {@inheritDoc} */
  @Override
  public void println(@SuppressWarnings("unused") final String aStr, @SuppressWarnings("unused") final boolean aCmdFlag) {
    /* no console, nothing done */
  }

  /** {@inheritDoc} */
  @Override
  public void println() {
    /* no console, nothing done */
  }

  /** {@inheritDoc} */
  @Override
  public String fmtTS() {
    return "";
  }
}
