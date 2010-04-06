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
   * @param c the character
   * @return true if c is '/', false otherwise
   * @see IWordDetector#isWordStart
   */
  public boolean isWordStart(final char c) {
    return c == '/';
  }

  /**
   * @param c the character
   * @return true if c is '*' or '/', false otherwise
   * @see IWordDetector#isWordPart
   */
  public boolean isWordPart(final char c) {
    return c == '*' || c == '/';
  }
}
