package sf.eclipse.javacc.editors;

import org.eclipse.jface.text.rules.IWordDetector;

/**
 * A word detector for detecting short multi line java comments (\/\*\*\/).
 * 
 * @author Remi Koutcherawy 2003-2010 CeCILL license http://www.cecill.info/index.en.html
 * @author Marc Mazas 2009-2010
 */
public class JJSMLJCDetector implements IWordDetector {

  // MMa 03/2010 : created

  /**
   * @param aCh the character
   * @return true if aCh is '/', false otherwise
   * @see IWordDetector#isWordStart
   */
  @Override
  public boolean isWordStart(final char aCh) {
    return aCh == '/';
  }

  /**
   * @param aCh the character
   * @return true if aCh is '*' or '/', false otherwise
   * @see IWordDetector#isWordPart
   */
  @Override
  public boolean isWordPart(final char aCh) {
    return aCh == '*' || aCh == '/';
  }
}
