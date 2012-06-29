package sf.eclipse.javacc.scanners;

import org.eclipse.jface.text.rules.IWordDetector;

/**
 * A word detector for detecting short multi line java comments (\/\*\*\/).
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010-2011-2012
 * @author Bill Fenlason 2012
 */
public class JJSMLJCDetector implements IWordDetector {

  // MMa 03/2010 : created
  // BF  06/2012 : inheritDoc tags added

  /**
   * {@inheritDoc}
   * 
   * @return true if aCh is '/', false otherwise
   */
  @Override
  public boolean isWordStart(final char aCh) {
    return aCh == '/';
  }

  /**
   * {@inheritDoc}
   * 
   * @param aCh - the character
   * @return true if aCh is '*' or '/', false otherwise
   */
  @Override
  public boolean isWordPart(final char aCh) {
    return aCh == '*' || aCh == '/';
  }
}
