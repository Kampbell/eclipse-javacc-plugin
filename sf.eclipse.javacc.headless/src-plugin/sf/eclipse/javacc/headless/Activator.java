package sf.eclipse.javacc.headless;

import sf.eclipse.javacc.base.AbstractActivator;
import sf.eclipse.javacc.base.IConsole;

/**
 * The main plugin for headless builds.<br>
 * Referenced by plugin.xml<br>
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012-2013-2014
 */
public class Activator extends AbstractActivator {

  // MMa 12/2014 : simplified by extending AbstractActivator

  /**
   * Creates a new output console. Specific to headless builds.
   * 
   * @return the console
   */
  @Override
  public IConsole getConsole() {
    return new NullConsole();
  }

}
